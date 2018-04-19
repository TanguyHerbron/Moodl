package com.nauk.coinfolio.DataManagers;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by Guitoune on 02/03/2018.
 */

public class MarketCapManager {

    private static final String topCurrenciesUrl = "https://api.coinmarketcap.com/v1/ticker/?limit=9&convert=";
    private static final String marketCapUrl = "https://api.coinmarketcap.com/v1/global/?convert=";
    private RequestQueue requestQueue;
    private String topRequestResult[];
    private long marketCap;
    private long dayVolume;

    public MarketCapManager(android.content.Context context)
    {
        requestQueue = Volley.newRequestQueue(context);
    }

    public void updateTopCurrencies(final VolleyCallBack callBack, final String toSymbol)
    {
        String requestString = topCurrenciesUrl + toSymbol;

        StringRequest strRequest = new StringRequest(Request.Method.GET, requestString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.length() > 0) {
                            processTopCurrencies(response, toSymbol);
                        }

                        callBack.onSuccess();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        requestQueue.add(strRequest);
    }

    public void updateMarketCap(final VolleyCallBack callBack, final String toSymbol)
    {
        String requestString = marketCapUrl + toSymbol;

        StringRequest strRequest = new StringRequest(Request.Method.GET, requestString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.length() > 0) {
                            processMarketCapData(response, toSymbol);
                        }

                        callBack.onSuccess();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        requestQueue.add(strRequest);
    }

    private void processMarketCapData(String response, String toSymbol)
    {
        try {
            JSONObject jsonObject = new JSONObject(response);

            marketCap = new BigDecimal(jsonObject.getString("total_market_cap_" + toSymbol.toLowerCase())).longValue();

            dayVolume = new BigDecimal(jsonObject.getString("total_24h_volume_" + toSymbol.toLowerCase())).longValue();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Float> getDominance(String toSymbol)
    {
        HashMap<String, Float> dominance = new HashMap<>();

        for(int i = 0; i < topRequestResult.length; i++)
        {
            try {
                JSONObject jsonObject = new JSONObject(topRequestResult[i]);

                dominance.put(jsonObject.getString("symbol"), (Float.parseFloat(jsonObject.getString("market_cap_" + toSymbol.toLowerCase())) / marketCap)*100);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return dominance;
    }

    public long getDayVolume()
    {
        return dayVolume;
    }

    private void processTopCurrencies(String response, String toSymbol)
    {
        response = response.substring(response.indexOf('[')+1, response.lastIndexOf(']'));

        topRequestResult = response.split(Pattern.quote("},"));

        for(int i = 0; i < topRequestResult.length; i++)
        {
            topRequestResult[i] += "}";
            /*try {

                JSONObject jsonObject = new JSONObject(topRequestResult[i]);

                //Log.d("coinfolio", "Symbol : " + jsonObject.getString("symbol") + " " + jsonObject.getString("rank"));

            } catch (JSONException e) {
                e.printStackTrace();
            }*/
        }
    }

    public long getMarketCap()
    {
        return marketCap;
    }

    public interface VolleyCallBack
    {
        void onSuccess();
    }
}
