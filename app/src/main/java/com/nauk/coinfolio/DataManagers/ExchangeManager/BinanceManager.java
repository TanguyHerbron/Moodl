package com.nauk.coinfolio.DataManagers.ExchangeManager;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Guitoune on 26/02/2018.
 */

public class BinanceManager {

    private String publicKey;
    private String privateKey;

    private List<Currency> balance;

    public BinanceManager(){}

    public BinanceManager(String publicKey, String privateKey)
    {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public void updateBalance(BinanceCallBack callBack)
    {
        Map<String, AssetBalance> accountBalanceCache;
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(publicKey, privateKey);
        BinanceApiRestClient client = factory.newRestClient();

        Account account = client.getAccount();
        List<AssetBalance> assets = account.getBalances();

        balance = new ArrayList<>();

        for(int i = 0; i < assets.size(); i++)
        {
            if(Double.parseDouble(assets.get(i).getFree()) > 0)
            {
                balance.add(new Currency(assets.get(i).getAsset(), Double.parseDouble(assets.get(i).getFree())));
            }
        }

        callBack.onSuccess();
    }

    public void setPublicKey(String publicKey)
    {
        this.publicKey = publicKey;
    }

    public void setPrivateKey(String privateKey)
    {
        this.privateKey = privateKey;
    }

    public List<Currency> getBalance()
    {
        return balance;
    }

    public interface BinanceCallBack {
        void onSuccess();
        void onError(String error);
    }
}
