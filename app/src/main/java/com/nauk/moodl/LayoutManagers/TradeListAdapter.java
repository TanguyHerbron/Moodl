package com.nauk.moodl.LayoutManagers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nauk.moodl.DataManagers.CurrencyData.Trade;
import com.nauk.moodl.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.nauk.moodl.MoodlBox.getDateFromTimestamp;

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
        TextView purchasedPrice = convertView.findViewById(R.id.purchasedPrice);
        TextView tradePair = convertView.findViewById(R.id.pair);
        TextView dateTxtView = convertView.findViewById(R.id.tradeDate);
        View tradeIndicator = convertView.findViewById(R.id.tradeIndicator);

        amountTxtView.setText(String.valueOf(trade.getQty()));
        purchasedPrice.setText(trade.getPrice());
        dateTxtView.setText(getDateFromTimestamp(trade.getTime()));
        tradePair.setText(trade.getSymbol() + "/" + trade.getPairSymbol());

        if(trade.isBuyer())
        {
            tradeIndicator.setBackgroundColor(context.getColor(R.color.green));
        }
        else
        {
            tradeIndicator.setBackgroundColor(context.getColor(R.color.red));
        }

        return convertView;
    }
}
