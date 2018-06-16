package com.nauk.coinfolio.DataManagers.ExchangeManager;

import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
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

/**
 * Created by Guitoune on 26/02/2018.
 */

public class HitBtcManager {

    private String publicKey;
    private String privateKey;
    final private String hitBalanceUrl = "https://api.hitbtc.com/api/2/trading/balance";
    private RequestQueue requestQueue;

    private List<Currency> balance;
    private android.content.Context context;

    public HitBtcManager(android.content.Context context, String publicKey, String privateKey)
    {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);

        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public void updateBalance(final HitBtcCallBack callBack)
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
        balance = new ArrayList<>();

        for(int i = 0; i < response.length(); i++)
        {
            try {
                JSONObject jsonObject = response.getJSONObject(i);

                if(Float.parseFloat(jsonObject.getString("available")) > 0)
                {
                    balance.add(new Currency(jsonObject.getString("currency"), Double.parseDouble(jsonObject.getString("available"))));
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
