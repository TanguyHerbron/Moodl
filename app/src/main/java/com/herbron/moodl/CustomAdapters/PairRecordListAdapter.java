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

import com.herbron.moodl.DataManagers.InfoAPIManagers.Pair;
import com.herbron.moodl.Utils.PlaceholderUtils;
import com.herbron.moodl.R;

import java.util.ArrayList;

public class PairRecordListAdapter extends ArrayAdapter<Pair> {

    private CustomFilter filter;
    private ArrayList<Pair> pairs, suggestions;

    public PairRecordListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Pair> pairs) {
        super(context, resource, pairs);

        this.pairs = pairs;
        this.suggestions = pairs;
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
        return pairs.size();
    }

    @Nullable
    @Override
    public Pair getItem(int position) {
        return pairs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return pairs.indexOf(getItem(position));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Pair pair = getItem(position);

        if(convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.simple_spinner_item, parent, false);
        }

        TextView pairTxtView = convertView.findViewById(R.id.textView);
        pairTxtView.setTextColor(getContext().getResources().getColor(android.R.color.tab_indicator_text));

        pairTxtView.setText(PlaceholderUtils.getPairString(pair.getFrom(), pair.getTo(), getContext()));

        return convertView;
    }

    private class CustomFilter extends Filter
    {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if(constraint != null && constraint.length() > 0)
            {
                constraint = constraint.toString().toUpperCase();

                ArrayList<Pair> filters = new ArrayList<>();

                for(int i = 0; i < suggestions.size(); i++)
                {
                    if(suggestions.get(i).getFrom().contains(constraint) || suggestions.get(i).getTo().contains(constraint))
                    {
                        Pair pair = new Pair(suggestions.get(i));

                        filters.add(pair);
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
                pairs = (ArrayList<Pair>) results.values;
            }

            notifyDataSetChanged();
        }
    }
}
