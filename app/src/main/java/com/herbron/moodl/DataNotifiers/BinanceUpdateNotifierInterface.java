package com.herbron.moodl.DataNotifiers;

import com.herbron.moodl.DataManagers.CurrencyData.Trade;

import java.util.List;

public interface BinanceUpdateNotifierInterface {

    void onBinanceTradesUpdated(List<Trade> trades);

    void onBinanceBalanceUpdateSuccess();

    void onBinanceBalanceUpdateError(int accountId, String error);
}
