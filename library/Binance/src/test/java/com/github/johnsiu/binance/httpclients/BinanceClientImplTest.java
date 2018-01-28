package com.github.johnsiu.binance.httpclients;

import com.github.johnsiu.binance.exceptions.ClientErrorException;
import com.github.johnsiu.binance.inject.BinanceClientModule;
import com.github.johnsiu.binance.models.Account;
import com.github.johnsiu.binance.models.Depth;
import com.github.johnsiu.binance.models.Keys;
import com.github.johnsiu.binance.models.Order;
import com.github.johnsiu.binance.models.Order.OrderSide;
import com.github.johnsiu.binance.models.Order.TimeInForce;
import com.github.johnsiu.binance.models.OrderStatus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for {@link BinanceClientImpl}
 */
public class BinanceClientImplTest {

  @Test
  public void testGetTicker() {
    Injector injector = Guice.createInjector(new BinanceClientModule());
    BinanceClient client = injector.getInstance(BinanceClient.class);

    Assert.assertNotNull(client.getTicker("ETHBTC"));
  }

  @Test
  public void testGetDepth() {
    Injector injector = Guice.createInjector(new BinanceClientModule());
    BinanceClient client = injector.getInstance(BinanceClient.class);

    Assert.assertNotNull(client.getDepth("ETHBTC"));
  }

  @Test
  public void testGetDepthError() {
    Injector injector = Guice.createInjector(new BinanceClientModule());
    BinanceClient client = injector.getInstance(BinanceClient.class);

    try {
      Depth depth = client.getDepth("ETHBT");
      Assert.fail();
    } catch (Exception e) {
      ClientErrorException cast = (ClientErrorException) e;
      Assert.assertEquals("Invalid symbol.", cast.getErrorDetails().getMsg());
    }
  }

  @Test
  public void testOrder() {
    Injector injector = Guice.createInjector(new BinanceClientModule());
    BinanceClient client = injector.getInstance(BinanceClient.class);

    Keys keys = new Keys(System.getProperty("apiKey"),
        System.getProperty("secretKey"));
    String symbol = "MCOETH";
    Order order = client
        .placeLimitOrder(keys, symbol, OrderSide.BUY, TimeInForce.GTC, 1, 0.020041);
    Assert.assertNotNull(order);

    OrderStatus orderStatus = client.checkOrderStatus(keys, order);
    Assert.assertNotNull(orderStatus);

    List<OrderStatus> openOrders = client.getOpenOrders(keys, symbol);
    Assert.assertFalse(openOrders.isEmpty());

    openOrders.forEach(openOrder -> client.cancelOrder(keys, openOrder));

    openOrders = client.getOpenOrders(keys, symbol);
    Assert.assertTrue(openOrders.isEmpty());
  }

  @Test
  public void testAccount() {
    Injector injector = Guice.createInjector(new BinanceClientModule());
    BinanceClient client = injector.getInstance(BinanceClient.class);

    Keys keys = new Keys(System.getProperty("apiKey"),
        System.getProperty("secretKey"));
    Account account = client.getAccount(keys);
    Assert.assertNotNull(account);
  }
}
