package com.nauk.coinfolio.LayoutManagers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.db.chart.model.ChartSet;
import com.db.chart.model.LineSet;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.view.LineChartView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.nauk.coinfolio.Activities.CurrencyDetailsActivity;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.DataManagers.CurrencyData.CurrencyDataChart;
import com.nauk.coinfolio.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.abs;

/**
 * Created by Tiji on 05/01/2018.
 */

public class HomeLayoutGenerator {

    android.content.Context context;

    public HomeLayoutGenerator(Context context)
    {
        this.context = context;
    }

    public View getInfoLayout(final Currency currency, boolean isExtended, float totalValue, boolean isBalanceHidden)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_currency, null);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.findViewById(R.id.collapsableLayout).getVisibility() == View.VISIBLE)
                {
                    collapseView(view);
                }
                else
                {
                    extendView(view);
                }
            }
        });

        updateCardViewInfos(view, currency, totalValue, isBalanceHidden);

        setupLineChart(view, currency);

        if(isExtended)
        {
            extendView(view);
        }
        else
        {
            collapseView(view);
        }

        updateColor(view, currency);

        return view;
    }

    public static void expand(final View v) {
        v.measure(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? CardView.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
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

        lineChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context.getApplicationContext(), CurrencyDetailsActivity.class);
                intent.putExtra("currency", currency);
                context.getApplicationContext().startActivity(intent);
            }
        });
    }

    private void updateCardViewInfos(View view, Currency currency, float totalValue, boolean isBalanceHidden)
    {
        ((ImageView) view.findViewById(R.id.currencyIcon))
                .setImageBitmap(currency.getIcon());
        ((TextView) view.findViewById(R.id.currencyNameTextView))
                .setText(currency.getName());
        ((TextView) view.findViewById(R.id.currencySymbolTextView))
                .setText(context.getResources().getString(R.string.currencySymbolPlaceholder, currency.getSymbol()));
        ((TextView) view.findViewById(R.id.currencyOwnedTextView))
                .setText(context.getResources().getString(R.string.currencyBalancePlaceholder, numberConformer(currency.getBalance()), currency.getSymbol()));
        ((TextView) view.findViewById(R.id.currencyValueOwnedTextView))
                .setText(context.getResources().getString(R.string.currencyDollarParenthesisPlaceholder, numberConformer(currency.getValue() * currency.getBalance())));

        ((TextView) view.findViewById(R.id.currencyValueTextView))
                .setText(context.getResources().getString(R.string.currencyDollarPlaceholder, numberConformer(currency.getValue())));
        ((TextView) view.findViewById(R.id.currencyFluctuationPercentageTextView))
                .setText(context.getResources().getString(R.string.currencyPercentagePlaceholder, numberConformer(currency.getDayFluctuationPercentage())));
        ((TextView) view.findViewById(R.id.currencyFluctuationTextView))
                .setText(context.getResources().getString(R.string.currencyDollarParenthesisPlaceholder, numberConformer(currency.getDayFluctuation())));
        ((ImageView) view.findViewById(R.id.detailsArrow))
                .getDrawable().setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));

        if(isBalanceHidden)
        {
            double value = currency.getValue() * currency.getBalance();
            double percentage = value / totalValue * 100;

            view.findViewById(R.id.currencyPortfolioDominance).setVisibility(View.VISIBLE);
            ((ProgressBar) view.findViewById(R.id.currencyPortfolioDominance)).setProgress((int) Math.round(percentage));
            ((ProgressBar) view.findViewById(R.id.currencyPortfolioDominance)).getIndeterminateDrawable().setColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_ATOP);
        }
        else
        {
            view.findViewById(R.id.currencyPortfolioDominance).setVisibility(View.GONE);
        }
    }

    private void collapseView(View view)
    {
        collapse(view.findViewById(R.id.collapsableLayout));
    }

    private void extendView(View view)
    {
        expand(view.findViewById(R.id.collapsableLayout));
        view.findViewById(R.id.LineChartView).invalidate();
    }

    private List<Double> getAxisBorders(Currency currency)
    {
        List<Double> borders = new ArrayList<>();

        List<CurrencyDataChart> dataChartList = currency.getHistoryMinutes();

        borders.add(0, currency.getHistoryMinutes().get(0).getOpen());
        borders.add(1, currency.getHistoryMinutes().get(0).getOpen());

        for(int i = 0; i < dataChartList.size(); i++)
        {
            if(borders.get(0) > dataChartList.get(i).getOpen())
            {
                borders.set(0, dataChartList.get(i).getOpen());
            }

            if(borders.get(1) < dataChartList.get(i).getOpen())
            {
                borders.set(1, dataChartList.get(i).getOpen());
            }
        }

        return borders;
    }

    private void updateColor(View view, Currency currency)
    {
        if(currency.getDayFluctuationPercentage() > 0)
        {
            ((TextView) view.findViewById(R.id.currencyFluctuationPercentageTextView))
                    .setTextColor(context.getResources().getColor(R.color.increase));
            ((TextView) view.findViewById(R.id.currencyFluctuationTextView))
                    .setTextColor(context.getResources().getColor(R.color.increase));
        }
        else
        {
            ((TextView) view.findViewById(R.id.currencyFluctuationPercentageTextView))
                    .setTextColor(context.getResources().getColor(R.color.decrease));
            ((TextView) view.findViewById(R.id.currencyFluctuationTextView))
                    .setTextColor(context.getResources().getColor(R.color.decrease));
        }
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
