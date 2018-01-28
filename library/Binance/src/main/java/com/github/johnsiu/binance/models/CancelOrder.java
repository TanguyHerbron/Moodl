package com.github.johnsiu.binance.models;

/**
 * Represents a cancel order.
 */
public class CancelOrder {

  private String symbol;
  private long orderId;
  private String origClientOrderId;
  private String clientOrderId;

  public String getSymbol() {
    return symbol;
  }

  public long getOrderId() {
    return orderId;
  }

  public String getOrigClientOrderId() {
    return origClientOrderId;
  }

  public String getClientOrderId() {
    return clientOrderId;
  }

  @Override
  public String toString() {
    return "CancelOrder{" +
        "symbol='" + symbol + '\'' +
        ", orderId=" + orderId +
        ", origClientOrderId='" + origClientOrderId + '\'' +
        ", clientOrderId='" + clientOrderId + '\'' +
        '}';
  }
}
