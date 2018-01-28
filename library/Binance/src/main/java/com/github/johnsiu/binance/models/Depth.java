package com.github.johnsiu.binance.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Represents depth of a symbol.
 */
public class Depth {

  private long lastUpdateId;
  private List<PriceQuantity> bids;
  private List<PriceQuantity> asks;

  @JsonCreator
  public Depth(@JsonProperty("lastUpdateId") long lastUpdateId, @JsonProperty("bids") List<?> bids,
      @JsonProperty("asks") List<?> asks) {
    this.lastUpdateId = lastUpdateId;

    Function<List<?>, List<PriceQuantity>> buildOrdersFunc = (orders) -> {
      Builder<PriceQuantity> ordersBuilder = ImmutableList.<PriceQuantity>builder();
      if (orders != null && orders.size() >= 2) {
        orders.forEach(value -> {
          Iterator iterator = Collection.class.cast(value).iterator();
          ordersBuilder.add(new PriceQuantity(Double.valueOf((String) iterator.next()),
              Double.valueOf((String) iterator.next())));
        });
      }
      return ordersBuilder.build();
    };
    this.bids = buildOrdersFunc.apply(bids);
    this.asks = buildOrdersFunc.apply(asks);
  }

  public long getLastUpdateId() {
    return lastUpdateId;
  }

  public List<PriceQuantity> getBids() {
    return bids;
  }

  public List<PriceQuantity> getAsks() {
    return asks;
  }

  @Override
  public String toString() {
    return "Depth{" +
        "lastUpdateId=" + lastUpdateId +
        ", bids=" + bids +
        ", asks=" + asks +
        '}';
  }
}
