package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import model.ProxyBid;

public interface ProxyBidDAO {
    void addProxyBid(Connection conn, ProxyBid proxyBid) throws SQLException;
    List<ProxyBid> getActiveProxyBidsBySession(Connection conn, String sessionId) throws SQLException;
    ProxyBid getActiveProxyBid(Connection conn, int userId, String sessionId) throws SQLException;
    void deactivateProxyBid(Connection conn, int proxyBidId) throws SQLException;
    void deactivateAllProxyBidsForSession(Connection conn, String sessionId) throws SQLException;
}