package com.nauk.coinfolio.DataManagers.CurrencyData;

/**
 * Created by Guitoune on 30/01/2018.
 */

public class Transaction {

    private int transactionId;
    private String symbol;
    private double amount;
    private double purchasedPrice;
    private boolean isMined;

    public Transaction(int transactionId, String symbol, double amount)
    {
        this.transactionId = transactionId;
        this.symbol = symbol;
        this.amount = amount;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
