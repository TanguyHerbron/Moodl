package com.github.johnsiu.binance.models;

import java.util.Date;

/**
 * Represents an Order. Can be cancelled.
 */
public class Order {

  private String symbol;
  private long orderId;
  private String clientOrderId;
  private Date transactTime;

  public String getSymbol() {
    return symbol;
  }

  public long getOrderId() {
    return orderId;
  }

  public String getClientOrderId() {
    return clientOrderId;
  }

  public Date getTransactTime() {
    return transactTime;
  }

  @Override
  public String toString() {
    return "Order{" +
        "symbol='" + symbol + '\'' +
        ", orderId=" + orderId +
        ", clientOrderId='" + clientOrderId + '\'' +
        ", transactTime=" + transactTime +
        '}';
  }

  public enum OrderSide {
    BUY, SELL
  }

  public enum OrderType {
    LIMIT, MARKET
  }

  public enum TimeInForce {
    // Good Till Cancel
    GTC,
    // Immediate or Cancel
    IOC
  }
}
