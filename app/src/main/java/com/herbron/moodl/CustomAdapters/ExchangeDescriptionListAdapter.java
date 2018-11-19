package com.herbron.moodl.CustomAdapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.herbron.moodl.Activities.AddExchangeActivity;
import com.herbron.moodl.DataManagers.DatabaseManager;
import com.herbron.moodl.DataManagers.ExchangeManager.Exchange;
import com.herbron.moodl.R;

import java.util.ArrayList;

import static com.herbron.moodl.DataManagers.DatabaseManager.BINANCE_TYPE;
import static com.herbron.moodl.DataManagers.DatabaseManager.HITBTC_TYPE;

public class ExchangeDescriptionListAdapter extends ArrayAdapter<Exchange> {

    private Context context;

    public ExchangeDescriptionListAdapter(Context context, ArrayList<Exchange> exchanges)
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
        ImageView exchangeImageView = convertView.findViewById(R.id.exchange_icon_imageView);

        exchangeNameTextView.setText(exchange.getName());
        exchangeDescriptionTextView.setText(exchange.getDescription());

        switch (exchange.getType())
        {
            case BINANCE_TYPE:
                exchangeImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.exchange_logo_binance));
                break;
            case HITBTC_TYPE:
                exchangeImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.exchange_logo_hitbtc));
                break;
        }

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
                editExchangeAccountIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
