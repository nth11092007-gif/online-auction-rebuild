package com.auction.common.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles deposit and withdrawal transactions for users.
 */
public class Transaction {

  private final Logger logger =
      LoggerFactory.getLogger(Transaction.class);

  /**
   * Deposits the specified amount into the user's account.
   *
   * @param user the user receiving the deposit
   * @param amount the amount to deposit
   */
  public void deposit(User user, double amount) {
    if (amount > 0) {
      user.deposit(amount);
      logger.info("Nạp thành công {} vào tài khoản của {}",
          amount, user.getUsername());
    } else {
      logger.error("Không thể thực hiện giao dịch!");
    }
  }

  /**
   * Withdraws the specified amount from the user's account.
   *
   * @param user the user making the withdrawal
   * @param amount the amount to withdraw
   */
  public void withdraw(User user, double amount) {
    if (amount > 0 && user.getBalance() >= amount) {
      user.withdraw(amount);
      logger.info("Rút thành công {} từ tài khoản của {}",
          amount, user.getUsername());
    } else {
      logger.error("Không thể thực hiện giao dịch!");
    }
  }
}
