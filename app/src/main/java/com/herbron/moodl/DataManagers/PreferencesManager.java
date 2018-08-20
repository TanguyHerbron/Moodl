package com.herbron.moodl.DataManagers;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Guitoune on 09/01/2018.
 */

public class PreferencesManager {

    private SharedPreferences settingPreferences;

    public PreferencesManager(android.content.Context context)
    {
        settingPreferences = PreferenceManager.getDefaultSharedPreferences(context);
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

    public String getDefaultCurrency()
    {
        return settingPreferences.getString("default_currency", "USD");
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
