package com.nauk.coinfolio.LayoutManagers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.nauk.coinfolio.Activities.CurrencyDetailsActivity;
import com.nauk.coinfolio.Activities.HomeActivity;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.DataManagers.CurrencyData.CurrencyDataChart;
import com.nauk.coinfolio.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.abs;

/**
 * Created by Tiji on 07/04/2018.
 */

public class CurrencyCardViewAdapter extends RecyclerView.Adapter<CurrencyViewHolder> {

    List<Currency> currencies;
    Context context;
    boolean isBalanceHidden;
    float totalValue;

    public CurrencyCardViewAdapter(Context context, List<Currency> currencies, boolean isExtended, float totalValue, boolean isBalanceHidden)
    {
        this.context = context;
        this.currencies = currencies;
        this.totalValue = totalValue;
        this.isBalanceHidden = isBalanceHidden;
    }

    @NonNull
    @Override
    public CurrencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_currency, parent, false);
        return new CurrencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyViewHolder holder, int position) {
        holder.currencyIcon.setImageBitmap(currencies.get(position).getIcon());
        holder.currencyNameTextView.setText(currencies.get(position).getName());
        holder.currencySymbolTextView.setText(context.getResources().getString(R.string.currencySymbolPlaceholder, currencies.get(position).getSymbol()));
        holder.currencyOwnedTextView.setText(context.getResources().getString(R.string.currencyBalancePlaceholder, numberConformer(currencies.get(position).getBalance()), currencies.get(position).getSymbol()));
        holder.currencyValueOwnedTextView.setText(context.getResources().getString(R.string.currencyDollarParenthesisPlaceholder, numberConformer(currencies.get(position).getValue() * currencies.get(position).getBalance())));
        holder.currencyValueTextView.setText(context.getResources().getString(R.string.currencyDollarPlaceholder, numberConformer(currencies.get(position).getValue())));
        holder.currencyFluctuationPercentageTextView.setText(currencies.get(position).getName());
        holder.currencyNameTextView.setText(context.getResources().getString(R.string.currencyPercentagePlaceholder, numberConformer(currencies.get(position).getDayFluctuationPercentage())));
        holder.currencyFluctuationTextView.setText(context.getResources().getString(R.string.currencyDollarParenthesisPlaceholder, numberConformer(currencies.get(position).getDayFluctuation())));
        holder.detailsArrow.getDrawable().setColorFilter(new PorterDuffColorFilter(currencies.get(position).getChartColor(), PorterDuff.Mode.SRC_IN));

        if(isBalanceHidden)
        {
            double value = currencies.get(position).getValue() * currencies.get(position).getBalance();
            double percentage = value / totalValue * 100;

            holder.dominancePercentageProgrressBar.setVisibility(View.VISIBLE);
            holder.dominancePercentageProgrressBar.setProgress((int) Math.round(percentage));
            holder.dominancePercentageProgrressBar.getIndeterminateDrawable().setColorFilter(currencies.get(position).getChartColor(), PorterDuff.Mode.SRC_ATOP);
        }
        else
        {
            holder.dominancePercentageProgrressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    private void setupLineChart(View view, final Currency currency)
    {
        LineChart lineChart = view.findViewById(R.id.LineChartView);

        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(false);
        lineChart.setDrawMarkers(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setScaleEnabled(false);
        lineChart.setDragEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisLeft().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getXAxis().setEnabled(false);
        lineChart.setViewPortOffsets(0, 0, 0, 0);
        lineChart.setData(generateData(currency));

        /*lineChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.class, CurrencyDetailsActivity.class);
                intent.putExtra("currency", currency);
                context.getApplicationContext().startActivity(intent);
            }
        });*/
    }

    private LineData generateData(Currency currency)
    {
        LineDataSet dataSet;
        List<CurrencyDataChart> dataChartList = currency.getHistoryMinutes();
        ArrayList<Entry> values = new ArrayList<>();

        for(int i = 0; i < dataChartList.size(); i+=10)
        {
            values.add(new Entry(i, (float) dataChartList.get(i).getOpen()));
        }

        dataSet = new LineDataSet(values, "History");
        dataSet.setDrawIcons(false);
        dataSet.setColor(currency.getChartColor());
        dataSet.setLineWidth(1);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getColorWithAplha(currency.getChartColor(), 0.5f));
        dataSet.setFormLineWidth(1);
        dataSet.setFormSize(15);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setHighlightEnabled(false);

        return new LineData(dataSet);
    }

    private int getColorWithAplha(int color, float ratio)
    {
        int transColor;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        transColor = Color.argb(alpha, r, g, b);

        return transColor ;
    }

    private String numberConformer(double number)
    {
        String str;

        if(abs(number) > 1)
        {
            str = String.format( Locale.UK, "%.2f", number);
        }
        else
        {
            str = String.format( Locale.UK, "%.4f", number);
        }

        return str;
    }
}
