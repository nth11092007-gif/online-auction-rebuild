package com.auction.common.exception;

/** PasswordStrengthCheck - exception thrown when a password does not meet strength requirements. */
public class PasswordStrengthCheck extends RuntimeException {
  public PasswordStrengthCheck() {
    super("Mật khẩu không đủ mạnh!");
  }
}
