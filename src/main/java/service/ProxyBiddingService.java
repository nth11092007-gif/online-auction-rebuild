// service/ProxyBiddingService.java
package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import dao.AuctionSessionDAO;
import dao.AuctionSessionDAOImpl;
import dao.BidDAO;
import dao.BidDAOImpl;
import dao.ProxyBidDAO;
import dao.ProxyBidDAOImpl;
import dao.UserDAO;
import dao.UserDAOImpl;
import model.AuctionSession;
import model.Bid;
import model.ProxyBid;
import model.User;
import utils.DBConnection;

public class ProxyBiddingService {
    private final ProxyBidDAO proxyBidDAO = new ProxyBidDAOImpl();
    private final AuctionService auctionService;
    private final UserDAO userDAO = new UserDAOImpl();
    private final AuctionSessionDAO sessionDAO = new AuctionSessionDAOImpl();
    private final BidDAO bidDAO = new BidDAOImpl();

    public ProxyBiddingService(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    // Đặt proxy bid
    public void placeProxyBid(int userId, String sessionId, double maxAmount) throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            AuctionSession session = sessionDAO.getSessionById(conn, sessionId);
            if (session == null || !session.getState().canJoin()) {
                throw new IllegalArgumentException("Phiên không tồn tại hoặc không thể tham gia");
            }
            Bid highestBid = bidDAO.getHighestBid(conn, sessionId);
            double currentPrice = (highestBid != null) ? highestBid.getAmount() : session.getStartingPrice();
            double increment = session.getIncrementStep();
            if (maxAmount <= currentPrice + increment) {
                throw new IllegalArgumentException("Mức tối đa phải cao hơn giá hiện tại ít nhất một bước giá");
            }
            // Kiểm tra số dư (tối thiểu phải đủ một bước)
            User user = userDAO.getUserById(conn, userId);
            if (user.getBalance() < currentPrice + increment) {
                throw new IllegalArgumentException("Số dư không đủ để đặt proxy");
            }
            // Vô hiệu proxy cũ nếu có
            ProxyBid oldProxy = proxyBidDAO.getActiveProxyBid(conn, userId, sessionId);
            if (oldProxy != null) {
                proxyBidDAO.deactivateProxyBid(conn, oldProxy.getId());
            }
            ProxyBid newProxy = new ProxyBid(userId, sessionId, maxAmount);
            proxyBidDAO.addProxyBid(conn, newProxy);
            conn.commit();
        }
        // Kích hoạt xử lý proxy ngay sau khi đặt
        processProxyBids(sessionId);
    }

    // Xử lý tất cả proxy cho một phiên (gọi sau khi có bid mới hoặc khi kích hoạt)
    public void processProxyBids(String sessionId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Lấy thông tin phiên và giá cao nhất hiện tại
            AuctionSession session = sessionDAO.getSessionById(conn, sessionId);
            if (session == null || !session.getState().canJoin()) {
                return;
            }
            Bid highestBid = bidDAO.getHighestBid(conn, sessionId);
            double currentPrice = (highestBid != null) ? highestBid.getAmount() : session.getStartingPrice();
            double increment = session.getIncrementStep();

            // Lấy danh sách proxy active, sắp xếp theo maxAmount giảm dần
            List<ProxyBid> proxyBids = proxyBidDAO.getActiveProxyBidsBySession(conn, sessionId);
            proxyBids.sort(Comparator.comparingDouble(ProxyBid::getMaxAmount).reversed());

            int currentHighestUserId = (highestBid != null) ? highestBid.getBidder().getID() : -1;

            for (ProxyBid proxy : proxyBids) {
                // Bỏ qua nếu user này đang giữ giá cao nhất
                if (proxy.getUserId() == currentHighestUserId) continue;

                double proxyMax = proxy.getMaxAmount();
                if (proxyMax >= currentPrice + increment) {
                    double bidAmount = currentPrice + increment;
                    User user = userDAO.getUserById(conn, proxy.getUserId());
                    if (user != null && user.getBalance() >= bidAmount) {
                        // Gọi placeBid – phương thức này tự lo transaction và sẽ tự động freeze/unfreeze
                        boolean success = auctionService.placeBid(proxy.getUserId(), sessionId, bidAmount);
                        if (success) {
                            // Cập nhật giá hiện tại và người cao nhất cho vòng lặp
                            currentPrice = bidAmount;
                            currentHighestUserId = proxy.getUserId();
                        } else {
                            // Nếu thất bại (không đủ tiền sau khi freeze?) => vô hiệu proxy
                            proxyBidDAO.deactivateProxyBid(conn, proxy.getId());
                        }
                    } else {
                        proxyBidDAO.deactivateProxyBid(conn, proxy.getId());
                    }
                } else {
                    // Proxy không còn khả năng vượt giá => vô hiệu
                    proxyBidDAO.deactivateProxyBid(conn, proxy.getId());
                }
            }
            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}