package com.github.johnsiu.binance.exceptions;

import com.github.johnsiu.binance.models.ErrorDetails;

/**
 * Exception thrown when Binance API returned an HTTP 4xx client error.
 */
public class ClientErrorException extends RuntimeException {

  private int httpStatusCode;
  // error message from Binance API.
  private ErrorDetails errorDetails;

  public ClientErrorException(int httpStatusCode, ErrorDetails errorDetails) {
    super(errorDetails.getMsg());
    this.httpStatusCode = httpStatusCode;
    this.errorDetails = errorDetails;
  }

  public int getHttpStatusCode() {
    return httpStatusCode;
  }

  public ErrorDetails getErrorDetails() {
    return errorDetails;
  }
}
