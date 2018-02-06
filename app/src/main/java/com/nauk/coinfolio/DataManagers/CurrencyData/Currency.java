package com.nauk.coinfolio.DataManagers.CurrencyData;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static java.sql.Types.NULL;

/**
 * Created by Tiji on 25/12/2017.
 */

public class Currency implements Parcelable {

    private int id;
    private String name;
    private String symbol;
    private double value;
    private double balance;
    private float dayFluctuationPercentage;
    private double dayFluctuation;
    private List<CurrencyDataChart> historyMinutes;
    private List<CurrencyDataChart> historyHours;
    private List<CurrencyDataChart> historyDays;
    private CurrencyDataRetriever dataRetriver;
    private Bitmap icon;
    private int chartColor;

    public Currency(Currency currency)
    {
        this.id = currency.id;
        this.name = currency.name;
        this.symbol = currency.symbol;
        this.value = currency.value;
        this.balance = currency.balance;
        this.dayFluctuationPercentage = currency.getDayFluctuationPercentage();
        this.dayFluctuation = currency.getDayFluctuation();
        this.historyMinutes = currency.historyMinutes;
        this.dataRetriver = currency.getDataRetriver();
        this.icon = currency.icon;
        this.chartColor = currency.chartColor;
    }

    public Currency(String symbol, double balance)
    {
        this.symbol = symbol;
        this.balance = balance;
    }

    public Currency(String symbol, String name, double balance)
    {
        this.symbol = symbol;
        this.name = name;
        this.balance = balance;
    }

    public Currency(String name, String symbol)
    {
        this.name = name;
        this.symbol = symbol;
    }

    public void updateHistoryMinutes(android.content.Context context, final CurrencyCallBack callBack)
    {
        dataRetriver = new CurrencyDataRetriever(context);
        dataRetriver.updateHistory(symbol, new CurrencyDataRetriever.DataChartCallBack() {
            @Override
            public void onSuccess(List<CurrencyDataChart> dataChart) {
                setHistoryMinutes(dataChart);
                updateDayFluctuation();

                if(dataChart != null)
                {
                    setValue(dataChart.get(dataChart.size() - 1).getClose());
                }
                else
                {
                    value = NULL;
                }

                callBack.onSuccess(Currency.this);
            }
        }, CurrencyDataRetriever.MINUTES);
    }

    public void updateHistoryHours(android.content.Context context, final CurrencyCallBack callBack)
    {
        dataRetriver = new CurrencyDataRetriever(context);
        dataRetriver.updateHistory(symbol, new CurrencyDataRetriever.DataChartCallBack() {
            @Override
            public void onSuccess(List<CurrencyDataChart> dataChart) {
                setHistoryHours(dataChart);

                callBack.onSuccess(Currency.this);
            }
        }, CurrencyDataRetriever.HOURS);
    }

    public void updateHistoryDays(android.content.Context context, final CurrencyCallBack callBack)
    {
        dataRetriver = new CurrencyDataRetriever(context);
        dataRetriver.updateHistory(symbol, new CurrencyDataRetriever.DataChartCallBack() {
            @Override
            public void onSuccess(List<CurrencyDataChart> dataChart) {
                setHistoryDays(dataChart);

                callBack.onSuccess(Currency.this);
            }
        }, CurrencyDataRetriever.DAYS);
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }

    public void setChartColor(int chartColor)
    {
        this.chartColor = chartColor;
    }

    public int getChartColor()
    {
        return chartColor;
    }

    public CurrencyDataRetriever getDataRetriver()
    {
        return dataRetriver;
    }

    public List<CurrencyDataChart> getHistoryMinutes()
    {
        return historyMinutes;
    }

    public List<CurrencyDataChart> getHistoryHours()
    {
        return historyHours;
    }

    public List<CurrencyDataChart> getHistoryDays()
    {
        return historyDays;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String newName)
    {
        name = newName;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public double getValue()
    {
        return value;
    }

    public void setValue(double newValue)
    {
        value = newValue;
    }

    public double getBalance()
    {
        return balance;
    }

    public float getDayFluctuationPercentage()
    {
        return  dayFluctuationPercentage;
    }

    public double getDayFluctuation()
    {
        return dayFluctuation;
    }

    public void setBalance(double newBalance)
    {
        balance = newBalance;
    }

    private void setHistoryMinutes(List<CurrencyDataChart> newDataChart)
    {
        historyMinutes = newDataChart;
    }

    private void setHistoryHours(List<CurrencyDataChart> newDataChart)
    {
        historyHours = newDataChart;
    }

    private void setHistoryDays(List<CurrencyDataChart> newDataChart)
    {
        historyDays = newDataChart;
    }

    public void setIcon(Bitmap newIcon)
    {
        icon = newIcon;
    }

    public Bitmap getIcon()
    {
        return icon;
    }

    private void updateDayFluctuation()
    {
        if(historyMinutes != null)
        {
            dayFluctuation = historyMinutes.get(historyMinutes.size() - 1).getOpen() - historyMinutes.get(0).getOpen();

            dayFluctuationPercentage = (float) (dayFluctuation / historyMinutes.get(0).getOpen() * 100);
        }
    }

    public interface CurrencyCallBack {
        void onSuccess(Currency currency);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.symbol);
        dest.writeDouble(this.value);
        dest.writeDouble(this.balance);
        dest.writeFloat(this.dayFluctuationPercentage);
        dest.writeDouble(this.dayFluctuation);
        dest.writeList(this.historyMinutes);
        dest.writeParcelable(this.icon, flags);
        dest.writeInt(this.chartColor);
    }

    protected Currency(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.symbol = in.readString();
        this.value = in.readDouble();
        this.balance = in.readDouble();
        this.dayFluctuationPercentage = in.readFloat();
        this.dayFluctuation = in.readDouble();
        this.historyMinutes = new ArrayList<CurrencyDataChart>();
        in.readList(this.historyMinutes, CurrencyDataChart.class.getClassLoader());
        this.icon = in.readParcelable(Bitmap.class.getClassLoader());
        this.chartColor = in.readInt();
    }

    public static final Parcelable.Creator<Currency> CREATOR = new Parcelable.Creator<Currency>() {
        @Override
        public Currency createFromParcel(Parcel source) {
            return new Currency(source);
        }

        @Override
        public Currency[] newArray(int size) {
            return new Currency[size];
        }
    };
}
