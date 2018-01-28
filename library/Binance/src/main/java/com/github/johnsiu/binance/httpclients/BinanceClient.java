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

/**
 * Client for accessing Binance API.
 */
@ImplementedBy(BinanceClientImpl.class)
public interface BinanceClient {

  /**
   * Get the ticker given the symbol.
   */
  Ticker getTicker(String symbol);

  /**
   * Get the depth given the symbol. Limit defaults to 100.
   */
  Depth getDepth(String symbol);

  /**
   * Get the depth given the symbol and limit.
   */
  Depth getDepth(String symbol, int limit);

  /**
   * Place a LIMIT order.
   */
  Order placeLimitOrder(Keys keys, String symbol, OrderSide side,
      TimeInForce timeInForce, double quantity, double price);

  /**
   * Place a MARKET order.
   */
  Order placeMarketOrder(Keys keys, String symbol, OrderSide side,
      double quantity);

  /**
   * Check the status of an order.
   */
  OrderStatus checkOrderStatus(Keys keys, Order order);

  /**
   * Cancel an order.
   */
  CancelOrder cancelOrder(Keys keys, Order order);

  /**
   * Get all open orders.
   */
  List<OrderStatus> getOpenOrders(Keys keys, String symbol);

  /**
   * Get account info.
   */
  Account getAccount(Keys keys);

}
