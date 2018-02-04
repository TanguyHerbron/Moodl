package com.nauk.coinfolio.DataManagers.CurrencyData;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nauk.coinfolio.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Tiji on 05/01/2018.
 */

public class CurrencyDataRetriver  {

    String minuteHistoryUrl = "https://min-api.cryptocompare.com/data/histominute";
    String hourHistoryUrl = "https://min-api.cryptocompare.com/data/histohour";
    String dayHistoryUrl = "https://min-api.cryptocompare.com/data/histoday";
    String nameUrl = "https://api.hitbtc.com/api/2/public/currency/";

    RequestQueue requestQueue;

    android.content.Context context;

    public CurrencyDataRetriver(android.content.Context context)
    {
        this.context = context;

        requestQueue = Volley.newRequestQueue(context);
    }

    public void updateLastHourHistory(String symbolCurrencyFrom, String symbolCyrrencyTo, final DataChartCallBack callBack)
    {
        final String requestUrl = minuteHistoryUrl + "?fsym=" + symbolCurrencyFrom + "&tsym=" + symbolCyrrencyTo + "&limit=60";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callBack.onSuccess(processHourResult(response));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        requestQueue.add(stringRequest);
    }

    public void updateCurrencyName(String symbol, final NameCallBack callBack)
    {
        final String requestUrl = nameUrl + symbol;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        response = response.substring(response.indexOf(",") + 13);
                        response = response.substring(0, response.indexOf(",") - 1);

                        callBack.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.onSuccess(null);
                    }
                });

        requestQueue.add(stringRequest);
    }

    public void updateLastDayHistory(String symbolCurrencyFrom, String symbolCyrrencyTo, final DataChartCallBack callBack)
    {
        final String requestUrl = minuteHistoryUrl + "?fsym=" + symbolCurrencyFrom + "&tsym=" + symbolCyrrencyTo + "&limit=1440";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callBack.onSuccess(processHourResult(response));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.onSuccess(null);
                    }
                });

        requestQueue.add(stringRequest);
    }

    private List<CurrencyDataChart> processHourResult(String response)
    {
        List<CurrencyDataChart> dataChart = new ArrayList<>();

        if(response.length() > 200)
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

                    long timestamp = Long.parseLong(jsonObject.getString("time"));
                    double close = Double.parseDouble(jsonObject.getString("close"));
                    double high = Double.parseDouble(jsonObject.getString("high"));
                    double low = Double.parseDouble(jsonObject.getString("low"));
                    double open = Double.parseDouble(jsonObject.getString("open"));

                    dataChart.add(new CurrencyDataChart(timestamp, close, high, low, open));

                } catch (JSONException e) {
                    Log.d(context.getResources().getString(R.string.debug_volley), "API Request error: " + e + " index: " + i);
                }
            }
        }
        else
        {
            /*for(int i = 1; i <= 1440; i++)
            {
                dataChart.add(new CurrencyDataChart(i, 1, 1, 1, 1));
            }*/

            dataChart = null;
        }

        return dataChart;
    }

    public void updateLastHourHistory(String symbolCurrencyFrom, final DataChartCallBack callBack)
    {
        updateLastHourHistory(symbolCurrencyFrom, "USD", callBack);
    }

    public void updateLastDayHistory(String symbolCurrencyFrom, final DataChartCallBack callBack)
    {
        if(!symbolCurrencyFrom.equals("USD"))
        {
            updateLastDayHistory(symbolCurrencyFrom, "USD", callBack);
        }
    }

    public interface DataChartCallBack {
        void onSuccess(List<CurrencyDataChart> dataChart);
    }

    public interface CurrencyDetailCallBack {
        void onSuccess();
    }

    public interface NameCallBack {
        void onSuccess(String name);
    }
}
