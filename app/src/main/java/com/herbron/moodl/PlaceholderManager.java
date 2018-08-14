package com.herbron.moodl;

import android.content.Context;

import com.herbron.moodl.DataManagers.PreferencesManager;

import java.util.ArrayList;
import java.util.List;

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

    public static List<String> getFeeOptionsForSymbol(String symbol, Context context)
    {
        List<String> options = new ArrayList<>();

        options.add(context.getResources().getString(R.string.fixedFee, symbol));
        options.add(context.getResources().getString(R.string.percentageFee, symbol));

        return options;
    }

    public static String getPairString(String pair1, String pair2, Context context)
    {
        return context.getResources().getString(R.string.pairPlaceholder, pair1, pair2);
    }

    public static String getDenomination(String coinName, String coinSymbol, Context context)
    {
        return context.getResources().getString(R.string.denomincationPlaceholder, coinName, coinSymbol);
    }

    public static String getEditTransactionString(String coinName, Context context)
    {
        return context.getResources().getString(R.string.edit_transaction, coinName);
    }

    public static String getEmitedPercentageString(String percentage, Context context)
    {
        return context.getResources().getString(R.string.emitedPlaceholder, percentage);
    }

    public static String getValuePercentageString(String value, String percentage, Context context)
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
