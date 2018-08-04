package com.herbron.moodl.CustomAdapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.herbron.moodl.DataManagers.ExchangeManager.Exchange;
import com.herbron.moodl.R;

import java.util.ArrayList;

public class ExchangeRecordListAdapter extends ArrayAdapter<Exchange> {

    private CustomFilter filter;
    private ArrayList<Exchange> exchanges, suggestions;

    public ExchangeRecordListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Exchange> exchanges) {
        super(context, resource, exchanges);

        this.exchanges = exchanges;
        this.suggestions = exchanges;
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

    @Override
    public int getCount() {
        return exchanges.size();
    }

    @Nullable
    @Override
    public Exchange getItem(int position) {
        return exchanges.get(position);
    }

    @Override
    public long getItemId(int position) {
        return exchanges.indexOf(getItem(position));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Exchange exchange = getItem(position);

        if(convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.simple_spinner_item, parent, false);
        }

        TextView nameTxtview = convertView.findViewById(R.id.textView);
        nameTxtview.setTextColor(getContext().getResources().getColor(android.R.color.tab_indicator_text));

        nameTxtview.setText(exchange.getName());

        return convertView;
    }

    private class CustomFilter extends Filter
    {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if(constraint != null && constraint.length() > 0)
            {
                constraint = constraint.toString().toLowerCase();

                ArrayList<Exchange> filters = new ArrayList<>();

                for(int i = 0; i < suggestions.size(); i++)
                {
                    if(suggestions.get(i).getName().toLowerCase().contains(constraint))
                    {
                        Exchange exchange = new Exchange(suggestions.get(i));

                        filters.add(exchange);
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
                exchanges = (ArrayList<Exchange>) results.values;
            }

            notifyDataSetChanged();
        }
    }
}
