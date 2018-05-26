package com.nauk.moodl.DataManagers.CurrencyData;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nauk.moodl.DataManagers.BalanceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Guitoune on 22/04/2018.
 */

public class CurrencyTickerList {

    final private static String TICKERLISTURL = "https://api.coinmarketcap.com/v2/listings/";
    private RequestQueue requestQueue;
    private List<Currency> currencyTickerList;
    private static CurrencyTickerList INSTANCE;
    private boolean upToDate;

    private CurrencyTickerList(Context context)
    {
        requestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized CurrencyTickerList getInstance(Context context)
    {
        if(INSTANCE == null)
        {
            INSTANCE = new CurrencyTickerList(context);
        }

        return INSTANCE;
    }

    public boolean isUpToDate()
    {
        return upToDate;
    }

    public void update(final BalanceManager.IconCallBack callBack)
    {
        currencyTickerList = new ArrayList<>();
        StringRequest strRequest = new StringRequest(Request.Method.GET, TICKERLISTURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.length() > 0) {
                            processTickerListResult(response, callBack);
                        }
                        upToDate = true;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        upToDate = true;
                    }
                });

        requestQueue.add(strRequest);
    }

    public int getTickerIdForSymbol(String symbol)
    {
        int tickerId = 0;
        int i = 0;

        while(!currencyTickerList.get(i).getSymbol().equals(symbol) && currencyTickerList.size() > i+1)
        {
            i++;
        }

        if(currencyTickerList.get(i).getSymbol().equals(symbol))
        {
            tickerId = currencyTickerList.get(i).getTickerId();
        }

        return tickerId;
    }

    public void processTickerListResult(String response, BalanceManager.IconCallBack callBack)
    {
        try {
            JSONObject dataJsonObject = new JSONObject(response);
            JSONArray dataJsonArray = dataJsonObject.getJSONArray("data");

            for(int i = 0; i < dataJsonArray.length(); i++)
            {
                JSONObject currencyJsonObject = dataJsonArray.getJSONObject(i);
                currencyTickerList.add(new Currency(currencyJsonObject.getString("name"), currencyJsonObject.getString("symbol"), currencyJsonObject.getInt("id")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*response = response.substring(16, response.length() - 2);
        String[] strTable = response.split(Pattern.quote("},"));

        for(int i = 0; i < strTable.length; i++)
        {
            strTable[i] += "}";
            Log.d("moodl", "TICKER " + i + " " + strTable[i]);
            try {
                JSONObject jsonObject = new JSONObject(strTable[i]);
                Log.d("moodl", "TICKER JSON " + i + " " + jsonObject);
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
        }*/

        callBack.onSuccess();
    }
}
