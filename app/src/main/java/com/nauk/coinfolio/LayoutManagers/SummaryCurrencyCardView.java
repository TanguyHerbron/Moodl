package com.nauk.coinfolio.LayoutManagers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.nauk.coinfolio.Activities.CurrencyDetailsActivity;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.DataManagers.CurrencyData.CurrencyDataChart;
import com.nauk.coinfolio.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.abs;

/**
 * Created by Tiji on 06/04/2018.
 */

public class SummaryCurrencyCardView extends CardView implements View.OnClickListener {

    public SummaryCurrencyCardView(Context context)
    {
        this(context, null, false, 0, false);
    }

    public SummaryCurrencyCardView(Context context, final Currency currency, boolean isExtended, float totalValue, boolean isBalanceHidden)
    {
        super(context, null, 0);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.cardview_currency, this, true);

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("coinfolio", "Clicked");
                if(view.findViewById(R.id.collapsableLayout).getVisibility() == View.VISIBLE)
                {
                    collapseView();
                }
                else
                {
                    extendView();
                }
            }
        });

        //updateCardViewInfos(currency, totalValue, isBalanceHidden);
        updateCardViewInfos(currency, totalValue, true);

        setupLineChart(currency);

        if(isExtended)
        {
            extendView();
        }
        else
        {
            collapseView();
        }

        updateColor(currency);
    }

    @Override
    public void onClick(View view)
    {
        Log.d("coinfolio", "Clicked");

        if(view.findViewById(R.id.collapsableLayout).getVisibility() == View.VISIBLE)
        {
            collapseView();
        }
        else
        {
            extendView();
        }
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

    private void setupLineChart(final Currency currency)
    {
        LineChart lineChart = findViewById(R.id.LineChartView);

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
                Intent intent = new Intent(getContext(), CurrencyDetailsActivity.class);
                intent.putExtra("currency", currency);
                getContext().startActivity(intent);
            }
        });
    }

    private void updateCardViewInfos(Currency currency, float totalValue, boolean isBalanceHidden)
    {
        ((ImageView) findViewById(R.id.currencyIcon))
                .setImageBitmap(currency.getIcon());
        ((TextView) findViewById(R.id.currencyNameTextView))
                .setText(currency.getName());
        ((TextView) findViewById(R.id.currencySymbolTextView))
                .setText(getResources().getString(R.string.currencySymbolPlaceholder, currency.getSymbol()));
        ((TextView) findViewById(R.id.currencyOwnedTextView))
                .setText(getResources().getString(R.string.currencyBalancePlaceholder, numberConformer(currency.getBalance()), currency.getSymbol()));
        ((TextView) findViewById(R.id.currencyValueOwnedTextView))
                .setText(getResources().getString(R.string.currencyDollarParenthesisPlaceholder, numberConformer(currency.getValue() * currency.getBalance())));

        ((TextView) findViewById(R.id.currencyValueTextView))
                .setText(getResources().getString(R.string.currencyDollarPlaceholder, numberConformer(currency.getValue())));
        ((TextView) findViewById(R.id.currencyFluctuationPercentageTextView))
                .setText(getResources().getString(R.string.currencyPercentagePlaceholder, numberConformer(currency.getDayFluctuationPercentage())));
        ((TextView) findViewById(R.id.currencyFluctuationTextView))
                .setText(getResources().getString(R.string.currencyDollarParenthesisPlaceholder, numberConformer(currency.getDayFluctuation())));
        ((ImageView) findViewById(R.id.detailsArrow))
                .getDrawable().setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));

        if(isBalanceHidden)
        {
            double value = currency.getValue() * currency.getBalance();
            double percentage = value / totalValue * 100;

            findViewById(R.id.currencyPortfolioDominance).setVisibility(View.VISIBLE);
            ((ProgressBar) findViewById(R.id.currencyPortfolioDominance)).setProgress((int) Math.round(percentage));
            ((ProgressBar) findViewById(R.id.currencyPortfolioDominance)).getIndeterminateDrawable().setColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_ATOP);
        }
        else
        {
            findViewById(R.id.currencyPortfolioDominance).setVisibility(View.GONE);
        }
    }

    private void collapseView()
    {
        collapse(findViewById(R.id.collapsableLayout));
    }

    private void extendView()
    {
        expand(findViewById(R.id.collapsableLayout));
        findViewById(R.id.LineChartView).invalidate();
    }

    private void updateColor(Currency currency)
    {
        if(currency.getDayFluctuationPercentage() > 0)
        {
            ((TextView) findViewById(R.id.currencyFluctuationPercentageTextView))
                    .setTextColor(getResources().getColor(R.color.increase));
            ((TextView) findViewById(R.id.currencyFluctuationTextView))
                    .setTextColor(getResources().getColor(R.color.increase));
        }
        else
        {
            ((TextView) findViewById(R.id.currencyFluctuationPercentageTextView))
                    .setTextColor(getResources().getColor(R.color.decrease));
            ((TextView) findViewById(R.id.currencyFluctuationTextView))
                    .setTextColor(getResources().getColor(R.color.decrease));
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
