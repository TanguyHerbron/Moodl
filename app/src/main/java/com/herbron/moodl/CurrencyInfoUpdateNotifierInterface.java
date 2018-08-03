package com.herbron.moodl;

import com.herbron.moodl.DataManagers.CurrencyData.Currency;

public interface CurrencyInfoUpdateNotifierInterface {

    void onTimestampPriveUpdated(String price);

    void onHistoryDataUpdated();

    void onPriceUpdated(Currency currency);

}
