package com.nauk.moodl.DataManagers.CurrencyData;

/**
 * Created by Guitoune on 30/01/2018.
 */

public class Transaction {

    private int transactionId;
    private String symbol;
    private double amount;
    private long timestamp;
    private double purchasedPrice;
    private double fees;
    private boolean isMined;

    public Transaction(int transactionId, String symbol, double amount, long timestamp, double purchasedPrice, double fees)
    {
        this.transactionId = transactionId;
        this.symbol = symbol;
        this.amount = amount;
        this.timestamp = timestamp;
        this.purchasedPrice = purchasedPrice;
        this.fees = fees;
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

    public long getTimestamp()
    {
        return timestamp;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setPurchasedPrice(double purchasedPrice)
    {
        this.purchasedPrice = purchasedPrice;
    }

    public double getPurchasedPrice()
    {
        return purchasedPrice;
    }

    public double getFees() {
        return fees;
    }

    public void setFees(double fees) {
        this.fees = fees;
    }
}
