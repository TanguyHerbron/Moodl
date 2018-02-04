package com.nauk.coinfolio.LayoutManagers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;

import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.R;

/**
 * Created by Guitoune on 17/01/2018.
 */

public class CurrencyAdapter extends ArrayAdapter<Currency> {

        private ArrayList<Currency> tempCurrency, suggestions;
        private Context context;

        public CurrencyAdapter(Context context, ArrayList<Currency> objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            this.tempCurrency = new ArrayList<Currency>(objects);
            this.suggestions = new ArrayList<Currency>(objects);

            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Currency currency = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_currency_row, parent, false);
            }
            TextView currencyName = (TextView) convertView.findViewById(R.id.currencyName);
            TextView currencySymbol = (TextView) convertView.findViewById(R.id.currencySymbol);
            if (currencyName != null)
                currencyName.setText(currency.getName());
            if(currencySymbol != null)
            {
                currencySymbol.setText(currency.getSymbol());
            }
            // Now assign alternate color for rows
            if (position % 2 == 0)
                convertView.setBackgroundColor(context.getResources().getColor(R.color.listBackground));
            else
                convertView.setBackgroundColor(context.getResources().getColor(R.color.listBackground2));

            return convertView;
        }

        @Override
        public Filter getFilter() {
            return myFilter;
        }

        Filter myFilter = new Filter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                Currency currency = (Currency) resultValue;
                return currency.getName();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint != null) {
                    suggestions.clear();

                    String temp = constraint.toString().toLowerCase();
                    for (Currency currency : tempCurrency) {
                        if (currency.getName().toLowerCase().startsWith(temp)
                                || currency.getSymbol().toLowerCase().startsWith(temp)) {
                            suggestions.add(currency);
                        }
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                    return filterResults;
                } else {
                    return new FilterResults();
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                ArrayList<Currency> c = (ArrayList<Currency>) results.values;
                if (results != null && results.count > 0) {
                    clear();
                    for (Currency currency : c) {
                        add(currency);
                        notifyDataSetChanged();
                    }
                }
            }
        };
}
