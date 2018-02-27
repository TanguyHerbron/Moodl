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

public class CurrencyDataRetriever {

    final static int MINUTES = 0;
    final static int HOURS = 1;
    final static int DAYS = 2;

    private String minuteHistoryUrl = "https://min-api.cryptocompare.com/data/histominute";
    private String hourHistoryUrl = "https://min-api.cryptocompare.com/data/histohour";
    private String dayHistoryUrl = "https://min-api.cryptocompare.com/data/histoday";

    private RequestQueue requestQueue;

    protected android.content.Context context;

    CurrencyDataRetriever(android.content.Context context)
    {
        this.context = context;

        requestQueue = Volley.newRequestQueue(context);
    }

    private void updateHistory(final String symbolCurrencyFrom, String symbolCyrrencyTo, final DataChartCallBack callBack, int timeUnit)
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
                        callBack.onSuccess(null);
                    }
                });

        requestQueue.add(stringRequest);
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
            dataChart = null;
        }

        return dataChart;
    }

    void updateHistory(String symbolCurrencyFrom, final DataChartCallBack callBack, int timeUnit)
    {
        if(symbolCurrencyFrom.equals("USD"))
        {
            callBack.onSuccess(null);
        }
        else
        {
            updateHistory(symbolCurrencyFrom, "USD", callBack, timeUnit);
        }
    }

    public interface DataChartCallBack {
        void onSuccess(List<CurrencyDataChart> dataChart);
    }
}
