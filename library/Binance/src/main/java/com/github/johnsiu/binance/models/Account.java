package com.github.johnsiu.binance.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;
import com.github.johnsiu.binance.deser.BalanceMapDeserializer;

/**
 * Represents an account info.
 */
public class Account {

  private int makerCommission;
  private int takerCommission;
  private int buyerCommission;
  private int sellerCommission;
  private boolean canTrade;
  private boolean canWithdraw;
  private boolean canDeposit;
  // asset to Balance
  private Map<String, Balance> balances;

  public int getMakerCommission() {
    return makerCommission;
  }

  public int getTakerCommission() {
    return takerCommission;
  }

  public int getBuyerCommission() {
    return buyerCommission;
  }

  public int getSellerCommission() {
    return sellerCommission;
  }

  public boolean isCanTrade() {
    return canTrade;
  }

  public boolean isCanWithdraw() {
    return canWithdraw;
  }

  public boolean isCanDeposit() {
    return canDeposit;
  }

  @JsonDeserialize(using = BalanceMapDeserializer.class)
  public Map<String, Balance> getBalances() {
    return balances;
  }

  @Override
  public String toString() {
    return "Account{" +
        "makerCommission=" + makerCommission +
        ", takerCommission=" + takerCommission +
        ", buyerCommission=" + buyerCommission +
        ", sellerCommission=" + sellerCommission +
        ", canTrade=" + canTrade +
        ", canWithdraw=" + canWithdraw +
        ", canDeposit=" + canDeposit +
        ", balances=" + balances +
        '}';
  }

  public static class Balance {

    private String asset;
    private double free;
    private double locked;

    public String getAsset() {
      return asset;
    }

    public double getFree() {
      return free;
    }

    public double getLocked() {
      return locked;
    }

    @Override
    public String toString() {
      return "Balance{" +
          "asset='" + asset + '\'' +
          ", free=" + free +
          ", locked=" + locked +
          '}';
    }
  }

}

