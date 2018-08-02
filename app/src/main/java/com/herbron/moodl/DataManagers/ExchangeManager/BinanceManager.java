package com.herbron.moodl.DataManagers.ExchangeManager;

import android.util.Log;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.exception.BinanceApiException;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guitoune on 26/02/2018.
 */

public class BinanceManager extends Exchange {

    private List<Currency> balance;
    private ArrayList<com.herbron.moodl.DataManagers.CurrencyData.Trade> trades;
    private static List<String> pairSymbolList;

    public BinanceManager(Exchange exchange)
    {
        super(exchange.id, exchange.name, exchange.type, exchange.description, exchange.publicKey, exchange.privateKey, exchange.isEnabled);

        createPairSymbolList();
    }

    private void createPairSymbolList()
    {
        pairSymbolList = new ArrayList<>();

        pairSymbolList.add("BTC");
        pairSymbolList.add("ETH");
        pairSymbolList.add("BNB");
        pairSymbolList.add("USDT");
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
                    //balance.add(new Currency(assets.get(i).getAsset(), Double.parseDouble(assets.get(i).getFree()) + Double.parseDouble(assets.get(i).getLocked())));
                    if(!assets.get(i).getAsset().equals("VEN"))
                    {
                        balance.add(new Currency(assets.get(i).getAsset(), Double.parseDouble(assets.get(i).getFree()) + Double.parseDouble(assets.get(i).getLocked())));
                    }
                }
            }

            callBack.onSuccess();
        } catch (BinanceApiException e) {
            callBack.onError(e.getMessage());
        }
    }

    public void updateTrades(BinanceCallBack callBack, String symbol)
    {
        trades = new ArrayList<>();

        for(int i = 0; i < pairSymbolList.size(); i++)
        {
            if(!pairSymbolList.get(i).equals(symbol))
            {
                trades.addAll(updateTrades(symbol, pairSymbolList.get(i)));
            }
        }

        callBack.onSuccess();
    }

    public void updateTrades(BinanceCallBack callBack, String symbol, long fromId)
    {
        trades = new ArrayList<>();

        for(int i = 0; i < pairSymbolList.size(); i++)
        {
            if(!pairSymbolList.get(i).equals(symbol))
            {
                trades.addAll(updateTrades(symbol, pairSymbolList.get(i), fromId));
            }
        }

        callBack.onSuccess();
    }

    public void updateTransactions(String symbol)
    {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(publicKey, privateKey);
        BinanceApiRestClient client = factory.newRestClient();
    }


    public List<com.herbron.moodl.DataManagers.CurrencyData.Trade> updateTrades(String symbol, String pairSymbol)
    {
        List<Trade> presentTrades = new ArrayList<>();
        List<com.herbron.moodl.DataManagers.CurrencyData.Trade> customTrades = new ArrayList<>();
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(publicKey, privateKey);
        BinanceApiRestClient client = factory.newRestClient();

        if(!symbol.equals(pairSymbol))
        {
            try {
                presentTrades = client.getMyTrades(symbol + pairSymbol);


            } catch (BinanceApiException e) {
                /*try {
                    presentTrades = client.getMyTrades(pairSymbol + symbol);

                } catch (BinanceApiException f) {
                    f.printStackTrace();
                }*/
            }
        }

        for(int i = 0; i < presentTrades.size(); i++)
        {
            customTrades.add(new com.herbron.moodl.DataManagers.CurrencyData.Trade(symbol, pairSymbol, presentTrades.get(i)));
        }

        return customTrades;
    }


    public List<com.herbron.moodl.DataManagers.CurrencyData.Trade> updateTrades(String symbol, String pairSymbol, long fromId)
    {
        List<Trade> presentTrades = new ArrayList<>();
        List<com.herbron.moodl.DataManagers.CurrencyData.Trade> customTrades = new ArrayList<>();
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(publicKey, privateKey);
        BinanceApiRestClient client = factory.newRestClient();

        if(!symbol.equals(pairSymbol))
        {
            try {
                presentTrades = client.getMyTrades(symbol + pairSymbol, 20, fromId, System.currentTimeMillis(), System.currentTimeMillis());


            } catch (BinanceApiException e) {
                try {
                    presentTrades = client.getMyTrades(pairSymbol + symbol, 20, fromId, System.currentTimeMillis(), System.currentTimeMillis());

                } catch (BinanceApiException f) {
                    f.printStackTrace();
                }
            }
        }

        for(int i = 0; i < presentTrades.size(); i++)
        {
            customTrades.add(new com.herbron.moodl.DataManagers.CurrencyData.Trade(symbol, pairSymbol, presentTrades.get(i)));
        }

        return customTrades;
    }

    public List<Currency> getBalance()
    {
        return balance;
    }

    public ArrayList<com.herbron.moodl.DataManagers.CurrencyData.Trade> getTrades()
    {
        return trades;
    }

    public interface BinanceCallBack {
        void onSuccess();
        void onError(String error);
    }
}
