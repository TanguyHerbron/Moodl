package com.herbron.moodl;

import com.herbron.moodl.DataManagers.CurrencyData.Currency;

public interface CurrencyInfoUpdateNotifierInterface {

    void onTimestampPriceUpdated(String price);

    void onHistoryDataUpdated();

    void onPriceUpdated(Currency currency);

}
