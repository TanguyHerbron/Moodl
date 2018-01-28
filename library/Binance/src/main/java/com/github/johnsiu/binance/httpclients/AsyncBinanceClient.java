package com.github.johnsiu.binance.httpclients;

import com.github.johnsiu.binance.models.Account;
import com.github.johnsiu.binance.models.CancelOrder;
import com.github.johnsiu.binance.models.Depth;
import com.github.johnsiu.binance.models.Keys;
import com.github.johnsiu.binance.models.Order;
import com.github.johnsiu.binance.models.Order.OrderSide;
import com.github.johnsiu.binance.models.Order.TimeInForce;
import com.github.johnsiu.binance.models.OrderStatus;
import com.github.johnsiu.binance.models.Ticker;
import com.google.inject.ImplementedBy;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Async client for accessing Binance API.
 */
@ImplementedBy(AsyncBinanceClientImpl.class)
public interface AsyncBinanceClient {

  /**
   * Get the ticker given the symbol.
   */
  CompletionStage<Ticker> getTicker(String symbol);

  /**
   * Get the depth given the symbol. Limit defaults to 100.
   */
  CompletionStage<Depth> getDepth(String symbol);

  /**
   * Get the depth given the symbol and limit.
   */
  CompletionStage<Depth> getDepth(String symbol, int limit);

  /**
   * Place a LIMIT order.
   */
  CompletionStage<Order> placeLimitOrder(Keys keys, String symbol, OrderSide side,
      TimeInForce timeInForce, double quantity, double price);

  /**
   * Place a MARKET order.
   */
  CompletionStage<Order> placeMarketOrder(Keys keys, String symbol, OrderSide side,
      double quantity);

  /**
   * Check the status of an order.
   */
  CompletionStage<OrderStatus> checkOrderStatus(Keys keys, Order order);

  /**
   * Cancel an order.
   */
  CompletionStage<CancelOrder> cancelOrder(Keys keys, Order order);

  /**
   * Get all open orders.
   */
  CompletionStage<List<OrderStatus>> getOpenOrders(Keys keys, String symbol);

  /**
   * Get account info.
   */
  CompletionStage<Account> getAccount(Keys keys);

}
