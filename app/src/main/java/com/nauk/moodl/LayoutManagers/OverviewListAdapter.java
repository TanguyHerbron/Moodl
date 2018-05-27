package com.nauk.moodl.LayoutManagers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nauk.moodl.DataManagers.CurrencyData.Currency;
import com.nauk.moodl.PlaceholderManager;
import com.nauk.moodl.R;

import java.util.List;

import static com.nauk.moodl.MoodlBox.numberConformer;

/**
 * Created by Administrator on 28/05/2018.
 */

public class OverviewListAdapter extends ArrayAdapter<Currency> {

    private Context context;

    public OverviewListAdapter(Context context, List<Currency> currencies)
    {
        super(context, android.R.layout.simple_list_item_1, currencies);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        Currency currency = getItem(position);

        if(convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cardview_watchlist, parent, false);
        }

        TextView symbolTxtView = convertView.findViewById(R.id.currencySymbolTextView);
        TextView nameTxtView = convertView.findViewById(R.id.currencyNameTextView);
        TextView valueTxtView = convertView.findViewById(R.id.currencyValueTextView);
        TextView fluctuationTxtView = convertView.findViewById(R.id.currencyFluctuationTextView);
        TextView percentageTxtView = convertView.findViewById(R.id.currencyFluctuationPercentageTextView);
        ImageView iconImageView = convertView.findViewById(R.id.currencyIcon);

        symbolTxtView.setText(currency.getSymbol());
        nameTxtView.setText(currency.getName());
        valueTxtView.setText(PlaceholderManager.getValueString(numberConformer(currency.getValue()), getContext()));
        fluctuationTxtView.setText(PlaceholderManager.getValueParenthesisString(numberConformer(currency.getDayFluctuation()), getContext()));
        percentageTxtView.setText(PlaceholderManager.getPercentageString(numberConformer(currency.getDayFluctuationPercentage()), getContext()));
        iconImageView.setImageBitmap(currency.getIcon());

        return convertView;
    }
}
