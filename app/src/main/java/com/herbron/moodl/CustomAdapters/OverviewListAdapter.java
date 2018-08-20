package com.herbron.moodl.CustomAdapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.CurrencyData.CurrencyCardview;
import com.herbron.moodl.DataManagers.InfoAPIManagers.CryptocompareApiManager;

import java.util.List;

/**
 * Created by Administrator on 28/05/2018.
 */

public class OverviewListAdapter extends ArrayAdapter<Currency> {

    private Activity activity;
    private CryptocompareApiManager cryptocompareApiManager;

    public OverviewListAdapter(Context context, List<Currency> currencies, Activity activity)
    {
        super(context, android.R.layout.simple_expandable_list_item_1, currencies);
        this.activity = activity;

        cryptocompareApiManager = CryptocompareApiManager.getInstance(getContext());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        Currency currency = getItem(position);

        cryptocompareApiManager.getCurrencyDetailsFromSymbol(currency.getSymbol());

        CurrencyCardview currencyCardview = new CurrencyCardview(getContext(), currency, activity);
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.addView(currencyCardview);
        convertView = linearLayout;

        return convertView;
    }
}
