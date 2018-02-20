package com.nauk.coinfolio.DataManagers;

import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by Tiji on 25/12/2017.
 */

public class BalanceManager {

    private String publicHitKey;
    private String publicBinanceKey;
    private String publicPoloniex;
    private String privateHitKey;
    private String privateBinanceKey;
    private String privatePoloniex;
    final private String hitBalanceUrl = "https://api.hitbtc.com/api/2/trading/balance";
    final private String detailUrl = "https://www.cryptocompare.com/api/data/coinlist/";
    private RequestQueue requestQueue;
    private List<Currency> binanceBalance;
    private List<Currency> hitBalance;
    private List<Currency> manualBalances;
    private List<Currency> totalBalance;
    private android.content.Context context;
    private LinkedHashMap<String, String> coinInfosHashmap;
    private PreferencesManager preferenceManager;
    private DatabaseManager databaseManager;

    private BinanceApiClientFactory binanceApiClientFactory;

    public BalanceManager(android.content.Context context)
    {
        this.context = context;
        preferenceManager = new PreferencesManager(context);
        requestQueue = Volley.newRequestQueue(context);
        binanceBalance = new ArrayList<Currency>();
        hitBalance = new ArrayList<Currency>();
        manualBalances = new ArrayList<Currency>();
        databaseManager = new DatabaseManager(context);
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

    public List<String> getBiggestCurrencies()
    {
        List<String> currenciesDetails = new ArrayList<>();

        int index = 0;
        Iterator<String> coinIterator = coinInfosHashmap.keySet().iterator();

        while(index < 11)
        {
            //currenciesDetails.add(index, coinInfosHashmap.keySet().iterator().next());
            index++;

            Log.d("coinfolio", "For " + index + " : " + coinIterator.next());
        }

        return currenciesDetails;
    }

    public List<String> getOrders()
    {
        List<String> currenciesOrder = new ArrayList<>();

        for(String symbol : coinInfosHashmap.keySet())
        {
            try {
                JSONObject jsonObject = new JSONObject(coinInfosHashmap.get(symbol));
                currenciesOrder.add(jsonObject.getString("SortOrder"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return currenciesOrder;
    }

    public List<String> getCurrenciesSymbol()
    {
        return new ArrayList<>(coinInfosHashmap.keySet());
    }

    public void updateExchangeKeys()
    {
        publicHitKey = preferenceManager.getHitBTCPublicKey();
        privateHitKey = preferenceManager.getHitBTCPrivateKey();

        publicBinanceKey = preferenceManager.getBinancePublicKey();
        privateBinanceKey = preferenceManager.getBinancePrivateKey();
    }

    public boolean isBinanceConfigured()
    {
        boolean isConfigured = true;

        if(publicBinanceKey == null || privateBinanceKey == null)
        {
            isConfigured = false;
        }

        return isConfigured;
    }

    public boolean isHitBTCConfigured()
    {
        boolean isConfigured = true;

        if(publicHitKey == null || privateHitKey == null)
        {
            isConfigured = false;
        }

        return isConfigured;
    }

    public void setPublicHitKey(String newKey)
    {
        publicHitKey = newKey;
    }

    public void setPrivateHitKey(String newKey)
    {
        privateHitKey = newKey;
    }

    public List<Currency> getTotalBalance()
    {
        return totalBalance;
    }

    public List<Currency> getHitBalance()
    {
        return hitBalance;
    }

    public List<Currency> getManualBalances()
    {
        return manualBalances;
    }

    public void updateTotalBalance(final VolleyCallBack callBack)
    {
        boolean isUpdated = false;
        manualBalances = databaseManager.getAllCurrencyFromManualCurrency();

        Log.d("coinfolio", "Updating balances " + (privateBinanceKey != null && publicBinanceKey != null && preferenceManager.isBinanceActivated()));

        if(privateHitKey != null && publicHitKey != null && preferenceManager.isHitBTCActivated())
        {
            updateHitBalance(callBack);
            isUpdated = true;
        }
        else
        {
            hitBalance = new ArrayList<Currency>();
        }

        if(privateBinanceKey != null && publicBinanceKey != null && preferenceManager.isBinanceActivated())
        {
            Log.d("coinfolio", "Updating Binance");
            updateBinanceBalance();
            isUpdated = true;
        }

        if(!isUpdated)
        {
            refreshAllBalances(callBack);
        }
    }

    private void updateBinanceBalance()
    {
        Map<String, AssetBalance> accountBalanceCache;
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(publicBinanceKey, privateBinanceKey);
        BinanceApiRestClient client = factory.newRestClient();

        Account account = client.getAccount();
        List<AssetBalance> assets = account.getBalances();

        binanceBalance = new ArrayList<Currency>();

        for(int i = 0; i < assets.size(); i++)
        {
            if(Double.parseDouble(assets.get(i).getFree()) > 0)
            {
                binanceBalance.add(new Currency(assets.get(i).getAsset(), assets.get(i).getFree()));
            }
        }

        Log.d("coinfolio", "Binance size : " + binanceBalance.size());

        for(int i = 0; i < binanceBalance.size(); i++)
        {
            Log.d("coinfolio", "Binance : " + binanceBalance.get(i).getSymbol() + " " + binanceBalance.get(i).getBalance());
        }
    }

    private void updateHitBalance(final VolleyCallBack callBack)
    {
        JsonArrayRequest arrReq = new JsonArrayRequest(Request.Method.GET, hitBalanceUrl,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() > 0) {

                            parseHitBalance(response);
                            refreshAllBalances(callBack);

                        } else {
                            //No balance
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(context.getResources().getString(R.string.debug_volley), "API Error : " + error.toString() + ":");
                        callBack.onError(error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders()throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = publicHitKey + ":" + privateHitKey;
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);

                return headers;
            }
        };

        requestQueue.add(arrReq);
    }

    private void parseHitBalance(JSONArray response)
    {
        hitBalance = new ArrayList<>();

        for (int i = 0; i < response.length(); i++)
        {
            try {
                JSONObject jsonObj = response.getJSONObject(i);

                if(Float.parseFloat(jsonObj.getString("available")) > 0)
                {
                    hitBalance.add(new Currency(jsonObj.getString("currency"), Double.parseDouble(jsonObj.getString("available"))));
                }

            } catch (JSONException e) {
                Log.e(context.getResources().getString(R.string.debug_volley), "Invalid JSON Object");
            }
        }
    }

    private void refreshAllBalances(final VolleyCallBack callBack)
    {
        totalBalance = new ArrayList<>();

        totalBalance.addAll(hitBalance);

        for(int i = 0; i < manualBalances.size(); i++)
        {
            boolean isIn = false;

            for(int j = 0; j < totalBalance.size(); j++)
            {
                if(manualBalances.get(i).getSymbol().equals(totalBalance.get(j).getSymbol()))
                {
                    totalBalance.get(j).setBalance(totalBalance.get(j).getBalance() + manualBalances.get(i).getBalance());

                    isIn = true;
                }
            }

            if(!isIn)
            {
                totalBalance.add(manualBalances.get(i));
            }
        }

        callBack.onSuccess();
    }

    public interface VolleyCallBack {
        void onSuccess();
        void onError(String error);
    }

    public interface IconCallBack {
        void onSuccess();
    }

    public void sortCoins()
    {
        for(int i = 0; i < totalBalance.size(); i++)
        {
            for(int j = i; j < totalBalance.size(); j++)
            {
                if(totalBalance.get(j).getBalance() * totalBalance.get(j).getValue() > totalBalance.get(i).getBalance() * totalBalance.get(i).getValue())
                {
                    Currency temp = totalBalance.get(j);
                    totalBalance.set(j, totalBalance.get(i));
                    totalBalance.set(i, temp);
                }
            }
        }
    }

    public void updateMarketCap(final VolleyCallBack callBack)
    {
        StringRequest strRequest = new StringRequest(Request.Method.GET, detailUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.length() > 0) {

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

    public void updateDetails(final IconCallBack callBack)
    {
        StringRequest strRequest = new StringRequest(Request.Method.GET, detailUrl,
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

    public String getIconUrl(String symbol)
    {
        String url;

        try {
            JSONObject jsonObject = new JSONObject(coinInfosHashmap.get(symbol));
            url = "https://www.cryptocompare.com" + jsonObject.getString("ImageUrl") + "?width=50";
        } catch (NullPointerException e) {
            Log.d(context.getResources().getString(R.string.debug), symbol + " has no icon URL");
            url = null;
        } catch (JSONException e) {
            Log.d(context.getResources().getString(R.string.debug), "Url parsing error for " + symbol);
            url = null;
        }

        return url;
    }

    public String getCurrencyName(String symbol)
    {
        String currencyName = null;

        try {
            JSONObject jsonObject = new JSONObject(coinInfosHashmap.get(symbol));
            currencyName = jsonObject.getString("CoinName");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return currencyName;
    }

    public int getCurrencyId(String symbol)
    {
        int id = 0;

        try {
            JSONObject jsonObject = new JSONObject(coinInfosHashmap.get(symbol));
            id = jsonObject.getInt("Id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return id;
    }

    private void processDetailResult(String response, final IconCallBack callBack)
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
}
