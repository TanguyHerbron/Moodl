package com.herbron.moodl.DataNotifiers;

public interface BinanceUpdateNotifierInterface {

    void onBinanceTradesUpdated();

    void onBinanceBalanceUpdateSuccess();

    void onBinanceBalanceUpdateError(int accountId, String error);
}
