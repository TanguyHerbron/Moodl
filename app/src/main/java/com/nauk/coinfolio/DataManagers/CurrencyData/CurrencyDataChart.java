package com.nauk.coinfolio.DataManagers.CurrencyData;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Tiji on 05/01/2018.
 */

public class CurrencyDataChart implements Parcelable {

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.timestamp);
        dest.writeDouble(this.close);
        dest.writeDouble(this.high);
        dest.writeDouble(this.low);
        dest.writeDouble(this.open);
    }

    protected CurrencyDataChart(Parcel in) {
        this.timestamp = in.readLong();
        this.close = in.readDouble();
        this.high = in.readDouble();
        this.low = in.readDouble();
        this.open = in.readDouble();
    }

    public static final Parcelable.Creator<CurrencyDataChart> CREATOR = new Parcelable.Creator<CurrencyDataChart>() {
        @Override
        public CurrencyDataChart createFromParcel(Parcel source) {
            return new CurrencyDataChart(source);
        }

        @Override
        public CurrencyDataChart[] newArray(int size) {
            return new CurrencyDataChart[size];
        }
    };
}
