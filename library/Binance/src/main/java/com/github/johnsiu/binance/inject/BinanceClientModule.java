package com.github.johnsiu.binance.inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;

/**
 * Guice module for dependency injection.
 */
public class BinanceClientModule extends AbstractModule {

  public static final String BINANCE_CLIENT = "BinanceClient";

  @Override
  protected void configure() {
    ObjectMapper objectMapper = new ObjectMapper();
    bind(ObjectMapper.class)
        .annotatedWith(Names.named(BINANCE_CLIENT))
        .toInstance(objectMapper);
    bind(AsyncHttpClient.class).to(DefaultAsyncHttpClient.class);
  }
}
