package com.auction.server.service;

import com.auction.server.dao.AuctionSessionDAO;
import com.auction.server.dao.AuctionSessionDAOImpl;
import com.auction.server.dao.BidDAO;
import com.auction.server.dao.BidDAOImpl;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.ItemDAOImpl;
import com.auction.server.dao.UserDAO;
import com.auction.server.dao.UserDAOImpl;

/**
 * Singleton cung cấp các service instances chung cho toàn bộ ứng dụng.
 * Controllers nên dùng class này thay vì tự new DAO/Service.
 */
public class ServiceFactory {

  private static class Holder {
    private static final ServiceFactory INSTANCE =
        new ServiceFactory();
  }

  public static ServiceFactory getInstance() {
    return Holder.INSTANCE;
  }

  private final AuctionService auctionService;
  private final UserService userService;
  private final SettlementService settlementService;
  private final UserDAO userDao;
  private final BidDAO bidDao;
  private final ItemDAO itemDao;
  private final AuctionSessionDAO sessionDao;

  private ServiceFactory() {
    this.userDao = new UserDAOImpl();
    this.bidDao = new BidDAOImpl();
    this.itemDao = new ItemDAOImpl();
    this.sessionDao = new AuctionSessionDAOImpl();
    this.auctionService = new AuctionService();
    this.userService = new UserService(userDao);
    this.settlementService = new SettlementService();
  }

  public AuctionService getAuctionService() {
    return auctionService;
  }

  public UserService getUserService() {
    return userService;
  }

  public SettlementService getSettlementService() {
    return settlementService;
  }

  public UserDAO getUserDao() {
    return userDao;
  }

  public BidDAO getBidDao() {
    return bidDao;
  }

  public ItemDAO getItemDao() {
    return itemDao;
  }

  public AuctionSessionDAO getSessionDao() {
    return sessionDao;
  }
}
