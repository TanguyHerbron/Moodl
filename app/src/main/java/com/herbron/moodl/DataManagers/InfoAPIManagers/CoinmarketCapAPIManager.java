package com.herbron.moodl.DataManagers.InfoAPIManagers;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.herbron.moodl.DataNotifiers.CoinmarketcapNotifierInterface;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Guitoune on 22/04/2018.
 */

public class CoinmarketCapAPIManager {

    final private static String LISTINGURL = "https://api.coinmarketcap.com/v2/listings/";
    final private static String TICKERLISTURL1 = "https://api.coinmarketcap.com/v2/ticker/?start=";


    private static final String topCurrenciesUrl = "https://api.coinmarketcap.com/v2/ticker/?limit=9&convert=";
    private static final String marketCapUrl = "https://api.coinmarketcap.com/v2/global/?convert=";

    private RequestQueue requestQueue;
    private List<Currency> currencyTickerList;
    private static CoinmarketCapAPIManager INSTANCE;
    private boolean upToDate;

    private List<Currency> topCurrencies;
    private long marketCap;
    private long dayVolume;
    private String active_crypto;
    private String active_markets;

    private List<CoinmarketcapNotifierInterface> coinmarketcapNotifierInterfaceList;

    private CoinmarketCapAPIManager(Context context)
    {
        requestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized CoinmarketCapAPIManager getInstance(Context context)
    {
        if(INSTANCE == null)
        {
            INSTANCE = new CoinmarketCapAPIManager(context);
        }

        return INSTANCE;
    }

    public void addListener(CoinmarketcapNotifierInterface coinmarketcapNotifierInterface)
    {
        if(coinmarketcapNotifierInterfaceList == null)
        {
            coinmarketcapNotifierInterfaceList = new ArrayList<>();
        }

        coinmarketcapNotifierInterfaceList.add(coinmarketcapNotifierInterface);
    }

    public boolean isUpToDate()
    {
        if(currencyTickerList == null)
        {
            upToDate = false;
        }

        return upToDate;
    }

    public void getCurrenciesFrom(int indexFrom, final String toSymbol)
    {
        String requetsString = TICKERLISTURL1 + indexFrom + "&limit=50&convert=" + toSymbol;

        StringRequest strRequest = new StringRequest(Request.Method.GET, requetsString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.length() > 0)
                        {
                            processTickersResult(response, toSymbol);
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

    public void updateListing()
    {
        currencyTickerList = new ArrayList<>();
        StringRequest strRequest = new StringRequest(Request.Method.GET, LISTINGURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.length() > 0) {
                            processTickerListResult(response);
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

    private void processTickersResult(String response, String toSymbol)
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

        for(CoinmarketcapNotifierInterface coinmarketcapNotifierInterface : coinmarketcapNotifierInterfaceList)
        {
            coinmarketcapNotifierInterface.onCurrenciesRetrieved(currencyList);
        }
    }

    private void processTickerListResult(String response)
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

        for(CoinmarketcapNotifierInterface coinmarketcapNotifierInterface : coinmarketcapNotifierInterfaceList)
        {
            coinmarketcapNotifierInterface.onListingUpdated();
        }
    }

    public List<Currency> getTotalListing()
    {
        return currencyTickerList;
    }

    public void updateTopCurrencies(final String toSymbol)
    {
        String requestString = topCurrenciesUrl + toSymbol;

        topCurrencies = new ArrayList<>();

        StringRequest strRequest = new StringRequest(Request.Method.GET, requestString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.length() > 0) {
                            processTopCurrencies(response, toSymbol);
                        }

                        for(CoinmarketcapNotifierInterface coinmarketcapNotifierInterface : coinmarketcapNotifierInterfaceList)
                        {
                            coinmarketcapNotifierInterface.onTopCurrenciesUpdated();
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

    public void updateMarketCap(final String toSymbol)
    {
        String requestString = marketCapUrl + toSymbol;

        StringRequest strRequest = new StringRequest(Request.Method.GET, requestString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.length() > 0) {
                            processMarketCapData(response, toSymbol);
                        }


                        for(CoinmarketcapNotifierInterface coinmarketcapNotifierInterface : coinmarketcapNotifierInterfaceList)
                        {
                            coinmarketcapNotifierInterface.onMarketCapUpdated();
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

    private void processMarketCapData(String response, String toSymbol)
    {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject dataJsonObject = jsonObject.getJSONObject("data");
            JSONObject quotesJsonObject = dataJsonObject.getJSONObject("quotes");
            JSONObject valuesJsonObject = quotesJsonObject.getJSONObject(toSymbol);

            active_crypto = dataJsonObject.getString("active_cryptocurrencies");
            active_markets = dataJsonObject.getString("active_markets");
            marketCap = valuesJsonObject.getLong("total_market_cap");
            dayVolume = valuesJsonObject.getLong("total_volume_24h");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getActive_crypto()
    {
        return active_crypto;
    }

    public String getActive_markets()
    {
        return active_markets;
    }

    public List<Currency> getTopCurrencies()
    {
        return topCurrencies;
    }

    public long getDayVolume()
    {
        return dayVolume;
    }

    private void processTopCurrencies(String response, String toSymbol)
    {
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
                    JSONObject quoteJsonObject = subCurrencyJsonObject.getJSONObject("quotes");
                    JSONObject symJsonObject = quoteJsonObject.getJSONObject(toSymbol);
                    newCurrency.setMarketCapitalization(symJsonObject.getDouble("market_cap"));
                    newCurrency.setVolume24h(symJsonObject.getDouble("volume_24h"));

                    topCurrencies.add(newCurrency);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Currency getCurrencyFromSymbol(String symbol)
    {
        Currency returnedCurrency = null;
        int index = 0;

        do {
            if(symbol.equals(topCurrencies.get(index).getSymbol()))
            {
                returnedCurrency = topCurrencies.get(index);
            }

            index++;
        } while(index < topCurrencies.size() && returnedCurrency == null);


        return returnedCurrency;
    }

    public long getMarketCap()
    {
        return marketCap;
    }
}
