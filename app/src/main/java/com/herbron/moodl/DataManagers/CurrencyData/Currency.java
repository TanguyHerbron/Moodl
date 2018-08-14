package com.herbron.moodl.DataManagers.CurrencyData;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.herbron.moodl.CurrencyInfoUpdateNotifierInterface;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tiji on 25/12/2017.
 */

public class Currency implements Parcelable {

    private int id;
    private int tickerId;
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
    private double maxCoinSupply;
    private double minedCoinSupply;
    private String description;
    private String algorithm;
    private String proofType;
    private int totalSupply;
    private double marketCapitalization;

    private double volume24h;
    private double dominance;
    private int rank;
    private String startDate;
    private List<String> socialMediaLinks;
    private OnTimestampPriceUpdatedListener onTimestampPriceUpdatedListener;
    //private String proofType

    private CurrencyInfoUpdateNotifierInterface currencyInfoUpdateNotifierInterface;

    public Currency() {}

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

    public Currency(String name, String symbol, int tickerId)
    {
        this.name = name;
        this.symbol = symbol;
        this.tickerId = tickerId;
    }

    public void setListener(CurrencyInfoUpdateNotifierInterface currencyInfoUpdateNotifierInterface)
    {
        this.currencyInfoUpdateNotifierInterface = currencyInfoUpdateNotifierInterface;
    }

    //public Currency(int id, String symbol, String name, String algorithm, String proofType, )

    public void getTimestampPrice(android.content.Context context, String toSymbol, long timestamp)
    {
        dataRetriver = new CurrencyDataRetriever(context);

        dataRetriver.getPriceTimestamp(symbol, toSymbol, new CurrencyDataRetriever.DataChartCallBack() {
            @Override
            public void onSuccess(List<CurrencyDataChart> dataChart) {}

            @Override
            public void onSuccess(String price) {
                currencyInfoUpdateNotifierInterface.onTimestampPriceUpdated(price);

                if(onTimestampPriceUpdatedListener != null)
                {
                    onTimestampPriceUpdatedListener.onTimeStampPriceUpdated(price);
                }
            }
        }, timestamp);
    }

    public void updatePrice(android.content.Context context, String toSymbol, final CurrencyInfoUpdateNotifierInterface callBack)
    {
        dataRetriver = new CurrencyDataRetriever(context);

        dataRetriver.updatePrice(symbol, toSymbol, new CurrencyDataRetriever.CurrencyCallBack() {
            @Override
            public void onSuccess(Currency currencyInfo) {
                if(currencyInfo != null)
                {
                    setValue(currencyInfo.getValue());
                    setDayFluctuation(currencyInfo.getDayFluctuation());
                    setDayFluctuationPercentage(currencyInfo.getDayFluctuationPercentage());
                }

                callBack.onPriceUpdated(currencyInfo);
            }
        });
    }

    public void updateHistoryMinutes(android.content.Context context, String toSymbol)
    {
        dataRetriver = new CurrencyDataRetriever(context);

        dataRetriver.updateHistory(symbol, toSymbol, new CurrencyDataRetriever.DataChartCallBack() {
            @Override
            public void onSuccess(List<CurrencyDataChart> dataChart) {
                setHistoryMinutes(dataChart);

                currencyInfoUpdateNotifierInterface.onHistoryDataUpdated();
            }

            @Override
            public void onSuccess(String result){}
        }, CurrencyDataRetriever.MINUTES);
    }

    private void mergeWith(Currency currency)
    {
        dataRetriver = currency.dataRetriver;
        maxCoinSupply = currency.maxCoinSupply;
        minedCoinSupply = currency.minedCoinSupply;
        description = currency.description;
        algorithm = currency.algorithm;
        proofType = currency.proofType;
        totalSupply = currency.totalSupply;
        marketCapitalization = currency.marketCapitalization;
        socialMediaLinks = currency.socialMediaLinks;
    }

    public void updateSnapshot(android.content.Context context, final CurrencyCallBack callBack)
    {
        dataRetriver = new CurrencyDataRetriever(context);
        dataRetriver.updateSnapshot(id, new CurrencyDataRetriever.CurrencyCallBack() {
            @Override
            public void onSuccess(Currency currencyInfo) {
                //Currency.this.mergeWith(currencyInfo);

                Currency.this.proofType = currencyInfo.proofType;
                Currency.this.algorithm = currencyInfo.algorithm;
                Currency.this.description = currencyInfo.description;
                Currency.this.maxCoinSupply = currencyInfo.maxCoinSupply;
                Currency.this.minedCoinSupply = currencyInfo.minedCoinSupply;
                Currency.this.startDate = currencyInfo.startDate;

                callBack.onSuccess(Currency.this);
            }
        });
    }

    public void updateTicker(android.content.Context context, String toSymbol, final CurrencyCallBack callBack)
    {
        dataRetriver = new CurrencyDataRetriever(context);
        dataRetriver.updateTickerInfos(tickerId, toSymbol, new CurrencyDataRetriever.CurrencyCallBack() {
            @Override
            public void onSuccess(Currency currencyInfo) {
                //Currency.this.mergeWith(currencyInfo);

                Currency.this.marketCapitalization = currencyInfo.marketCapitalization;
                Currency.this.rank = currencyInfo.rank;

                callBack.onSuccess(Currency.this);
            }
        });
    }

    public void updateHistoryHours(android.content.Context context, String toSymbol)
    {
        dataRetriver = new CurrencyDataRetriever(context);
        dataRetriver.updateHistory(symbol, toSymbol, new CurrencyDataRetriever.DataChartCallBack() {
            @Override
            public void onSuccess(List<CurrencyDataChart> dataChart) {
                setHistoryHours(dataChart);

                currencyInfoUpdateNotifierInterface.onHistoryDataUpdated();
            }

            @Override
            public void onSuccess(String price) {}
        }, CurrencyDataRetriever.HOURS);
    }

    public void updateHistoryDays(android.content.Context context, String toSymbol)
    {
        dataRetriver = new CurrencyDataRetriever(context);
        dataRetriver.updateHistory(symbol, toSymbol, new CurrencyDataRetriever.DataChartCallBack() {
            @Override
            public void onSuccess(List<CurrencyDataChart> dataChart) {
                setHistoryDays(dataChart);

                currencyInfoUpdateNotifierInterface.onHistoryDataUpdated();
            }

            @Override
            public void onSuccess(String price) {}
        }, CurrencyDataRetriever.DAYS);
    }

    public void updateDetails(android.content.Context context, final CurrencyCallBack callBack)
    {
            dataRetriver = new CurrencyDataRetriever(context);

    }

    private int getDarkenColor(int color)
    {
        int transColor;
        int alpha = Color.alpha(color);
        int r = Math.round(Color.red(color) * 0.8f);
        int g = Math.round(Color.green(color) * 0.8f);
        int b = Math.round(Color.blue(color) * 0.8f);

        transColor = Color.argb(alpha, r, g, b);

        return transColor ;
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
        double lightness = 1 - (0.299 * Color.red(chartColor) + 0.587 * Color.green(chartColor) + 0.114 * Color.blue(chartColor)) / 255;

        if(lightness < 0.1)
        {
            chartColor = getDarkenColor(chartColor);
        }

        this.chartColor = chartColor;
    }

    public double getVolume24h() {
        return volume24h;
    }

    public void setVolume24h(double volume24h) {
        this.volume24h = volume24h;
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

    public void setDayFluctuationPercentage(float dayFluctuationPercentage) {
        this.dayFluctuationPercentage = dayFluctuationPercentage;
    }

    public void setDayFluctuation(double dayFluctuation) {
        this.dayFluctuation = dayFluctuation;
    }

    public void setIcon(Bitmap newIcon)
    {
        icon = newIcon;
    }

    public Bitmap getIcon()
    {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getMaxCoinSupply() {
        return maxCoinSupply;
    }

    public void setMaxCoinSupply(double maxCoinSupply) {
        this.maxCoinSupply = maxCoinSupply;
    }

    public double getMinedCoinSupply() {
        return minedCoinSupply;
    }

    public void setMinedCoinSupply(double minedCoinSupply) {
        this.minedCoinSupply = minedCoinSupply;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getProofType() {
        return proofType;
    }

    public void setProofType(String proofType) {
        this.proofType = proofType;
    }

    public double getMarketCapitalization() {
        return marketCapitalization;
    }

    public void setMarketCapitalization(double marketCapitalization) {
        this.marketCapitalization = marketCapitalization;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getTickerId() {
        return tickerId;
    }

    public void setTickerId(int tickerId) {
        this.tickerId = tickerId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public float getDominance(float totalMarketCapitalization)
    {
        return (float) (marketCapitalization / totalMarketCapitalization) * 100;
    }

    private void updateDayFluctuation()
    {
        if(historyMinutes != null)
        {
            dayFluctuation = historyMinutes.get(historyMinutes.size() - 1).getOpen() - historyMinutes.get(0).getOpen();

            dayFluctuationPercentage = (float) (dayFluctuation / historyMinutes.get(0).getOpen() * 100);
        }
    }

    @Override
    public String toString()
    {
        Field[] fields = this.getClass().getDeclaredFields();
        String currencyString = "Currency >";

        for(Field field : fields)
        {
            currencyString += "\n\t";

            try {
                currencyString += field.getName();
                currencyString += ": ";
                currencyString += field.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return currencyString;
    }

    public interface CurrencyCallBack {
        void onSuccess(Currency currency);
    }

    public interface PriceCallBack {
        void onSuccess(String price);
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
        dest.writeInt(this.tickerId);
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
        this.tickerId = in.readInt();
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

    public interface OnTimestampPriceUpdatedListener
    {
        void onTimeStampPriceUpdated(String price);
    }

    public void setOnTimestampPriceUpdatedListener(OnTimestampPriceUpdatedListener onTimestampPriceUpdatedListener)
    {
        this.onTimestampPriceUpdatedListener = onTimestampPriceUpdatedListener;
    }
}
