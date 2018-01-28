package com.github.johnsiu.binance.models;

/**
 * Represents an error from Binance API.
 */
public class ErrorDetails {

  private long code;
  private String msg;

  public long getCode() {
    return code;
  }

  public String getMsg() {
    return msg;
  }

  @Override
  public String toString() {
    return "ErrorDetails{" +
        "code=" + code +
        ", msg='" + msg + '\'' +
        '}';
  }
}
