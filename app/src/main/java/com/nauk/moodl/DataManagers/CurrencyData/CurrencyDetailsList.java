package com.nauk.moodl.DataManagers.CurrencyData;

import android.os.StrictMode;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nauk.moodl.DataManagers.BalanceManager;
import com.nauk.moodl.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Tiji on 11/04/2018.
 */

public class CurrencyDetailsList {

    final private static String DETAILURL = "https://www.cryptocompare.com/api/data/coinlist/";
    private RequestQueue requestQueue;
    private LinkedHashMap<String, String> coinInfosHashmap;
    private android.content.Context context;

    public CurrencyDetailsList(android.content.Context context)
    {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    public void update(final BalanceManager.IconCallBack callBack)
    {
        StringRequest strRequest = new StringRequest(Request.Method.GET, DETAILURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.length() > 0) {
                            processDetailResult(response, callBack);
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


    private void processDetailResult(String response, final BalanceManager.IconCallBack callBack)
    {
        response = response.substring(response.indexOf("\"Data\"") + 7, response.lastIndexOf("},\"Type\":100}"));
        String[] tab = response.split(Pattern.quote("},"));

        coinInfosHashmap = new LinkedHashMap<>();

        for(int i = 0; i < tab.length; i++)
        {
            tab[i] = tab[i].substring(tab[i].indexOf("\":{")+2, tab[i].length()) + "}";
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                JSONObject jsonObject = new JSONObject(tab[i]);

                coinInfosHashmap.put(jsonObject.getString("Symbol"), tab[i]);
            } catch (JSONException e) {
                Log.d(context.getResources().getString(R.string.debug), "ImageUrl not found.");
            }
        }

        sortDetails();

        callBack.onSuccess();
    }

    private void sortDetails()
    {
        LinkedHashMap<String, String> sortedHashmap = new LinkedHashMap<>();
        List<String> listInfos = new ArrayList<>(coinInfosHashmap.values());
        List<String> listSymbols = new ArrayList<>(coinInfosHashmap.keySet());

        for(int i = 0; i < coinInfosHashmap.keySet().size(); i++)
        {

            try {
                JSONObject jsonObject = new JSONObject(listInfos.get(i));
                int index = jsonObject.getInt("SortOrder");

                listInfos.add(index, listInfos.get(i));
                listSymbols.add(index, listSymbols.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for(int i = 0; i < listInfos.size(); i++)
        {
            sortedHashmap.put(listSymbols.get(i), listInfos.get(i));
        }

        coinInfosHashmap = sortedHashmap;
    }

    public LinkedHashMap<String, String> getCoinInfosHashmap() {
        return coinInfosHashmap;
    }

    public List<String> getCurrenciesName()
    {
        List<String> currenciesName = new ArrayList<>();

        for (String symbol : coinInfosHashmap.keySet())
        {
            try {
                JSONObject jsonObject = new JSONObject(coinInfosHashmap.get(symbol));
                currenciesName.add(jsonObject.getString("CoinName"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return currenciesName;
    }

    public Currency getCurrencyDetailsFromSymbol(String symbol)
    {
        //Currency currency = new Currency();

        return null;
    }

    public List<String> getCurrenciesSymbol()
    {
        return new ArrayList<>(coinInfosHashmap.keySet());
    }
}
