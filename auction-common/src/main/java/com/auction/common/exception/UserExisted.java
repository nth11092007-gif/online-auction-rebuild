package com.auction.common.exception;

/** UserExisted - exception thrown when attempting to register an already existing username. */
public class UserExisted extends RuntimeException {
  public UserExisted() {
    super("Tên người dùng đã tồn tại!");
  }
}
