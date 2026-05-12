package model;

import java.util.ArrayList;
import java.util.List;
/* 
Mẫu thiết kế áp dụng: "Bill Pugh Singleton - sử dụng một static inner class (lớp tĩnh bên trong). 
Lớp tĩnh này chỉ được JVM tải vào bộ nhớ và khởi tạo đối tượng khi hàm getInstance() được gọi lần đầu tiên (Lazy Loading),
đồng thời tận dụng cơ chế nội tại của JVM để đảm bảo an toàn luồng (Thread-safe) mà không gây ảnh hưởng đến hiệu suất.
*/

public class AuctionManager {
    final List<AuctionSession> auctionList; // final để không thay đổi tham chiếu, vẫn có thể thay đổi nội dung bên trong
    private AuctionManager(){
        this.auctionList = new ArrayList<>();
    };
    private static class SingletonHelper {
        private static final AuctionManager AUCTION_MANAGER = new AuctionManager();
    }
    public static AuctionManager getInstance(){
        return SingletonHelper.AUCTION_MANAGER;
    }
    public void addSession(AuctionSession session){
        if (session != null){
            auctionList.add(session);
            System.out.println("Đã thêm phiên đấu giá [" + session.getSessionID() + "] vào hệ thống quản lý.");
        }
    }
    /**
     * Trả về 1 list các phiên đấu giá dựa trên trạng thái của nó.
     */
    public List<AuctionSession> getSessionsByStatus(AuctionSession.Status status) {
        List<AuctionSession> filteredList = new ArrayList<>();
        for (AuctionSession session : auctionList) {
            if (session.getStatus().equals(status)) {
                filteredList.add(session);
            }
        }
        return filteredList;
    }
    public AuctionSession findSessionByID(String sessionID) {
        for (AuctionSession session : auctionList) {
            if (session.getSessionID().equals(sessionID)) {
                return session; // Tìm thấy thì trả về phiên đó
            }
        }
        System.out.println("Không tìm thấy phiên đấu giá với ID: " + sessionID);
        return null; // Không tìm thấy
    }
}
