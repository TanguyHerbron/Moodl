package com.nauk.coinfolio.DataManagers.CurrencyData;

/**
 * Created by Tiji on 05/01/2018.
 */

public class CurrencyDataChart {

    long timestamp;
    double close;
    double high;
    double low;
    double open;

    public CurrencyDataChart(long timestamp, double close, double high, double low, double open)
    {
        this.timestamp = timestamp;
        this.close = close;
        this.high = high;
        this.low = low;
        this.open = open;
    }

    public double getOpen()
    {
        return open;
    }

    public double getClose()
    {
        return close;
    }

    public long getTimestamp()
    {
        return timestamp;
    }
}
