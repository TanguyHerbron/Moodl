package com.herbron.moodl.DataManagers.CurrencyData;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.herbron.moodl.DataManagers.BalanceManager;

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

    final private static String DETAILURL = "https://min-api.cryptocompare.com/data/all/coinlist";
    private RequestQueue requestQueue;
    private LinkedHashMap<String, String> coinInfosHashmap;
    private static CurrencyDetailsList INSTANCE;
    private boolean upToDate;

    private CurrencyDetailsList(Context context)
    {
        requestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized CurrencyDetailsList getInstance(Context context)
    {
        if(INSTANCE == null)
        {
            INSTANCE = new CurrencyDetailsList(context);
        }

        return INSTANCE;
    }

    public void update(final BalanceManager.IconCallBack callBack)
    {
        StringRequest strRequest = new StringRequest(Request.Method.GET, DETAILURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response.length() > 0) {
                            processDetailResult(response, callBack);
                            upToDate = true;
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

    public boolean isUpToDate()
    {
        return upToDate;
    }

    private void processDetailResult(String response, final BalanceManager.IconCallBack callBack)
    {
        response = response.substring(response.indexOf("\"Data\"") + 7, response.lastIndexOf("},\"BaseImageUrl\""));
        String[] tab = response.split(Pattern.quote("},"));

        coinInfosHashmap = new LinkedHashMap<>();

        for(int i = 0; i < tab.length; i++)
        {
            tab[i] = tab[i].substring(tab[i].indexOf("\":{")+2, tab[i].length()) + "}";
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                JSONObject jsonObject = new JSONObject(tab[i]);

                switch (jsonObject.getString("Symbol"))
                {
                    case "IOT":
                        coinInfosHashmap.put("MIOTA", tab[i]);
                        break;
                    case "XRB":
                        coinInfosHashmap.put("NANO", tab[i]);
                        break;
                    default:
                        coinInfosHashmap.put(jsonObject.getString("Symbol"), tab[i]);
                        break;
                }
            } catch (JSONException e) {
                Log.d("moodl", "ImageUrl not found.");
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
