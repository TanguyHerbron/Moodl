package com.herbron.moodl;

import com.herbron.moodl.DataManagers.PreferencesManager;

/**
 * Created by Tiji on 19/04/2018.
 */

public class PlaceholderManager {

    public static String getValueString(String value, android.content.Context context)
    {
        PreferencesManager preferencesManager = new PreferencesManager(context);
        String formattedString = null;

        switch (preferencesManager.getDefaultCurrency())
        {
            case "EUR":
                formattedString = context.getResources().getString(R.string.currencyEurosPlaceholder, value);
                break;
            case "GBP":
                formattedString = context.getResources().getString(R.string.currencyPoundPlaceholder, value);
                break;
            case "JPY":
                formattedString = context.getResources().getString(R.string.currencyYenPlaceholder, value);
                break;
            default:
                formattedString = context.getResources().getString(R.string.currencyDollarPlaceholder, value);
                break;
        }

        return formattedString;
    }

    public static String getValuePercentageString(String value, String percentage, android.content.Context context)
    {
        PreferencesManager preferencesManager = new PreferencesManager(context);
        String formattedString = null;

        switch (preferencesManager.getDefaultCurrency())
        {
            case "EUR":
                formattedString = context.getResources().getString(R.string.fluctuationEurosPercentagePlaceholder, value, percentage);
                break;
            case "GBP":
                formattedString = context.getResources().getString(R.string.fluctuationPoundPercentagePlaceholder, value, percentage);
                break;
            case "JPY":
                formattedString = context.getResources().getString(R.string.fluctuationYenPercentagePlaceholder, value, percentage);
                break;
            default:
                formattedString = context.getResources().getString(R.string.fluctuationDollarPercentagePlaceholder, value, percentage);
                break;
        }

        return formattedString;
    }

    public static String getValueParenthesisString(String value, android.content.Context context)
    {
        PreferencesManager preferencesManager = new PreferencesManager(context);
        String formattedString = null;

        switch (preferencesManager.getDefaultCurrency())
        {
            case "EUR":
                formattedString = context.getResources().getString(R.string.currencyEurosParenthesisPlaceholder, value);
                break;
            case "GBP":
                formattedString = context.getResources().getString(R.string.currencyPoundParenthesisPlaceholder, value);
                break;
            case "JPY":
                formattedString = context.getResources().getString(R.string.currencyYenParenthesisPlaceholder, value);
                break;
            default:
                formattedString = context.getResources().getString(R.string.currencyDollarParenthesisPlaceholder, value);
                break;
        }

        return formattedString;
    }

    public static String getPriceString(String value, android.content.Context context)
    {
        PreferencesManager preferencesManager = new PreferencesManager(context);
        String formattedString = null;

        switch (preferencesManager.getDefaultCurrency())
        {
            case "EUR":
                formattedString = context.getResources().getString(R.string.priceEurosPlaceholder, value);
                break;
            case "GBP":
                formattedString = context.getResources().getString(R.string.pricePoundPlaceholder, value);
                break;
            case "JPY":
                formattedString = context.getResources().getString(R.string.priceYenPlaceholder, value);
                break;
            default:
                formattedString = context.getResources().getString(R.string.priceDollarPlaceholder, value);
                break;
        }

        return formattedString;
    }

    public static String getVolumeString(String value, android.content.Context context)
    {
        PreferencesManager preferencesManager = new PreferencesManager(context);
        String formattedString = null;

        switch (preferencesManager.getDefaultCurrency())
        {
            case "EUR":
                formattedString = context.getResources().getString(R.string.volumeEurosPlaceholder, value);
                break;
            case "GBP":
                formattedString = context.getResources().getString(R.string.volumePoundPlaceholder, value);
                break;
            case "JPY":
                formattedString = context.getResources().getString(R.string.volumeYenPlaceholder, value);
                break;
            default:
                formattedString = context.getResources().getString(R.string.volumeDollarPlaceholder, value);
                break;
        }

        return formattedString;
    }

    public static String getSymbolString(String symbol, android.content.Context context)
    {
        return context.getResources().getString(R.string.currencySymbolPlaceholder, symbol);
    }

    public static String getBalanceString(String balance, String symbol, android.content.Context context)
    {
        return context.getResources().getString(R.string.currencyBalancePlaceholder, balance, symbol);
    }

    public static String getPercentageString(String value, android.content.Context context)
    {
        return context.getResources().getString(R.string.currencyPercentagePlaceholder, value);
    }

    public static String getTimestampString(String date, android.content.Context context)
    {
        return context.getResources().getString(R.string.timestampPlaceholder, date);
    }
}
