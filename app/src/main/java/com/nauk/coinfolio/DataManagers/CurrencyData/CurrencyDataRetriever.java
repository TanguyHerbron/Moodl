package com.nauk.coinfolio.DataManagers.CurrencyData;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.nauk.coinfolio.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Tiji on 05/01/2018.
 */

public class CurrencyDataRetriever {

    final static int MINUTES = 0;
    final static int HOURS = 1;
    final static int DAYS = 2;

    private String minuteHistoryUrl = "https://min-api.cryptocompare.com/data/histominute";
    private String hourHistoryUrl = "https://min-api.cryptocompare.com/data/histohour";
    private String dayHistoryUrl = "https://min-api.cryptocompare.com/data/histoday";
    private String priceUrl = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=";
    private String snapshotUrl = "https://www.cryptocompare.com/api/data/coinsnapshotfullbyid/?id=";
    private String tickerUrl = "https://api.coinmarketcap.com/v1/ticker/";

    private RequestQueue requestQueue;

    protected android.content.Context context;

    CurrencyDataRetriever(android.content.Context context)
    {
        this.context = context;

        requestQueue = Volley.newRequestQueue(context);
    }

    public void updateTickerInfos(String currencyName, final String toSymbol, final CurrencyCallBack callBack)
    {
        final String requestUrl = tickerUrl + currencyName + "/?convert=" + toSymbol;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callBack.onSuccess(processTicker(response, toSymbol));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.onSuccess(new Currency());
                    }
                });

        requestQueue.add(stringRequest);
    }

    private Currency processTicker(String response, String toSymbol)
    {
        Currency currency = new Currency();

        response = response.substring(1, response.length()-1);

        try {
            JSONObject jsonObject = new JSONObject(response);

            currency.setMarketCapitalization(jsonObject.getDouble("market_cap_" + toSymbol.toLowerCase()));
            currency.setRank(jsonObject.getInt("rank"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return currency;
    }

    public void updateSnapshot(int id, final CurrencyCallBack callBack)
    {
        final String requestUrl = snapshotUrl + id;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callBack.onSuccess(processSnapshotResult(response));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.onSuccess(new Currency());
                    }
                });

        requestQueue.add(stringRequest);
    }

    public void getPriceTimestamp(final String symbolCurrencyFrom, String symbolCurrencyTo, final DataChartCallBack callBack, long timestamp)
    {
        final String requestUrl = "https://min-api.cryptocompare.com/data/pricehistorical?fsym=" + symbolCurrencyFrom + "&tsyms=" + symbolCurrencyTo + "&ts=" + timestamp;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callBack.onSuccess(processPriceTimestampResult(response));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        requestQueue.add(stringRequest);
    }

    private String processPriceTimestampResult(String result)
    {
        result = result.substring(result.lastIndexOf(':')+1);
        result = result.substring(0, result.indexOf('}'));

        return result;
    }

    public void updateHistory(final String symbolCurrencyFrom, String symbolCyrrencyTo, final DataChartCallBack callBack, int timeUnit)
    {
        String requestUrl = getRequestUrl(timeUnit, symbolCurrencyFrom, symbolCyrrencyTo);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callBack.onSuccess(processHistoryResult(response));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.onSuccess((List<CurrencyDataChart>) null);
                    }
                });

        requestQueue.add(stringRequest);
    }

    public void updatePrice(final String symbolCurrencyFrom, String symbolCurrencyTo, final CurrencyCallBack callBack)
    {
        String requestUrl = priceUrl + symbolCurrencyFrom + "&tsyms=" + symbolCurrencyTo;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callBack.onSuccess(processPriceResult(response));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        requestQueue.add(stringRequest);
    }

    private String getRequestUrl(int timeUnit, String symbolCurrencyFrom, String symbolCyrrencyTo)
    {
        String requestUrl = null;

        switch (timeUnit)
        {
            case MINUTES:
                requestUrl = minuteHistoryUrl + "?fsym=" + symbolCurrencyFrom + "&tsym=" + symbolCyrrencyTo + "&limit=1440";
                break;
            case HOURS:
                requestUrl = hourHistoryUrl + "?fsym=" + symbolCurrencyFrom + "&tsym=" + symbolCyrrencyTo + "&limit=744";
                break;
            case DAYS:
                requestUrl = dayHistoryUrl + "?fsym=" + symbolCurrencyFrom + "&tsym=" + symbolCyrrencyTo + "&limit=365";
                break;
        }

        return requestUrl;
    }

    private Currency processPriceResult(String response)
    {
        Currency currency = new Currency();

        if(response.length() > 500)
        {
            response = response.substring(response.indexOf("TYPE") - 2, response.length() - 3);

            try {
                JSONObject jsonObject = new JSONObject(response);
                double open24 = jsonObject.getDouble("OPEN24HOUR");
                double value = jsonObject.getDouble("PRICE");

                currency.setDayFluctuation(value - open24);
                currency.setDayFluctuationPercentage((float) (currency.getDayFluctuation() / open24 * 100));

                currency.setValue(value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return currency;
    }

    private Currency processSnapshotResult(String response)
    {
        Currency currency = new Currency();

        try {
            JSONObject jsonObject = new JSONObject(response);
            jsonObject = new JSONObject(jsonObject.getString("Data"));
            jsonObject = new JSONObject(jsonObject.getString("General"));

            currency.setProofType(jsonObject.getString("ProofType"));
            currency.setAlgorithm(jsonObject.getString("Algorithm"));
            currency.setDescription(jsonObject.getString("Description"));

            if(!jsonObject.getString("TotalCoinSupply").equals(""))
            {
                currency.setMaxCoinSupply(Double.parseDouble(jsonObject.getString("TotalCoinSupply")));
            }

            if(!jsonObject.getString("TotalCoinsMined").equals(""))
            {
                currency.setMinedCoinSupply(Double.parseDouble(jsonObject.getString("TotalCoinsMined")));
            }

            currency.setStartDate(jsonObject.getString("StartDate"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return currency;
    }

    private List<CurrencyDataChart> processHistoryResult(String response)
    {
        List<CurrencyDataChart> dataChart = new ArrayList<>();

        if(response.length() > 250)
        {
            response = response.substring(response.indexOf("Data\":[{") + 7, response.lastIndexOf("}],\"TimeTo"));
            String[] tab = response.split(Pattern.quote("},{"));
            for(int i = 0; i < tab.length; i++)
            {

                if(i == 0)
                {
                    tab[i] = tab[i] + "}";
                }
                else
                {
                    tab[i] = "{" + tab[i] + "}";
                }

                try {
                    JSONObject jsonObject = new JSONObject(tab[i]);

                    dataChart.add(parseJSON(jsonObject));

                } catch (JSONException e) {
                    Log.d(context.getResources().getString(R.string.debug_volley), "API Request error: " + e + " index: " + i);
                }
            }
        }
        else
        {
            dataChart = null;
        }

        return dataChart;
    }

    private CurrencyDataChart parseJSON(JSONObject jsonObject) throws JSONException {

        long timestamp = Long.parseLong(jsonObject.getString("time"));
        double close = Double.parseDouble(jsonObject.getString("close"));
        double high = Double.parseDouble(jsonObject.getString("high"));
        double low = Double.parseDouble(jsonObject.getString("low"));
        double open = Double.parseDouble(jsonObject.getString("open"));
        double volumeFrom = Double.parseDouble(jsonObject.getString("volumefrom"));
        double volumeTo = Double.parseDouble(jsonObject.getString("volumeto"));

        return new CurrencyDataChart(timestamp, close, high, low, open, volumeFrom, volumeTo);
    }

    public void getPriceTimestamp(String symbolCurrencyFrom, final DataChartCallBack callBack, long timestamp)
    {
        getPriceTimestamp(symbolCurrencyFrom, "USD", callBack, timestamp);
    }

    public void updateHistory(String symbolCurrencyFrom, final DataChartCallBack callBack, int timeUnit)
    {
        if(symbolCurrencyFrom.equals("USD"))
        {
            callBack.onSuccess((List<CurrencyDataChart>) null);
        }
        else
        {
            updateHistory(symbolCurrencyFrom, "USD", callBack, timeUnit);
        }
    }

    public void updatePrice(String symbolCurrencyFrom, final CurrencyCallBack callBack)
    {
        if(symbolCurrencyFrom.equals("USD"))
        {
            callBack.onSuccess(null);
        }
        else
        {
            updatePrice(symbolCurrencyFrom, "USD", callBack);
        }
    }

    /*public void updateCryptocompareDetails(int id, final Currency.CurrencyCallBack callBack)
    {
        String requestUrl = getRequestUrl(timeUnit, symbolCurrencyFrom, symbolCyrrencyTo);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callBack.onSuccess();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.onSuccess();
                    }
                });

        requestQueue.add(stringRequest);
    }*/

    public void updateCoinMarketCapDetails()
    {

    }

    public interface DataChartCallBack {
        void onSuccess(List<CurrencyDataChart> dataChart);
        void onSuccess(String price);
    }

    public interface CurrencyCallBack {
        void onSuccess(Currency currencyInfo);
    }
}
