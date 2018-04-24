package com.nauk.moodl.DataManagers.CurrencyData;

/**
 * Created by Guitoune on 24/04/2018.
 */

public class Trade extends com.binance.api.client.domain.account.Trade {

    private String symbol;
    private String pairSymbol;

    public Trade(String symbol, String pairSymbol, com.binance.api.client.domain.account.Trade biTrade)
    {
        this.symbol = symbol;
        this.pairSymbol = pairSymbol;
        setId(biTrade.getId());
        setPrice(biTrade.getPrice());
        setQty(biTrade.getQty());
        setCommission(biTrade.getCommission());
        setCommissionAsset(biTrade.getCommissionAsset());
        setTime(biTrade.getTime());
        setBuyer(biTrade.isBuyer());
        setMaker(biTrade.isMaker());
        setBestMatch(biTrade.isBestMatch());
        setOrderId(biTrade.getOrderId());
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPairSymbol() {
        return pairSymbol;
    }

    public void setPairSymbol(String pairSymbol) {
        this.pairSymbol = pairSymbol;
    }
}
