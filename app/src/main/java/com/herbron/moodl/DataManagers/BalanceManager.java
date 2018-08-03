package com.herbron.moodl.DataManagers;

import com.herbron.moodl.Activities.HomeActivity;
import com.herbron.moodl.DataNotifiers.BinanceUpdateNotifierInterface;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.InfoAPIManagers.CryptocompareApiManager;
import com.herbron.moodl.DataManagers.ExchangeManager.BinanceManager;
import com.herbron.moodl.DataManagers.ExchangeManager.HitBtcManager;
import com.herbron.moodl.DataNotifiers.BalanceUpdateNotifierInterface;
import com.herbron.moodl.DataNotifiers.HitBTCUpdateNotifierInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tiji on 25/12/2017.
 */

public class BalanceManager implements BinanceUpdateNotifierInterface, HitBTCUpdateNotifierInterface {

    private List<Currency> manualBalances;
    private List<Currency> totalBalance;
    private android.content.Context context;
    private DatabaseManager databaseManager;
    private CryptocompareApiManager cryptocompareApiManager;

    private int balanceCounter;

    //NEW IMPLEMENTATION
    private List<HitBtcManager> hitBtcManagers;
    private List<BinanceManager> binanceManagers;

    private BalanceUpdateNotifierInterface balanceUpdateNotifierInterface;

    public BalanceManager(android.content.Context context)
    {
        this.context = context;

        manualBalances = new ArrayList<Currency>();
        databaseManager = new DatabaseManager(context);
        hitBtcManagers = new ArrayList<>();
        binanceManagers = new ArrayList<>();
        cryptocompareApiManager = CryptocompareApiManager.getInstance(context);

        balanceCounter = 0;

        setListener((BalanceUpdateNotifierInterface) ((HomeActivity) context).getHoldingsFragment());
    }

    public void setListener(BalanceUpdateNotifierInterface balanceUpdateNotifierInterface)
    {
        this.balanceUpdateNotifierInterface = balanceUpdateNotifierInterface;
    }

    public void updateExchangeKeys()
    {
        hitBtcManagers.clear();

        hitBtcManagers = databaseManager.getHitBtcAccounts(context);

        binanceManagers.clear();

        binanceManagers = databaseManager.getBinanceAccounts();
    }

    public List<Currency> getTotalBalance()
    {
        return totalBalance;
    }

    public void updateTotalBalance()
    {
        boolean isUpdated = false;

        updateExchangeKeys();
        
        balanceCounter = 0;

        manualBalances = databaseManager.getAllCurrenciesFromManualCurrency();

        if(binanceManagers.size() > 0)
        {
            isUpdated = true;

            for(int i = 0; i < binanceManagers.size(); i++)
            {
                final BinanceManager binanceManager = binanceManagers.get(i);
                binanceManager.addListener(this);
                binanceManager.updateBalance();
            }
        }

        if(hitBtcManagers.size() > 0)
        {
            isUpdated = true;

            for(int i = 0; i < hitBtcManagers.size(); i++)
            {
                final HitBtcManager hitBtcManager = hitBtcManagers.get(i);
                hitBtcManager.addListener(this);
                hitBtcManager.updateGlobalBalance();
            }
        }

        if(!isUpdated)
        {
            refreshAllBalances();
        }
    }

    private void countBalances()
    {
        balanceCounter++;

        if(balanceCounter == hitBtcManagers.size() + binanceManagers.size())
        {
            refreshAllBalances();

            balanceCounter = 0;
        }
    }

    private void refreshAllBalances()
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

        balanceUpdateNotifierInterface.onBalanceDataUpdated();
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

    @Override
    public void onBinanceTradesUpdated() {

    }

    @Override
    public void onBinanceBalanceUpdateSuccess() {
        countBalances();
    }

    @Override
    public void onBinanceBalanceUpdateError(int accountId, String error) {
        databaseManager.disableExchangeAccount(accountId);
        balanceUpdateNotifierInterface.onBalanceError(error);
    }

    @Override
    public void onHitBTCBalanceUpdateSuccess() {
        countBalances();
    }

    @Override
    public void onHitBTCBalanceUpdateError(int accountId, String error) {
        databaseManager.disableExchangeAccount(accountId);
        balanceUpdateNotifierInterface.onBalanceError(error);
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

    public CryptocompareApiManager getCryptocompareApiManager()
    {
        return cryptocompareApiManager;
    }

    public String getCurrencyName(String symbol)
    {
        String currencyName = null;

        try {
            JSONObject jsonObject = new JSONObject(cryptocompareApiManager.getCoinInfosHashmap().get(symbol));
            currencyName = jsonObject.getString("CoinName");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            currencyName = symbol;
        }

        return currencyName;
    }

    public int getCurrencyId(String symbol)
    {
        int id = 0;

        try {
            JSONObject jsonObject = new JSONObject(cryptocompareApiManager.getCoinInfosHashmap().get(symbol));
            id = jsonObject.getInt("Id");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            id = -1;
        }

        return id;
    }
}
