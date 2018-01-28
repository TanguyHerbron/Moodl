# Java client for [Binance API](https://www.binance.com/restapipub.html)

## Synopsis
Client for accessing Binance API using Java. An [async](src/main/java/com/github/johnsiu/binance/httpclients/AsyncBinanceClient.java) and a [sync](src/main/java/com/github/johnsiu/binance/httpclients/BinanceClient.java) versions of the client are available. 

## Installation

Add the bintray repo to the pom of your maven project: 

```xml
    <repositories>
        <repository>
          <id>bintray-johnsiu-maven-repo</id>
          <url>https://dl.bintray.com/johnsiu/maven-repo</url>
        </repository>
     </repositories>
 ```
then, add the dependency:
```xml
    <dependency>
      <groupId>com.github.johnsiu</groupId>
      <artifactId>binance-java-client</artifactId>
      <version>1.0.1</version>
    </dependency>
 ```

## Usage

### Creating an instance of the async client using Guice
```java
    Injector injector = Guice.createInjector(new BinanceClientModule());
    AsyncBinanceClient asyncClient = injector.getInstance(AsyncBinanceClient.class);
```
### Creating an instance of the sync client using Guice
```java
    Injector injector = Guice.createInjector(new BinanceClientModule());
    BinanceClient client = injector.getInstance(BinanceClient.class);
```

### Getting latest price of a symbol
```java
    Ticker ticker = client.getTicker("ETHBTC"));
    double price = ticker.getLastPrice();
```
### Getting depth of a symbol
```java
    Depth depth = client.getDepth("ETHBTC"));
```
### Placing a LIMIT order
```java
    Keys keys = new Keys("YOUR_API_KEY", "YOUR_SECRET_KEY");
    double quantity = 1;
    double price = 0.020041;
    Order order = client.placeLimitOrder(keys, "MCOETH", OrderSide.BUY, TimeInForce.GTC, quantity, price);
```

### Placing a MARKET order
```java
    double quantity = 1;
    Order order = client.placeMarketOrder(keys, "MCOETH", OrderSide.SELL, quantity);
```
### Checking an order’s status
```java
    OrderStatus orderStatus = client.checkOrderStatus(keys, order);
```
   
### Cancelling an order
```java
    CancelOrder cancelOrder = client.cancelOrder(keys, order);
    // or 
    CancelOrder cancelOrder = client.cancelOrder(keys, orderStatus);
    
```

### Getting a list of open orders
```java
    List<OrderStatus> openOrders = client.getOpenOrders(keys, "MCOETH");
```

### Getting a list of current position
```java
    Account account = client.getAccount(keys);
    Map<String, Balance> balances = account.getBalances();
```

### Exception handling
```java
    try {
        Depth depth = client.getDepth("invalid symbol"));
    } catch (ClientErrorException e) {
        int httpStatusCode =  e.getHttpStatusCode();
        String errorCode = e.getErrorDetails().getCode();
        String errorMessage = e.getErrorDetails().getMsg();
    }
```

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/johnsiu/binance-java-client/tags).

## License

This project is released into the public domain - see the [UNLICENSE](UNLICENSE) file for details.