package com.herbron.moodl.DataManagers.CurrencyData;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.herbron.moodl.Activities.HomeActivityFragments.Overview;
import com.herbron.moodl.DataManagers.BalanceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Guitoune on 22/04/2018.
 */

public class CurrencyTickerList {

    final private static String LISTINGURL = "https://api.coinmarketcap.com/v2/listings/";
    final private static String TICKERLISTURL1 = "https://api.coinmarketcap.com/v2/ticker/?start=";
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

    public void getCurrenciesFrom(int indexFrom, final String toSymbol, Overview.UpdateCallBack callBack)
    {
        String requetsString = TICKERLISTURL1 + indexFrom + "&limit=50&convert=" + toSymbol;

        StringRequest strRequest = new StringRequest(Request.Method.GET, requetsString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.length() > 0)
                        {
                            processTickersResult(response, toSymbol, callBack);
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

    public void updateListing(final BalanceManager.IconCallBack callBack)
    {
        currencyTickerList = new ArrayList<>();
        StringRequest strRequest = new StringRequest(Request.Method.GET, LISTINGURL,
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

    private void processTickersResult(String response, String toSymbol, Overview.UpdateCallBack callBack)
    {
        List<Currency> currencyList = new ArrayList<>();

        try {
            JSONObject masterJsonObject = new JSONObject(response);

            if(masterJsonObject.keys().hasNext())
            {
                JSONObject currencyJsonObject = masterJsonObject.getJSONObject(masterJsonObject.keys().next());
                Iterator<?> keys = currencyJsonObject.keys();

                while(keys.hasNext())
                {
                    String key = keys.next().toString();
                    JSONObject subCurrencyJsonObject = currencyJsonObject.getJSONObject(key);
                    Currency newCurrency = new Currency(subCurrencyJsonObject.getString("name"), subCurrencyJsonObject.getString("symbol"), subCurrencyJsonObject.getInt("id"));
                    newCurrency.setRank(subCurrencyJsonObject.getInt("rank"));
                    JSONObject quoteJsonObject = subCurrencyJsonObject.getJSONObject("quotes");
                    JSONObject symJsonObject = quoteJsonObject.getJSONObject(toSymbol);
                    newCurrency.setValue(symJsonObject.getDouble("price"));
                    newCurrency.setDayFluctuationPercentage((float) symJsonObject.getDouble("percent_change_24h"));
                    newCurrency.setDayFluctuation(newCurrency.getDayFluctuationPercentage() * newCurrency.getValue() / 100);

                    currencyList.add(newCurrency);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        callBack.onSuccess(currencyList);
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
