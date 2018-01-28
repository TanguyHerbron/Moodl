package com.github.johnsiu.binance.httpclients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.johnsiu.binance.exceptions.ClientErrorException;
import com.github.johnsiu.binance.inject.BinanceClientModule;
import com.github.johnsiu.binance.models.Account;
import com.github.johnsiu.binance.models.CancelOrder;
import com.github.johnsiu.binance.models.Depth;
import com.github.johnsiu.binance.models.ErrorDetails;
import com.github.johnsiu.binance.models.Keys;
import com.github.johnsiu.binance.models.Order;
import com.github.johnsiu.binance.models.Order.OrderSide;
import com.github.johnsiu.binance.models.Order.OrderType;
import com.github.johnsiu.binance.models.Order.TimeInForce;
import com.github.johnsiu.binance.models.OrderStatus;
import com.github.johnsiu.binance.models.Ticker;
import io.netty.handler.codec.http.HttpMethod;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Named;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Response;

/**
 * Concrete implementation of {@link AsyncBinanceClient}.
 */
public class AsyncBinanceClientImpl implements AsyncBinanceClient {

  private static final String HMAC_SHA256 = "HmacSHA256";
  private static final String APIKEY_HEADER = "X-MBX-APIKEY";
  private static final String ORDER_ENDPOINT = "order";
  private static final String OPEN_ORDERS_ENDPOINT = "openOrders";
  private static final String ACCOUNT_ENDPOINT = "account";
  private final String API_URL = "https://www.binance.com/api/v1/";
  private final String API_V3_URL = "https://www.binance.com/api/v3/";
  private final AsyncHttpClient asyncHttpClient;
  private final ObjectMapper objectMapper;

  @Inject
  public AsyncBinanceClientImpl(AsyncHttpClient asyncHttpClient,
      @Named(BinanceClientModule.BINANCE_CLIENT) ObjectMapper objectMapper) {
    this.asyncHttpClient = asyncHttpClient;
    this.objectMapper = objectMapper;
  }

  @Override
  public CompletionStage<Ticker> getTicker(String symbol) {

    return handleResponse(
        asyncHttpClient.prepareGet(API_URL + "ticker/24hr?symbol=" + symbol).execute()
            .toCompletableFuture(), new TypeReference<Ticker>() {
        });
  }

  @Override
  public CompletionStage<Depth> getDepth(String symbol) {
    return getDepth(symbol, 100);
  }

  @Override
  public CompletionStage<Depth> getDepth(String symbol, int limit) {

    return handleResponse(asyncHttpClient.prepareGet(API_URL + "depth?symbol=" + symbol).execute()
        .toCompletableFuture(), new TypeReference<Depth>() {
    });
  }

  @Override
  public CompletionStage<Order> placeLimitOrder(Keys keys, String symbol, OrderSide side,
      TimeInForce timeInForce, double quantity, double price) {

    String queryStr = String.format(
        "symbol=%s&side=%s&type=%s&timeInForce=%s&quantity=%f&price=%f&timestamp=%d",
        symbol, side.name(), OrderType.LIMIT, timeInForce.name(), quantity, price,
        System.currentTimeMillis());

    return makeSignedRequest(keys, HttpMethod.POST, ORDER_ENDPOINT, queryStr,
        new TypeReference<Order>() {
        });
  }

  @Override
  public CompletionStage<Order> placeMarketOrder(Keys keys, String symbol, OrderSide side,
      double quantity) {

    String queryStr = String.format(
        "symbol=%s&side=%s&type=%s&quantity=%f&timestamp=%d",
        symbol, side.name(), OrderType.MARKET, quantity,
        System.currentTimeMillis());

    return makeSignedRequest(keys, HttpMethod.POST, ORDER_ENDPOINT, queryStr,
        new TypeReference<Order>() {
        });
  }

  @Override
  public CompletionStage<OrderStatus> checkOrderStatus(Keys keys, Order order) {

    String queryStr = String.format(
        "symbol=%s&orderId=%d&origClientOrderId=%s&timestamp=%d",
        order.getSymbol(), order.getOrderId(), order.getClientOrderId(),
        System.currentTimeMillis());

    return makeSignedRequest(keys, HttpMethod.GET, ORDER_ENDPOINT, queryStr,
        new TypeReference<OrderStatus>() {
        });
  }

  @Override
  public CompletionStage<CancelOrder> cancelOrder(Keys keys, Order order) {

    String queryStr = String.format(
        "symbol=%s&orderId=%d&origClientOrderId=%s&timestamp=%d",
        order.getSymbol(), order.getOrderId(), order.getClientOrderId(),
        System.currentTimeMillis());
    return makeSignedRequest(keys, HttpMethod.DELETE, ORDER_ENDPOINT, queryStr,
        new TypeReference<CancelOrder>() {
        });
  }

  @Override
  public CompletionStage<List<OrderStatus>> getOpenOrders(Keys keys, String symbol) {

    String queryStr = String.format(
        "symbol=%s&timestamp=%d", symbol, System.currentTimeMillis());

    return makeSignedRequest(keys, HttpMethod.GET, OPEN_ORDERS_ENDPOINT, queryStr,
        new TypeReference<List<OrderStatus>>() {
        }
    );
  }

  @Override
  public CompletionStage<Account> getAccount(Keys keys) {

    String queryStr = String.format(
        "timestamp=%d", System.currentTimeMillis());

    return makeSignedRequest(keys, HttpMethod.GET, ACCOUNT_ENDPOINT, queryStr,
        new TypeReference<Account>() {
        }
    );
  }

  private <T> CompletionStage<T> makeSignedRequest(Keys keys, HttpMethod method, String endpoint,
      String queryStr, TypeReference<T> typeReference) {

    return CompletableFuture.supplyAsync(() -> {
      try {
        String signature = signQueryString(keys, queryStr);
        String url = API_V3_URL + endpoint + "?" + queryStr + "&signature=" + signature;
        BoundRequestBuilder boundRequestBuilder;
        if (method == HttpMethod.GET) {
          boundRequestBuilder = asyncHttpClient.prepareGet(url);
        } else if (method == HttpMethod.POST) {
          boundRequestBuilder = asyncHttpClient.preparePost(url);
        } else if (method == HttpMethod.DELETE) {
          boundRequestBuilder = asyncHttpClient.prepareDelete(url);
        } else {
          throw new IllegalArgumentException("Unsupported method: " + method);
        }
        return handleResponse(boundRequestBuilder
            .addHeader(
                APIKEY_HEADER, keys.getApiKey())
            .execute().toCompletableFuture(), typeReference);
      } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
        throw new CompletionException(e);
      }
    }).thenCompose(Function.identity());
  }

  private String signQueryString(Keys keys, String queryString)
      throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {

    byte[] byteKey = keys.getSecretKey().getBytes("UTF-8");
    Mac sha256HMAC = Mac.getInstance(HMAC_SHA256);
    sha256HMAC.init(new SecretKeySpec(byteKey, HMAC_SHA256));
    return bytesToHex(sha256HMAC.doFinal(queryString.getBytes("UTF-8")));
  }

  private <T> CompletionStage<T> handleResponse(CompletableFuture<Response> responseFuture,
      TypeReference<T> typeReference) {

    return responseFuture.thenApply(response -> {
      try {
        if (response.getStatusCode() >= 400) {
          throw new ClientErrorException(response.getStatusCode(),
              objectMapper.readValue(response.getResponseBody(), ErrorDetails.class));
        }
        return objectMapper.readValue(response.getResponseBody(), typeReference);
      } catch (IOException e) {
        throw new CompletionException(e);
      }
    });
  }

  final private static char[] hexArray = "0123456789ABCDEF"
      .toCharArray();

  private String bytesToHex(byte[] bytes) {

    char[] hexChars = new char[bytes.length * 2];
    int v;
    for (int j = 0; j < bytes.length; j++) {
      v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }
}
