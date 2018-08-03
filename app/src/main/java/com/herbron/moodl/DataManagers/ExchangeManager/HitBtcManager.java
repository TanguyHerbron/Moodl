package com.herbron.moodl.DataManagers.ExchangeManager;

import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataNotifiers.HitBTCUpdateNotifierInterface;
import com.herbron.moodl.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Guitoune on 26/02/2018.
 */

public class HitBtcManager extends Exchange {

    final private String hitBalanceUrl = "https://api.hitbtc.com/api/2/account/balance";
    final private String hitTradingBalanceUrl = "https://api.hitbtc.com/api/2/trading/balance";
    final private String tradeHistoryUrl = "https://api.hitbtc.com/api/2/history/trades?";
    private RequestQueue requestQueue;
    private List<String> pairSymbolList;
    private boolean isTradingBalanceUpdated;
    private boolean isBalanceUpdated;

    private List<Currency> balance;
    private android.content.Context context;

    private List<HitBTCUpdateNotifierInterface> hitBTCUpdateNotifierInterfaceList;

    public HitBtcManager(android.content.Context context, Exchange exchange)
    {
        super(exchange.id, exchange.name, exchange.type, exchange.description, exchange.publicKey, exchange.privateKey, exchange.isEnabled);

        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    public void addListener(HitBTCUpdateNotifierInterface hitBTCUpdateNotifierInterface)
    {
        if(hitBTCUpdateNotifierInterfaceList == null)
        {
            hitBTCUpdateNotifierInterfaceList = new ArrayList<>();
        }

        hitBTCUpdateNotifierInterfaceList.add(hitBTCUpdateNotifierInterface);
    }

    private void createPairSymbolList()
    {
        pairSymbolList = new ArrayList<>();

        pairSymbolList.add("BTC");
        pairSymbolList.add("ETH");
        pairSymbolList.add("BNB");
        pairSymbolList.add("USDT");
    }

    private void mergeBalanceSymbols()
    {
        List<Currency> mergedBalance = new ArrayList<>();

        for(int i = 0; i < balance.size(); i++)
        {
            boolean updated = false;

            for(int j = 0; j < mergedBalance.size(); j++)
            {
                if(mergedBalance.get(j).getSymbol().equals(balance.get(i).getSymbol()))
                {
                    mergedBalance.get(j).setBalance(mergedBalance.get(j).getBalance() + balance.get(i).getBalance());
                    updated = true;
                }
            }

            if(!updated)
            {
                mergedBalance.add(balance.get(i));
            }
        }

        balance = mergedBalance;
    }

    public void updateGlobalBalance()
    {
        isTradingBalanceUpdated = false;
        isBalanceUpdated = false;

        balance = new ArrayList<>();

        updateBalance(new HitBtcCallBack() {
            @Override
            public void onSuccess() {
                isBalanceUpdated = true;

                if(isTradingBalanceUpdated)
                {
                    mergeBalanceSymbols();

                    for(HitBTCUpdateNotifierInterface hitBTCUpdateNotifierInterface : hitBTCUpdateNotifierInterfaceList)
                    {
                        hitBTCUpdateNotifierInterface.onHitBTCBalanceUpdateSuccess();
                    }
                }
            }

            @Override
            public void onError(String error) {
                for(HitBTCUpdateNotifierInterface hitBTCUpdateNotifierInterface : hitBTCUpdateNotifierInterfaceList)
                {
                    hitBTCUpdateNotifierInterface.onHitBTCBalanceUpdateError(id, error);
                }
            }
        });

        updateTradingBalance(new HitBtcCallBack() {
            @Override
            public void onSuccess() {
                isTradingBalanceUpdated = true;

                if(isBalanceUpdated)
                {
                    mergeBalanceSymbols();

                    for(HitBTCUpdateNotifierInterface hitBTCUpdateNotifierInterface : hitBTCUpdateNotifierInterfaceList)
                    {
                        hitBTCUpdateNotifierInterface.onHitBTCBalanceUpdateSuccess();
                    }
                }
            }

            @Override
            public void onError(String error) {
                for(HitBTCUpdateNotifierInterface hitBTCUpdateNotifierInterface : hitBTCUpdateNotifierInterfaceList)
                {
                    hitBTCUpdateNotifierInterface.onHitBTCBalanceUpdateError(id, error);
                }
            }
        });
    }

    private void updateTradingBalance(final HitBtcCallBack callBack)
    {
        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, hitTradingBalanceUrl
                , getResponseListener(callBack), getErrorResponseListener(callBack))
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = publicKey + ":" + privateKey;
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);

                return headers;
            }
        };

        requestQueue.add(arrayRequest);
    }

    private void updateBalance(final HitBtcCallBack callBack)
    {
        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, hitBalanceUrl
                , getResponseListener(callBack), getErrorResponseListener(callBack))
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = publicKey + ":" + privateKey;
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);

                return headers;
            }
        };

        requestQueue.add(arrayRequest);
    }

    private Response.Listener<JSONArray> getResponseListener(final HitBtcCallBack callBack)
    {
        return new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() > 0) {
                    parseBalance(response);
                }

                callBack.onSuccess();
            }
        };
    }

    private Response.ErrorListener getErrorResponseListener(final HitBtcCallBack callBack)
    {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(context.getResources().getString(R.string.debug), "API Error : " + error);
                callBack.onError(error.toString());
            }
        };
    }

    private void parseBalance(JSONArray response)
    {
        for(int i = 0; i < response.length(); i++)
        {
            try {
                JSONObject jsonObject = response.getJSONObject(i);
                double available = Double.parseDouble(jsonObject.getString("available"));
                double reserved = Double.parseDouble(jsonObject.getString("reserved"));

                if(available > 0 || reserved > 0)
                {
                    switch (jsonObject.getString("currency"))
                    {
                        case "IOTA":
                            balance.add(new Currency("MIOTA", available + reserved));
                            break;
                        default:
                            balance.add(new Currency(jsonObject.getString("currency"), available + reserved));
                            break;
                    }
                }

            } catch (JSONException e) {
                Log.e(context.getResources().getString(R.string.debug), "Invalid JSON Object");
            }
        }
    }

    public List<Currency> getBalance()
    {
        return balance;
    }

    public interface HitBtcCallBack {
        void onSuccess();
        void onError(String error);
    }
}
