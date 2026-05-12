package server;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionFeedServer {
    private final Map<String, List<Observer>> sessionObservers = new ConcurrentHashMap<>();
    public void subscribe(String sessionId, Observer observer) {
        sessionObservers.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>()).add(observer);
    }
    public void unsubscribe(String sessionId, Observer observer) {
        List<Observer> observers = sessionObservers.get(sessionId);
        if (observers != null) {
            observers.remove(observer);
        }
    }
    public void notifyObservers(String sessionId, String message) {
        List<Observer> observers = sessionObservers.get(sessionId);

        // Kiểm tra xem có ai đang theo dõi phiên này không (tránh lỗi NullPointerException)
        if (observers != null) {
            // Chạy vòng lặp và gửi thông báo
            for (Observer observer : observers) {
                observer.update(message);
            }
        }
    }
}
