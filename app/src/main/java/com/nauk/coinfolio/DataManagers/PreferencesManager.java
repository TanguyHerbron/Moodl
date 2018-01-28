package com.nauk.coinfolio.DataManagers;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Guitoune on 09/01/2018.
 */

public class PreferencesManager {

    private static final String currencyListFile = "CustomCurrencies";
    private static final String preferencesFile = "Preferences";
    private SharedPreferences settingPreferences;
    private SharedPreferences currencyList;
    private SharedPreferences preferencesList;
    android.content.Context context;

    public PreferencesManager(android.content.Context context)
    {
        this.context = context;
        settingPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        currencyList = context.getSharedPreferences(currencyListFile, 0);
        preferencesList = context.getSharedPreferences(preferencesFile, 0);
    }

    public void addCurrency(String symbol, double balance)
    {
        SharedPreferences.Editor editor = currencyList.edit();
        editor.putString(symbol, String.valueOf(balance));
        editor.apply();
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

    public void disableHitBTC()
    {
        SharedPreferences.Editor editor = settingPreferences.edit();
        editor.putBoolean("enable_hitbtc", false);
        editor.apply();
    }

}
