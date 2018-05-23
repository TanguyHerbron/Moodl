package com.nauk.moodl.DataManagers;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Guitoune on 09/01/2018.
 */

public class PreferencesManager {

    private static int fragmentUpdated = 0;
    private static final String currencyListFile = "CustomCurrencies";
    private static final String preferencesFile = "Preferences";
    private SharedPreferences settingPreferences;
    private SharedPreferences currencyList;
    private SharedPreferences preferencesList;

    public PreferencesManager(android.content.Context context)
    {
        settingPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        currencyList = context.getSharedPreferences(currencyListFile, 0);
        preferencesList = context.getSharedPreferences(preferencesFile, 0);
    }

    public void setDetailOption(boolean isExtended)
    {
        SharedPreferences.Editor editor = preferencesList.edit();
        editor.putBoolean("DetailOption", isExtended);
        editor.apply();
    }

    public boolean getDetailOption()
    {
        return preferencesList.getBoolean("DetailOption", true);
    }

    public boolean mustRefreshDefaultCurrency()
    {
        fragmentUpdated++;

        if(fragmentUpdated == 3)
        {
            disableRefreshDefaultCurrency();
            fragmentUpdated = 0;
        }

        return settingPreferences.getBoolean("refresh_default_currency", false);
    }

    public float getMinimumAmount()
    {
        String str = settingPreferences.getString("minimum_value_displayed", "0");
        float ret;

        if(str.equals(""))
        {
            ret = 0;
        }
        else
        {
            ret = Float.valueOf(str);
        }

        return ret;
    }

    private void disableRefreshDefaultCurrency()
    {
        SharedPreferences.Editor editor = settingPreferences.edit();
        editor.putBoolean("refresh_default_currency", false);
        editor.apply();
    }

    public String getDefaultCurrency()
    {
        return settingPreferences.getString("default_currency", "USD");
    }

    public String getHitBTCPublicKey()
    {
        return settingPreferences.getString("hitbtc_publickey", null);
    }

    public String getHitBTCPrivateKey()
    {
        return settingPreferences.getString("hitbtc_privatekey", null);
    }

    public boolean isHitBTCActivated()
    {
        return settingPreferences.getBoolean("enable_hitbtc", false);
    }

    public boolean switchBalanceHiddenState()
    {
        SharedPreferences.Editor editor = settingPreferences.edit();
        editor.putBoolean("hide_balance", !isBalanceHidden());
        editor.apply();

        return isBalanceHidden();
    }

    public boolean isBalanceHidden()
    {
        return settingPreferences.getBoolean("hide_balance", false);
    }

    public void disableHitBTC()
    {
        SharedPreferences.Editor editor = settingPreferences.edit();
        editor.putBoolean("enable_hitbtc", false);
        editor.apply();
    }

    public String getBinancePublicKey()
    {
        return settingPreferences.getString("binance_publickey", null);
    }

    public String getBinancePrivateKey()
    {
        return settingPreferences.getString("binance_privatekey", null);
    }

    public boolean isBinanceActivated()
    {
        return settingPreferences.getBoolean("enable_binance", false);
    }

    public void disableBinance()
    {
        SharedPreferences.Editor editor = settingPreferences.edit();
        editor.putBoolean("enable_binance", false);
        editor.apply();
    }

    public void setMustUpdateWatchlist(boolean mustUpdate)
    {
        SharedPreferences.Editor editor = settingPreferences.edit();
        editor.putBoolean("mustUpdateWatchlist", mustUpdate);
        editor.apply();
    }

    public boolean mustUpdateWatchlist()
    {
        boolean mustUpdate = settingPreferences.getBoolean("mustUpdateWatchlist", false);

        if(mustUpdate)
        {
            setMustUpdateWatchlist(false);
        }

        return mustUpdate;
    }

    public void setMustUpdateSummary(boolean mustUpdate)
    {
        SharedPreferences.Editor editor = settingPreferences.edit();
        editor.putBoolean("mustUpdateSummary", mustUpdate);
        editor.apply();
    }

    public boolean mustUpdateSummary()
    {
        boolean mustUpdate = settingPreferences.getBoolean("mustUpdateSummary", false);

        if(mustUpdate)
        {
            setMustUpdateSummary(false);
        }

        return mustUpdate;
    }
}
