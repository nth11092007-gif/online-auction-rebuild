// service/ProxyBiddingService.java
package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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

    public void placeProxyBid(int userId, String sessionId, double maxAmount) throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            AuctionSession session = sessionDAO.getSessionById(conn, sessionId);
            if (session == null || !session.joinable()) {
                throw new IllegalArgumentException("Phiên không tồn tại hoặc không thể tham gia");
            }
            Bid highestBid = bidDAO.getHighestBid(conn, sessionId);
            double currentPrice = (highestBid != null) ? highestBid.getAmount() : session.getStartingPrice();
            double increment = session.getIncrementStep();
            if (maxAmount <= currentPrice + increment) {
                throw new IllegalArgumentException("Mức tối đa phải cao hơn giá hiện tại ít nhất một bước giá");
            }
            User user = userDAO.getUserById(conn, userId);
            if (user.getBalance() < currentPrice + increment) {
                throw new IllegalArgumentException("Số dư không đủ để đặt proxy");
            }
            ProxyBid oldProxy = proxyBidDAO.getActiveProxyBid(conn, userId, sessionId);
            if (oldProxy != null) {
                proxyBidDAO.deactivateProxyBid(conn, oldProxy.getId());
            }
            ProxyBid newProxy = new ProxyBid(userId, sessionId, maxAmount);
            proxyBidDAO.addProxyBid(conn, newProxy);
            conn.commit();
        }
        processProxyBids(sessionId);
    }

    /**
     * Xử lý proxy: đọc DB trong transaction ngắn, mỗi lần đặt giá qua placeBid() dùng connection riêng.
     */
    public void processProxyBids(String sessionId) {
        List<ProxyBid> proxyBids;
        double increment;

        try (Connection conn = DBConnection.getConnection()) {
            AuctionSession session = sessionDAO.getSessionById(conn, sessionId);
            if (session == null || !session.joinable()) {
                return;
            }
            increment = session.getIncrementStep();
            proxyBids = new ArrayList<>(proxyBidDAO.getActiveProxyBidsBySession(conn, sessionId));
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        proxyBids.sort(Comparator.comparingDouble(ProxyBid::getMaxAmount).reversed());

        for (ProxyBid proxy : proxyBids) {
            double bidAmount;
            try (Connection conn = DBConnection.getConnection()) {
                AuctionSession session = sessionDAO.getSessionById(conn, sessionId);
                if (session == null || !session.joinable()) {
                    return;
                }
                Bid highestBid = bidDAO.getHighestBid(conn, sessionId);
                double currentPrice = (highestBid != null) ? highestBid.getAmount() : session.getStartingPrice();
                int currentHighestUserId = (highestBid != null) ? highestBid.getBidder().getID() : -1;

                if (proxy.getUserId() == currentHighestUserId) {
                    continue;
                }

                double proxyMax = proxy.getMaxAmount();
                if (proxyMax < currentPrice + increment) {
                    proxyBidDAO.deactivateProxyBid(conn, proxy.getId());
                    continue;
                }

                bidAmount = currentPrice + increment;
                User user = userDAO.getUserById(conn, proxy.getUserId());
                if (user == null || user.getBalance() < bidAmount) {
                    proxyBidDAO.deactivateProxyBid(conn, proxy.getId());
                    continue;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                continue;
            }

            boolean success = auctionService.placeBid(proxy.getUserId(), sessionId, bidAmount);
            if (!success) {
                try (Connection conn = DBConnection.getConnection()) {
                    proxyBidDAO.deactivateProxyBid(conn, proxy.getId());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
