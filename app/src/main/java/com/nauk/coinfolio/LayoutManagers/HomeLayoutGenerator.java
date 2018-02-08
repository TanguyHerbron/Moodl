package com.nauk.coinfolio.LayoutManagers;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.db.chart.model.ChartSet;
import com.db.chart.model.LineSet;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.view.LineChartView;
import com.nauk.coinfolio.Activities.CurrencyDetailsActivity;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.DataManagers.CurrencyData.CurrencyDataChart;
import com.nauk.coinfolio.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.abs;
import static java.lang.Math.floorDiv;
import static java.lang.Math.floorMod;
import static java.lang.Math.incrementExact;
import static java.sql.Types.NULL;

/**
 * Created by Tiji on 05/01/2018.
 */

public class HomeLayoutGenerator {

    android.content.Context context;

    public HomeLayoutGenerator(Context context)
    {
        this.context = context;
    }

    public View getInfoLayout(final Currency currency, boolean isExtended)
    {

        View view = LayoutInflater.from(context).inflate(R.layout.cardview_currency, null);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*view.animate();
                Intent intent = new Intent(context.getApplicationContext(), CurrencyDetailsActivity.class);
                intent.putExtra("currency", currency);
                context.getApplicationContext().startActivity(intent);*/
                if(view.findViewById(R.id.LineChartView).getVisibility() == View.VISIBLE || view.findViewById(R.id.errorTextView).getVisibility() == View.VISIBLE)
                {
                    collapseView(view);
                }
                else
                {
                    extendView(currency, view);
                }
            }
        });

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

        if(currency.getHistoryMinutes() != null)
        {
            List<Double> borders = getAxisBorders(currency);
            LineChartView chartView = (LineChartView) view.findViewById(R.id.LineChartView);

            chartView.setAxisBorderValues(borders.get(0).floatValue(), borders.get(1).floatValue())
                    .setYLabels(AxisRenderer.LabelPosition.NONE)
                    .setYAxis(false)
                    .setXAxis(false)
                    .setVisibility(View.VISIBLE);

            chartView.addData(generateChartSet(currency));

            chartView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context.getApplicationContext(), CurrencyDetailsActivity.class);
                    intent.putExtra("currency", currency);
                    context.getApplicationContext().startActivity(intent);
                }
            });
        }

        if(isExtended)
        {
            extendView(currency, view);
        }
        else
        {
            collapseView(view);
        }

        updateColor(view, currency);

        return view;
    }

    private void collapseView(View view)
    {
        view.findViewById(R.id.separationLayout).setVisibility(View.GONE);
        view.findViewById(R.id.frameLayoutChart).setVisibility(View.GONE);
        view.findViewById(R.id.LineChartView).setVisibility(View.GONE);
        view.findViewById(R.id.errorTextView).setVisibility(View.GONE);
        view.findViewById(R.id.detailsArrow).setVisibility(View.GONE);
    }

    private void extendView(Currency currency, View view)
    {
        view.findViewById(R.id.separationLayout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.detailsArrow).setVisibility(View.VISIBLE);
        view.findViewById(R.id.frameLayoutChart).setVisibility(View.VISIBLE);

        if(currency.getHistoryMinutes() != null)
        {
            ((LineChartView) view.findViewById(R.id.LineChartView)).setVisibility(View.VISIBLE);
            ((LineChartView) view.findViewById(R.id.LineChartView)).show();
            view.findViewById(R.id.errorTextView).setVisibility(View.GONE);
        }
        else
        {
            view.findViewById(R.id.LineChartView).setVisibility(View.GONE);

            view.findViewById(R.id.errorTextView).setVisibility(View.VISIBLE);
        }

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

    private ChartSet generateChartSet(Currency currency)
    {
        List<CurrencyDataChart> dataChartList = currency.getHistoryMinutes();
        LineSet lineSet = new LineSet();
        int counter = 0;
        Calendar calendar = Calendar.getInstance(Locale.FRANCE);
        String hour;
        String minute;

        for(int i = 0; i < dataChartList.size(); i+=10)
        {
            if(counter == 30)
            {
                calendar.setTimeInMillis(dataChartList.get(i).getTimestamp()*1000);

                hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                minute = String.valueOf(calendar.get(Calendar.MINUTE));

                if(hour.length() < 2)
                {
                    hour = "0" + hour;
                }

                if(minute.length() < 2)
                {
                    minute = "0" + minute;
                }

                lineSet.addPoint(hour + ":" + minute, (float) dataChartList.get(i).getOpen());
                counter = 0;
            }
            else
            {
                counter++;
                lineSet.addPoint("", (float) dataChartList.get(i).getOpen());
            }


        }

        lineSet.setSmooth(true);
        lineSet.setThickness(4);
        lineSet.setFill(getColorWithAplha(currency.getChartColor(), 0.5f));
        lineSet.setColor(currency.getChartColor());

        return lineSet;
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
            str = String.format("%.2f", number);
        }
        else
        {
            str = String.format("%.4f", number);
        }

        return str;
    }
}
