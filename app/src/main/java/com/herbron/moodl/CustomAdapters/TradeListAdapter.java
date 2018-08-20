package com.herbron.moodl.CustomAdapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.herbron.moodl.DataManagers.CurrencyData.Trade;
import com.herbron.moodl.R;

import java.util.ArrayList;

import static com.herbron.moodl.MoodlBox.getDateFromTimestamp;

/**
 * Created by Guitoune on 24/04/2018.
 */

public class TradeListAdapter extends ArrayAdapter<Trade> {

    private Context context;

    public TradeListAdapter(Context context, ArrayList<Trade> trades)
    {
        super(context, android.R.layout.simple_list_item_1, trades);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Trade trade = getItem(position);

        if(convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_trade_row, parent, false);
        }

        TextView amountTxtView = convertView.findViewById(R.id.amountPurchased);
        TextView purchasedPrice = convertView.findViewById(R.id.purchasePrice);
        TextView tradePair = convertView.findViewById(R.id.pair);
        TextView dateTxtView = convertView.findViewById(R.id.tradeDate);
        View tradeIndicator = convertView.findViewById(R.id.tradeIndicator);

        amountTxtView.setText(String.valueOf(trade.getQty()));
        purchasedPrice.setText(trade.getPrice());
        dateTxtView.setText(getDateFromTimestamp(trade.getTime()));
        tradePair.setText(trade.getSymbol() + "/" + trade.getPairSymbol());

        if(trade.isBuyer())
        {
            tradeIndicator.setBackgroundColor(context.getResources().getColor(R.color.green));
        }
        else
        {
            tradeIndicator.setBackgroundColor(context.getResources().getColor(R.color.red));
        }

        return convertView;
    }
}
