package com.herbron.moodl.CustomLayouts;

import android.support.v4.app.Fragment;

import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.ExchangeManager.Exchange;
import com.herbron.moodl.DataManagers.InfoAPIManagers.Pair;

public abstract class CustomRecordFragment extends Fragment {

    protected Currency currency;
    protected Exchange exchange;
    protected Pair pair;

    public void setCurrency(Currency currency)
    {
        this.currency = currency;

        onCurrencyUpdated();
    }

    public void setExchange(Exchange exchange)
    {
        this.exchange = exchange;

        onExchangeUpdated();
    }

    public void setPair(Pair pair)
    {
        this.pair = pair;

        onPairUpdated();
    }

    public abstract void onCurrencyUpdated();

    public abstract void onExchangeUpdated();

    public abstract void onPairUpdated();
}
