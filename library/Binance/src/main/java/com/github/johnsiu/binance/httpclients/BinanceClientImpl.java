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
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;

/**
 * Concrete implementation of {@link BinanceClient}.
 */
public class BinanceClientImpl implements BinanceClient {

  private final AsyncBinanceClient asyncBinanceClient;

  @Inject
  public BinanceClientImpl(AsyncBinanceClient asyncBinanceClient) {
    this.asyncBinanceClient = asyncBinanceClient;
  }

  @Override
  public Ticker getTicker(String symbol) {
    return joinAsync(asyncBinanceClient.getTicker(symbol));
  }

  @Override
  public Depth getDepth(String symbol) {
    return joinAsync(asyncBinanceClient.getDepth(symbol));
  }

  @Override
  public Depth getDepth(String symbol, int limit) {
    return joinAsync(asyncBinanceClient.getDepth(symbol, limit));
  }

  @Override
  public Order placeLimitOrder(Keys keys, String symbol, OrderSide side,
      TimeInForce timeInForce, double quantity, double price) {
    return joinAsync(
        asyncBinanceClient.placeLimitOrder(keys, symbol, side, timeInForce, quantity, price)
    );
  }

  @Override
  public Order placeMarketOrder(Keys keys, String symbol, OrderSide side,
      double quantity) {
    return joinAsync(
        asyncBinanceClient.placeMarketOrder(keys, symbol, side, quantity)
    );
  }

  @Override
  public OrderStatus checkOrderStatus(Keys keys, Order order) {
    return joinAsync(asyncBinanceClient.checkOrderStatus(keys, order));
  }

  @Override
  public CancelOrder cancelOrder(Keys keys, Order order) {
    return joinAsync(asyncBinanceClient.cancelOrder(keys, order));
  }

  @Override
  public List<OrderStatus> getOpenOrders(Keys keys, String symbol) {
    return joinAsync(asyncBinanceClient.getOpenOrders(keys, symbol));
  }

  @Override
  public Account getAccount(Keys keys) {
    return joinAsync(asyncBinanceClient.getAccount(keys));
  }

  private <T> T joinAsync(CompletionStage<T> completionStage) {
    try {
      return completionStage.toCompletableFuture().join();
    } catch (CompletionException e) {
      if (e.getCause() instanceof RuntimeException) {
        throw (RuntimeException) e.getCause();
      }
      throw new RuntimeException(e.getCause());
    }
  }
}
