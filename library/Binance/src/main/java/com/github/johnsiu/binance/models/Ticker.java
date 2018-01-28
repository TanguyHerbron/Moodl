package com.github.johnsiu.binance.models;

import java.util.Date;

/**
 * Represents a ticker of a symbol.
 */
public class Ticker {

  private double priceChange;
  private double priceChangePercent;
  private double weightedAvgPrice;
  private double prevClosePrice;
  private double lastPrice;
  private double lastQty;
  private double bidPrice;
  private double bidQty;
  private double askPrice;
  private double askQty;
  private double openPrice;
  private double highPrice;
  private double lowPrice;
  private double volume;
  private double quoteVolume;
  private Date openTime;
  private Date closeTime;
  private long firstId;
  private long lastId;
  private long count;

  public double getPriceChange() {
    return priceChange;
  }

  public double getPriceChangePercent() {
    return priceChangePercent;
  }

  public double getWeightedAvgPrice() {
    return weightedAvgPrice;
  }

  public double getPrevClosePrice() {
    return prevClosePrice;
  }

  public double getLastPrice() {
    return lastPrice;
  }

  public double getLastQty() {
    return lastQty;
  }

  public double getBidPrice() {
    return bidPrice;
  }

  public double getBidQty() {
    return bidQty;
  }

  public double getAskPrice() {
    return askPrice;
  }

  public double getAskQty() {
    return askQty;
  }

  public double getOpenPrice() {
    return openPrice;
  }

  public double getHighPrice() {
    return highPrice;
  }

  public double getLowPrice() {
    return lowPrice;
  }

  public double getVolume() {
    return volume;
  }

  public double getQuoteVolume() {
    return quoteVolume;
  }

  public Date getOpenTime() {
    return openTime;
  }

  public Date getCloseTime() {
    return closeTime;
  }

  public long getFirstId() {
    return firstId;
  }

  public long getLastId() {
    return lastId;
  }

  public long getCount() {
    return count;
  }

  @Override
  public String toString() {
    return "Ticker{" +
        "priceChange=" + priceChange +
        ", priceChangePercent=" + priceChangePercent +
        ", weightedAvgPrice=" + weightedAvgPrice +
        ", prevClosePrice=" + prevClosePrice +
        ", lastPrice=" + lastPrice +
        ", lastQty=" + lastQty +
        ", bidPrice=" + bidPrice +
        ", bidQty=" + bidQty +
        ", askPrice=" + askPrice +
        ", askQty=" + askQty +
        ", openPrice=" + openPrice +
        ", highPrice=" + highPrice +
        ", lowPrice=" + lowPrice +
        ", volume=" + volume +
        ", quoteVolume=" + quoteVolume +
        ", openTime=" + openTime +
        ", closeTime=" + closeTime +
        ", firstId=" + firstId +
        ", lastId=" + lastId +
        ", count=" + count +
        '}';
  }

}
