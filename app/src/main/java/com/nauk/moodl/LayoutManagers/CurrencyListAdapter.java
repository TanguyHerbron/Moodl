package com.nauk.moodl.LayoutManagers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.nauk.moodl.DataManagers.CurrencyData.Currency;
import com.nauk.moodl.R;

import java.util.ArrayList;

/**
 * Created by Guitoune on 17/01/2018.
 */

public class CurrencyListAdapter extends BaseAdapter implements Filterable {

        private ArrayList<Currency> currencies, suggestions;
        private Context context;
        private CustomFilter filter;

        public CurrencyListAdapter(Context context, ArrayList<Currency> currencies) {
            this.context = context;
            this.currencies = currencies;
            this.suggestions = currencies;
        }

        @Override
        public int getCount() {
            return currencies.size();
        }

        @Override
        public Object getItem(int position) {
            return currencies.get(position);
        }

        @Override
        public long getItemId(int position) {
            return currencies.indexOf(getItem(position));
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.custom_currency_row, parent, false);
            }

            TextView currencyName = convertView.findViewById(R.id.currencyName);
            TextView currencySymbol = convertView.findViewById(R.id.currencySymbol);

            if (currencyName != null)
                currencyName.setText(currencies.get(position).getName());

            if(currencySymbol != null)
            {
                currencySymbol.setText(currencies.get(position).getSymbol());
            }

            if (position % 2 == 0)
                convertView.setBackgroundColor(context.getResources().getColor(R.color.listBackground2));
            else
                convertView.setBackgroundColor(context.getResources().getColor(R.color.listBackground));

            return convertView;
        }

        @NonNull
        @Override
        public Filter getFilter() {

            if(filter == null)
            {
                filter = new CustomFilter();
            }
            return filter;
        }

        class CustomFilter extends Filter
        {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if(constraint != null && constraint.length() > 0)
                {
                    constraint = constraint.toString().toLowerCase();

                    ArrayList<Currency> filters = new ArrayList<Currency>();

                    for(int i = 0; i < suggestions.size(); i++)
                    {
                        if(suggestions.get(i).getName().toLowerCase().contains(constraint) || suggestions.get(i).getSymbol().toLowerCase().contains(constraint))
                        {
                            Currency currency = new Currency(suggestions.get(i).getName(), suggestions.get(i).getSymbol());

                            filters.add(currency);
                        }
                    }

                    results.count = filters.size();
                    results.values = filters;
                }
                else
                {
                    results.count = suggestions.size();
                    results.values = suggestions;
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                if(results != null)
                {
                    currencies = (ArrayList<Currency>) results.values;
                }

                notifyDataSetChanged();
            }
        }
}
