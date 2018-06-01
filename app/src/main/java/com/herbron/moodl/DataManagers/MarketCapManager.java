package com.herbron.moodl.DataManagers;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Guitoune on 02/03/2018.
 */

public class MarketCapManager {

    private static final String topCurrenciesUrl = "https://api.coinmarketcap.com/v2/ticker/?limit=9&convert=";
    private static final String marketCapUrl = "https://api.coinmarketcap.com/v2/global/?convert=";
    private RequestQueue requestQueue;
    private List<Currency> topCurrencies;
    private long marketCap;
    private long dayVolume;
    private String active_crypto;
    private String active_markets;

    public MarketCapManager(android.content.Context context)
    {
        requestQueue = Volley.newRequestQueue(context);
    }

    public void updateTopCurrencies(final VolleyCallBack callBack, final String toSymbol)
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

    public interface VolleyCallBack
    {
        void onSuccess();
    }
}
