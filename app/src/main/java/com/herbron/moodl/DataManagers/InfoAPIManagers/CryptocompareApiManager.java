package com.herbron.moodl.DataManagers.InfoAPIManagers;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.herbron.moodl.DataNotifiers.CryptocompareNotifierInterface;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.ExchangeManager.Exchange;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Tiji on 11/04/2018.
 */

public class CryptocompareApiManager {

    final private static String DETAILURL = "https://min-api.cryptocompare.com/data/all/coinlist";
    final private static String EXCHANGEURL = "https://min-api.cryptocompare.com/data/all/exchanges";
    private RequestQueue requestQueue;
    private LinkedHashMap<String, String> coinInfosHashmap;
    private List<Exchange> exchangeList;
    private static CryptocompareApiManager INSTANCE;
    private boolean exchangesUpToDate;
    private boolean detailsUpToDate;

    private List<CryptocompareNotifierInterface> cryptocompareNotifierInterfaceList;

    private CryptocompareApiManager(Context context)
    {
        requestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized CryptocompareApiManager getInstance(Context context)
    {
        if(INSTANCE == null)
        {
            INSTANCE = new CryptocompareApiManager(context);
        }

        return INSTANCE;
    }

    public void addListener(CryptocompareNotifierInterface cryptocompareNotifierInterface)
    {
        if(cryptocompareNotifierInterfaceList == null)
        {
            cryptocompareNotifierInterfaceList = new ArrayList<>();
        }

        cryptocompareNotifierInterfaceList.add(cryptocompareNotifierInterface);
    }

    public void updateExchangeList()
    {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EXCHANGEURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        processExchangeResult(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        requestQueue.add(stringRequest);
    }

    public void updateDetails()
    {
        StringRequest strRequest = new StringRequest(Request.Method.GET, DETAILURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response.length() > 0) {
                            processDetailResult(response);
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

    public boolean isExchangesUpToDate()
    {
        if(exchangeList == null)
        {
            exchangesUpToDate = false;
        }

        return exchangesUpToDate;
    }

    public boolean isDetailsUpToDate()
    {
        if(coinInfosHashmap == null)
        {
            detailsUpToDate = false;
        }

        return detailsUpToDate;
    }

    private void processExchangeResult(String response)
    {
        exchangeList = new ArrayList<>();

        try {
            JSONObject mainJsonObject = new JSONObject(response);
            Iterator<String> exchangeIterator = mainJsonObject.keys();

            while(exchangeIterator.hasNext())
            {
                String exchangeKey = exchangeIterator.next();
                JSONObject exchangeJsonObject = mainJsonObject.getJSONObject(exchangeKey);
                Iterator<String> pairIterator = exchangeJsonObject.keys();

                while(pairIterator.hasNext())
                {
                    String pairKey = pairIterator.next();
                    JSONArray pairJsonArray = exchangeJsonObject.getJSONArray(pairKey);

                    List<Pair> pairList = new ArrayList<>();

                    for(int i = 0; i < pairJsonArray.length(); i++)
                    {
                        pairList.add(new Pair(pairKey, pairJsonArray.get(i).toString()));
                    }

                    exchangeList.add(new Exchange(exchangeKey, pairList));
                }
            }

            for(CryptocompareNotifierInterface cryptocompareNotifierInterface : cryptocompareNotifierInterfaceList)
            {
                cryptocompareNotifierInterface.onExchangesUpdated();
            }

        } catch (JSONException e) {
            Log.d("moodl", "Error while processing exchange result");
        }
    }

    public List<Exchange> getExchangeList()
    {
        return exchangeList;
    }

    private void processDetailResult(String response)
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

        detailsUpToDate = true;

        for(CryptocompareNotifierInterface cryptocompareNotifierInterface : cryptocompareNotifierInterfaceList)
        {
            cryptocompareNotifierInterface.onDetailsUpdated();
        }
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

    public List<Currency> getCurrenciesDenomination()
    {
        List<Currency> currencies = new ArrayList<>();

        for(String symbol : coinInfosHashmap.keySet())
        {
            try {
                JSONObject jsonObject = new JSONObject(coinInfosHashmap.get(symbol));
                currencies.add(new Currency(jsonObject.getString("CoinName"), symbol));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return currencies;
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
