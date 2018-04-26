package com.nauk.moodl.DataManagers.CurrencyData;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nauk.moodl.DataManagers.BalanceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;

/**
 * Created by Guitoune on 22/04/2018.
 */

public class CurrencyTickerList {

    final private static String TICKERLISTURL = "https://api.coinmarketcap.com/v1/ticker/?limit=0";
    private RequestQueue requestQueue;
    private LinkedHashMap<String, String> coinTickersHashmap;
    private android.content.Context context;

    public CurrencyTickerList(android.content.Context context)
    {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    public void update(final BalanceManager.IconCallBack callBack)
    {
        coinTickersHashmap = new LinkedHashMap<>();
        StringRequest strRequest = new StringRequest(Request.Method.GET, TICKERLISTURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.length() > 0) {
                            processTickerListResult(response, callBack);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        requestQueue.add(strRequest);
    }

    public String getTickerIdForSymbol(String symbol)
    {
        String tickerId = null;
        try {
            JSONObject jsonObject = new JSONObject(coinTickersHashmap.get(symbol));
            tickerId = jsonObject.getString("id");
        } catch (JSONException | NullPointerException e) {
            switch (e.getMessage())
            {
                case "Attempt to invoke virtual method 'int java.lang.String.length()' on a null object reference":
                    Log.d("moodl", "Symbol " + symbol + " not supported");
                    break;
                default:
                    e.printStackTrace();
                    break;
            }
        }

        return tickerId;
    }

    public void processTickerListResult(String response, BalanceManager.IconCallBack callBack)
    {
        response = response.substring(1, response.length() - 1);
        String[] strTable = response.split(Pattern.quote("},"));

        for(int i = 0; i < strTable.length; i++)
        {
            strTable[i] += "}";
            try {
                JSONObject jsonObject = new JSONObject(strTable[i]);
                switch (jsonObject.getString("symbol"))
                {
                    case "MIOTA":
                        coinTickersHashmap.put("IOT", strTable[i]);
                        break;
                    case "NANO":
                        coinTickersHashmap.put("XRB", strTable[i]);
                        break;
                    default:
                        coinTickersHashmap.put(jsonObject.getString("symbol"), strTable[i]);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        callBack.onSuccess();
    }
}
