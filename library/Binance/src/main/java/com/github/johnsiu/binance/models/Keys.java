package com.github.johnsiu.binance.models;

/**
 * Represents API key and secret key for accessing SIGNED endpoints.
 */
public class Keys {

  private String apiKey;
  private String secretKey;

  public Keys(String apiKey, String secretKey) {
    this.apiKey = apiKey;
    this.secretKey = secretKey;
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getSecretKey() {
    return secretKey;
  }
}
