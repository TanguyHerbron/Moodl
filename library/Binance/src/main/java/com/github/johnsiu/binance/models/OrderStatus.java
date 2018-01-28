package com.github.johnsiu.binance.models;

import java.util.Date;

/**
 * Represents an order status. Can be cancelled.
 */
public class OrderStatus extends Order {

  private String price;
  private String origQty;
  private String executedQty;
  private Status status;
  private TimeInForce timeInForce;
  private OrderType type;
  private OrderSide side;
  private String stopPrice;
  private String icebergQty;
  private Date time;

  public String getPrice() {
    return price;
  }

  public String getOrigQty() {
    return origQty;
  }

  public String getExecutedQty() {
    return executedQty;
  }

  public Status getStatus() {
    return status;
  }

  public TimeInForce getTimeInForce() {
    return timeInForce;
  }

  public OrderType getType() {
    return type;
  }

  public OrderSide getSide() {
    return side;
  }

  public String getStopPrice() {
    return stopPrice;
  }

  public String getIcebergQty() {
    return icebergQty;
  }

  public Date getTime() {
    return time;
  }

  @Override
  public String toString() {
    return "OrderStatus{" +
        "price='" + price + '\'' +
        ", origQty='" + origQty + '\'' +
        ", executedQty='" + executedQty + '\'' +
        ", status=" + status +
        ", timeInForce=" + timeInForce +
        ", type=" + type +
        ", side=" + side +
        ", stopPrice='" + stopPrice + '\'' +
        ", icebergQty='" + icebergQty + '\'' +
        ", time=" + time +
        "} " + super.toString();
  }

  public enum Status {
    NEW,
    PARTIALLY_FILLED,
    FILLED,
    CANCELED,
    PENDING_CANCEL,
    REJECTED,
    EXPIRED
  }
}
