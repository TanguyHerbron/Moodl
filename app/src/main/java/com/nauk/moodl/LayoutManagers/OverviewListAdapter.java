package com.nauk.moodl.LayoutManagers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nauk.moodl.DataManagers.CurrencyData.Currency;
import com.nauk.moodl.DataManagers.CurrencyData.CurrencyCardview;
import com.nauk.moodl.DataManagers.CurrencyData.CurrencyDetailsList;
import com.nauk.moodl.PlaceholderManager;
import com.nauk.moodl.R;

import java.util.List;

import static com.nauk.moodl.MoodlBox.numberConformer;

/**
 * Created by Administrator on 28/05/2018.
 */

public class OverviewListAdapter extends ArrayAdapter<Currency> {

    private Activity activity;
    private CurrencyDetailsList currencyDetailsList;

    public OverviewListAdapter(Context context, List<Currency> currencies, Activity activity)
    {
        super(context, android.R.layout.simple_expandable_list_item_1, currencies);
        this.activity = activity;

        currencyDetailsList = CurrencyDetailsList.getInstance(getContext());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        Currency currency = getItem(position);

        currencyDetailsList.getCurrencyDetailsFromSymbol(currency.getSymbol());

        CurrencyCardview currencyCardview = new CurrencyCardview(getContext(), currency, activity);
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.addView(currencyCardview);
        convertView = linearLayout;

        return convertView;
    }
}
