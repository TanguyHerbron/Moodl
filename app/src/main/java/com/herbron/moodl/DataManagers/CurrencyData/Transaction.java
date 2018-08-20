package com.herbron.moodl.DataManagers.CurrencyData;

/**
 * Created by Guitoune on 30/01/2018.
 */

public class Transaction {

    private int transactionId;
    private String symbol;
    private double amount;
    private long timestamp;
    private double price;
    private double fees;
    private String feeCurrency;
    private String feeFormat;
    private String note;
    private String symPair;
    private String source;
    private String destination;
    private String type;
    private boolean isDeducted;

    public Transaction(int transactionId, String symbol, String symPair, double amount, long timestamp, double purchasedPrice, double fees, String note, String feeCurrency, String source, String destination, String type, String feeFormat, boolean isDeducted)
    {
        this.transactionId = transactionId;
        this.symbol = symbol;
        this.symPair = symPair;
        this.amount = amount;
        this.timestamp = timestamp;
        this.price = purchasedPrice;
        this.fees = fees;
        this.note = note;
        this.feeCurrency = feeCurrency;
        this.source = source;
        this.destination = destination;
        this.type = type;
        this.feeFormat = feeFormat;
        this.isDeducted = isDeducted;
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

    public void setPrice(double purchasedPrice)
    {
        this.price = purchasedPrice;
    }

    public double getPrice()
    {
        return price;
    }

    public double getFees() {
        return fees;
    }

    public void setFees(double fees) {
        this.fees = fees;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getSymPair() {
        return symPair;
    }

    public void setSymPair(String symPair) {
        this.symPair = symPair;
    }

    public String getFeeCurrency() {
        return feeCurrency;
    }

    public void setFeeCurrency(String feeCurrency) {
        this.feeCurrency = feeCurrency;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFeeFormat() {
        return feeFormat;
    }

    public void setFeeFormat(String feeFormat) {
        this.feeFormat = feeFormat;
    }

    public boolean isDeducted() {
        return isDeducted;
    }

    public void setDeducted(boolean deducted) {
        isDeducted = deducted;
    }
}
