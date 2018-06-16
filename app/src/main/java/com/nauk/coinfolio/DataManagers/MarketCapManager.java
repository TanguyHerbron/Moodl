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

    private static final String topCurrenciesUrl = "https://api.coinmarketcap.com/v1/ticker/?limit=9";
    private static final String marketCapUrl = "https://api.coinmarketcap.com/v1/global/";
    private RequestQueue requestQueue;
    private String topRequestResult[];
    private long marketCap;
    private long dayVolume;

    public MarketCapManager(android.content.Context context)
    {
        requestQueue = Volley.newRequestQueue(context);
    }

    public void updateTopCurrencies(final VolleyCallBack callBack)
    {
        StringRequest strRequest = new StringRequest(Request.Method.GET, topCurrenciesUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.length() > 0) {
                            processTopCurrencies(response);
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

    public void updateMarketCap(final VolleyCallBack callBack)
    {
        StringRequest strRequest = new StringRequest(Request.Method.GET, marketCapUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.length() > 0) {
                            processMarketCapData(response);
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

    private void processMarketCapData(String response)
    {
        try {
            JSONObject jsonObject = new JSONObject(response);

            marketCap = new BigDecimal(jsonObject.getString("total_market_cap_usd")).longValue();

            dayVolume = new BigDecimal(jsonObject.getString("total_24h_volume_usd")).longValue();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Float> getDominance()
    {
        HashMap<String, Float> dominance = new HashMap<>();

        for(int i = 0; i < topRequestResult.length; i++)
        {
            try {
                JSONObject jsonObject = new JSONObject(topRequestResult[i]);

                dominance.put(jsonObject.getString("symbol"), (Float.parseFloat(jsonObject.getString("market_cap_usd")) / marketCap)*100);
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

    private void processTopCurrencies(String response)
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
