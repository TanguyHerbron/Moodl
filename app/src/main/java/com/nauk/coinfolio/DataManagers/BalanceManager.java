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
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    final private String binanceBalanceUrl = "https://api.binance.com/api/v3/account";
    final private String binanceTimeUrl = "https://api.binance.com/api/v1/time";
    private RequestQueue requestQueue;
    private List<Currency> hitBalance;
    private List<Currency> manualBalances;
    private List<Currency> totalBalance;
    private android.content.Context context;
    private Map<String, String> iconUrlList;
    private Map<String, String> coinList;
    private PreferencesManager preferenceManager;
    private DatabaseManager databaseManager;

    public BalanceManager(android.content.Context context)
    {
        this.context = context;
        preferenceManager = new PreferencesManager(context);
        requestQueue = Volley.newRequestQueue(context);
        hitBalance = new ArrayList<Currency>();
        manualBalances = new ArrayList<Currency>();
        databaseManager = new DatabaseManager(context);
    }

    public List<String> getCurrenciesName()
    {
        return new ArrayList<>(coinList.values());
    }

    public List<String> getCurrenciesSymbol()
    {
        return new ArrayList<>(coinList.keySet());
    }

    public void updateExchangeKeys()
    {
        publicHitKey = preferenceManager.getHitBTCPublicKey();
        privateHitKey = preferenceManager.getHitBTCPrivateKey();
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
        manualBalances = databaseManager.getAllCurrencyFromManualCurrency();

        if(privateHitKey != null && publicHitKey != null && preferenceManager.isHitBTCActivated())
        {
            updateHitBalance(callBack);
        }
        else
        {
            hitBalance = new ArrayList<Currency>();
            refreshAllBalances(callBack);
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
        return iconUrlList.get(symbol);
    }

    private void processDetailResult(String response, final IconCallBack callBack)
    {
        response = response.substring(response.indexOf("\"Data\"") + 7, response.lastIndexOf("},\"Type\":100}"));
        String[] tab = response.split(Pattern.quote("},"));

        iconUrlList = new HashMap<>();
        coinList = new HashMap<>();

        for(int i = 0; i < tab.length; i++)
        {
            tab[i] = tab[i].substring(tab[i].indexOf("\":{")+2, tab[i].length()) + "}";
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                JSONObject jsonObject = new JSONObject(tab[i]);

                iconUrlList.put(jsonObject.getString("Symbol"), "https://www.cryptocompare.com" + jsonObject.getString("ImageUrl") + "?width=50");

                coinList.put(jsonObject.getString("Symbol"), jsonObject.getString("CoinName"));
            } catch (JSONException e) {
                Log.d(context.getResources().getString(R.string.debug), "ImageUrl not found.");
            }
        }

        callBack.onSuccess();
    }
}
