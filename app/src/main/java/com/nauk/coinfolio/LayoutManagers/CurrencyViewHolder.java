package com.nauk.coinfolio.LayoutManagers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.nauk.coinfolio.R;

/**
 * Created by Tiji on 07/04/2018.
 */

public class CurrencyViewHolder extends RecyclerView.ViewHolder {

    protected ProgressBar dominancePercentageProgrressBar;
    protected ImageView currencyIcon;
    protected TextView currencyNameTextView;
    protected TextView currencySymbolTextView;
    protected TextView currencyValueTextView;
    protected TextView currencyOwnedTextView;
    protected TextView currencyValueOwnedTextView;
    protected TextView currencyFluctuationPercentageTextView;
    protected TextView currencyFluctuationTextView;
    protected LinearLayout collapsableLayout;
    protected LineChart lineChart;
    protected ImageView detailsArrow;

    public CurrencyViewHolder(View v)
    {
        super(v);

        dominancePercentageProgrressBar = v.findViewById(R.id.currencyPortfolioDominance);
        currencyIcon = v.findViewById(R.id.currencyIcon);
        currencyNameTextView = v.findViewById(R.id.currencyNameTextView);
        currencySymbolTextView = v.findViewById(R.id.currencySymbolTextView);
        currencyValueTextView = v.findViewById(R.id.currencyValueTextView);
        currencyOwnedTextView = v.findViewById(R.id.currencyOwnedTextView);
        currencyValueOwnedTextView = v.findViewById(R.id.currencyValueOwnedTextView);
        currencyFluctuationPercentageTextView = v.findViewById(R.id.currencyFluctuationPercentageTextView);
        currencyFluctuationTextView = v.findViewById(R.id.currencyFluctuationTextView);
        collapsableLayout = v.findViewById(R.id.collapsableLayout);
        lineChart = v.findViewById(R.id.LineChartView);
        detailsArrow = v.findViewById(R.id.detailsArrow);
    }

}
