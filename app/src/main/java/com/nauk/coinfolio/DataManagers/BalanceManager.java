package com.nauk.coinfolio.DataManagers;

import android.os.StrictMode;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.DataManagers.ExchangeManager.BinanceManager;
import com.nauk.coinfolio.DataManagers.ExchangeManager.HitBtcManager;
import com.nauk.coinfolio.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
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

    private int balanceCounter;

    //NEW IMPLEMENTATION
    private List<HitBtcManager> hitBtcManagers;
    private List<BinanceManager> binanceManagers;

    public BalanceManager(android.content.Context context)
    {
        this.context = context;

        preferenceManager = new PreferencesManager(context);
        requestQueue = Volley.newRequestQueue(context);
        binanceBalance = new ArrayList<Currency>();
        hitBalance = new ArrayList<Currency>();
        manualBalances = new ArrayList<Currency>();
        databaseManager = new DatabaseManager(context);
        hitBtcManagers = new ArrayList<>();
        binanceManagers = new ArrayList<>();

        balanceCounter = 0;
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
        String publicKey = preferenceManager.getHitBTCPublicKey();
        String privateKey = preferenceManager.getHitBTCPrivateKey();

        hitBtcManagers.clear();

        if(publicKey != null && privateKey != null && preferenceManager.isHitBTCActivated())
        {
            hitBtcManagers.add(new HitBtcManager(context, publicKey, privateKey));
        }

        publicKey = preferenceManager.getBinancePublicKey();
        privateKey = preferenceManager.getBinancePrivateKey();

        binanceManagers.clear();

        if(publicKey != null && privateKey != null && preferenceManager.isBinanceActivated())
        {
            binanceManagers.add(new BinanceManager(publicKey, privateKey));
        }
    }

    public List<Currency> getTotalBalance()
    {
        return totalBalance;
    }

    public void updateTotalBalance(final VolleyCallBack callBack)
    {
        boolean isUpdated = false;

        manualBalances = databaseManager.getAllCurrencyFromManualCurrency();

        if(binanceManagers.size() > 0)
        {
            isUpdated = true;

            for(int i = 0; i < binanceManagers.size(); i++)
            {
                binanceManagers.get(i).updateBalance(new BinanceManager.BinanceCallBack() {
                    @Override
                    public void onSuccess() {
                        countBalances(callBack);
                    }

                    @Override
                    public void onError(String error) {
                        callBack.onError(error);
                    }
                });
            }
        }

        if(hitBtcManagers.size() > 0)
        {
            isUpdated = true;

            for(int i = 0; i < hitBtcManagers.size(); i++)
            {
                hitBtcManagers.get(i).updateBalance(new HitBtcManager.HitBtcCallBack() {
                    @Override
                    public void onSuccess() {
                        countBalances(callBack);
                    }

                    @Override
                    public void onError(String error) {
                        callBack.onError(error);
                    }
                });
            }
        }

        if(!isUpdated)
        {
            refreshAllBalances(callBack);
        }
    }

    private void countBalances(VolleyCallBack callBack)
    {
        balanceCounter++;

        if(balanceCounter == hitBtcManagers.size() + binanceManagers.size())
        {
            refreshAllBalances(callBack);

            balanceCounter = 0;
        }
    }

    private void refreshAllBalances(final VolleyCallBack callBack)
    {
        totalBalance = new ArrayList<>();

        for(int i = 0; i < hitBtcManagers.size(); i++)
        {
            mergeBalanceTotal(hitBtcManagers.get(i).getBalance());
        }

        for(int i = 0; i < binanceManagers.size(); i++)
        {
            mergeBalanceTotal(binanceManagers.get(i).getBalance());
        }

        mergeBalanceTotal(manualBalances);

        callBack.onSuccess();
    }

    private void mergeBalanceTotal(List<Currency> balance)
    {
        if(balance != null)
        {
            for(int i = 0; i < balance.size(); i++)
            {
                boolean isIn = false;

                for(int j = 0; j < totalBalance.size(); j++)
                {
                    if(balance.get(i).getSymbol().equals(totalBalance.get(j).getSymbol()))
                    {
                        totalBalance.get(j).setBalance(totalBalance.get(j).getBalance() + balance.get(i).getBalance());

                        isIn = true;
                    }
                }

                if(!isIn)
                {
                    totalBalance.add(balance.get(i));
                }
            }
        }
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
