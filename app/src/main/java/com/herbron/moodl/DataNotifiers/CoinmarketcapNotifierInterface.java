package com.herbron.moodl.DataNotifiers;

import com.herbron.moodl.DataManagers.CurrencyData.Currency;

import java.util.List;

public interface CoinmarketcapNotifierInterface {

    void onCurrenciesRetrieved(List<Currency> currencyList);

    void onTopCurrenciesUpdated();

    void onMarketCapUpdated();

    void onListingUpdated();
}
