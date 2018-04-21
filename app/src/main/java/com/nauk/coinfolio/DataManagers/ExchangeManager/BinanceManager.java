package com.nauk.coinfolio.DataManagers.ExchangeManager;

import android.util.Log;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.account.request.OrderRequest;
import com.binance.api.client.exception.BinanceApiException;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Guitoune on 26/02/2018.
 */

public class BinanceManager {

    private String publicKey;
    private String privateKey;

    private List<Currency> balance;
    private HashMap<String, List<Trade>> trades;

    public BinanceManager(String publicKey, String privateKey)
    {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public void updateBalance(BinanceCallBack callBack)
    {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(publicKey, privateKey);
        BinanceApiRestClient client = factory.newRestClient();

        try {
            Account account = client.getAccount();
            List<AssetBalance> assets = account.getBalances();

            balance = new ArrayList<>();

            for(int i = 0; i < assets.size(); i++)
            {
                if(Double.parseDouble(assets.get(i).getFree()) > 0 || Double.parseDouble(assets.get(i).getLocked()) > 0)
                {
                    balance.add(new Currency(assets.get(i).getAsset(), Double.parseDouble(assets.get(i).getFree()) + Double.parseDouble(assets.get(i).getLocked())));
                }
            }

            callBack.onSuccess();
        } catch (BinanceApiException e) {
            callBack.onError(e.getMessage());
        }
    }

    public void updateTrades(BinanceCallBack callBack, String symbol)
    {
        trades = new HashMap<>();

        trades.put("BTC", updateTrades(null, symbol, "BTC"));

        trades.put("ETH", updateTrades(null, symbol, "ETH"));

        trades.put("USDT", updateTrades(null, symbol, "USDT"));

        callBack.onSuccess();
    }

    public List<Trade> updateTrades(BinanceCallBack callBack, String symbol, String pairSymbol)
    {
        List<Trade> presentTrades = new ArrayList<>();
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(publicKey, privateKey);
        BinanceApiRestClient client = factory.newRestClient();

        if(!symbol.equals(pairSymbol))
        {
            try {
                presentTrades = client.getMyTrades(symbol + pairSymbol, 20);

            } catch (BinanceApiException e) {
                try {
                    presentTrades = client.getMyTrades(pairSymbol + symbol);

                } catch (BinanceApiException f) {
                    f.printStackTrace();
                }
            }
        }

        if(callBack != null)
        {
            callBack.onSuccess();
        }

        return presentTrades;
    }

    public List<Currency> getBalance()
    {
        return balance;
    }

    public HashMap<String, List<Trade>> getTrades()
    {
        return trades;
    }

    public interface BinanceCallBack {
        void onSuccess();
        void onError(String error);
    }
}
