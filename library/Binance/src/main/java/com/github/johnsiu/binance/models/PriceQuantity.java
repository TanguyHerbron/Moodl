package com.github.johnsiu.binance.models;

/**
 * Represents price and quantity of a bid or an ask.
 */
public class PriceQuantity {

  private double price;
  private double quantity;

  public PriceQuantity(double price, double quantity) {
    this.price = price;
    this.quantity = quantity;
  }

  public double getPrice() {
    return price;
  }

  public double getQuantity() {
    return quantity;
  }

  @Override
  public String toString() {
    return "PriceQuantity{" +
        "price=" + price +
        ", quantity=" + quantity +
        '}';
  }
}
