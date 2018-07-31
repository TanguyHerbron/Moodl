package com.herbron.moodl.LayoutManagers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.CurrencyData.Trade;
import com.herbron.moodl.DataManagers.ExchangeManager.Exchange;
import com.herbron.moodl.R;

import java.util.ArrayList;

import static com.herbron.moodl.MoodlBox.getDateFromTimestamp;

public class ExchangeListAdapter extends ArrayAdapter<Exchange> {

    private Context context;

    public ExchangeListAdapter(Context context, ArrayList<Exchange> exchanges)
    {
        super(context, android.R.layout.simple_list_item_1, exchanges);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Exchange exchange = getItem(position);

        if(convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.exchange_cell, parent, false);
        }

        TextView exchangeNameTextView = convertView.findViewById(R.id.exchange_name);
        TextView exchangeDescriptionTextView = convertView.findViewById(R.id.exchange_description);

        exchangeNameTextView.setText(exchange.getName());
        exchangeDescriptionTextView.setText(exchange.getDescription());

        return convertView;
    }

}