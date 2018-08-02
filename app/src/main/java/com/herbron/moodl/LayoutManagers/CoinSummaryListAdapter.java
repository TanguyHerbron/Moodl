package com.herbron.moodl.LayoutManagers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.R;

import java.util.ArrayList;

public class CoinSummaryListAdapter extends ArrayAdapter<Currency> {

    private CustomFilter filter;
    private ArrayList<Currency> currencies, suggestions;

    public CoinSummaryListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Currency> currencies) {
        super(context, resource, currencies);

        this.currencies = currencies;
        this.suggestions = currencies;
    }

    @Override
    public int getCount() {
        return currencies.size();
    }

    @Override
    public Currency getItem(int position) {
        return currencies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return currencies.indexOf(getItem(position));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Currency currency = getItem(position);

        if(convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_summary_coin_row, parent, false);
        }

        TextView nameTxtview = convertView.findViewById(R.id.currencyName);
        TextView symbolTxtView = convertView.findViewById(R.id.currencySymbol);

        nameTxtview.setText(currency.getName());
        symbolTxtView.setText(currency.getSymbol());

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

                ArrayList<Currency> filters = new ArrayList<>();

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
