package com.herbron.moodl.LayoutManagers;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.herbron.moodl.Activities.AddExchangeActivity;
import com.herbron.moodl.Activities.ExchangeListActivity;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.CurrencyData.Trade;
import com.herbron.moodl.DataManagers.DatabaseManager;
import com.herbron.moodl.DataManagers.ExchangeManager.Exchange;
import com.herbron.moodl.MoodlBox;
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
        ImageView accountOffImageView = convertView.findViewById(R.id.exchange_account_off_imageView);

        exchangeNameTextView.setText(exchange.getName());
        exchangeDescriptionTextView.setText(exchange.getDescription());

        if(!exchange.isEnabled())
        {
            accountOffImageView.setVisibility(View.VISIBLE);
        }

        convertView.findViewById(R.id.editExchangeInfosLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editExchangeAccountIntent = new Intent(context, AddExchangeActivity.class);
                editExchangeAccountIntent.putExtra("isEdit", true);
                editExchangeAccountIntent.putExtra("exchangeId", exchange.getId());
                context.startActivity(editExchangeAccountIntent);
            }
        });

        convertView.findViewById(R.id.deleteExchangeInfosLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseManager databaseManager = new DatabaseManager(getContext());
                databaseManager.deleteExchangeAccountFromId(exchange.getId());
                remove(exchange);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

}
