package com.nauk.coinfolio.LayoutManagers;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.view.LineChartView;
import com.nauk.coinfolio.Activities.CurrencyDetailsActivity;
import com.nauk.coinfolio.Activities.HomeActivity;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.DataManagers.CurrencyData.CurrencyDataChart;
import com.nauk.coinfolio.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.abs;
import static java.lang.Math.floorDiv;
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

    public CardView getInfoLayout(final Currency currency, int chartColor)
    //public CardView getInfoLayout(int index)
    {
        CardView mainCard = new CardView(context);
        LinearLayout mainLinear = new LinearLayout(context);
        View separationLine = new View(context);
        LinearLayout chartLayout = new LinearLayout(context);
        LinearLayout infoLayout = new LinearLayout(context);
        LinearLayout separatorLayout = new LinearLayout(context);
        TextView separatorTextView = new TextView(context);

        StateListAnimator stateListAnimator = AnimatorInflater.loadStateListAnimator(context, R.drawable.cardview_animator);
        mainCard.setStateListAnimator(stateListAnimator);

        //mainCard.setTag("full." + index);

        /*int[] attrs = new int[] { R.attr.selectableItemBackground };
        TypedArray ta = context.obtainStyledAttributes(attrs);
        Drawable drawable = ta.getDrawable(0);
        ta.recycle();

        mainCard.setBackground(drawable);*/

        mainCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //switchingView(view);
                view.animate();
                Intent intent = new Intent(context.getApplicationContext(), CurrencyDetailsActivity.class);
                intent.putExtra("symbol", currency.getSymbol());
                context.getApplicationContext().startActivity(intent);
            }
        });

        mainCard.setClickable(true);

        CardView.LayoutParams paramsCard = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        paramsCard.setMargins(10, 10, 10, 30);

        LinearLayout.LayoutParams paramsInfo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsInfo.setMargins(10, 10, 10, 10);

        chartLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams separatorParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3);
        separatorParams.gravity = Gravity.CENTER_VERTICAL;
        separatorParams.setMargins(10, 0, 10, 0);

        separationLine.setLayoutParams(separatorParams);
        separationLine.setBackgroundColor(context.getResources().getColor(R.color.separationLine));

        LinearLayout.LayoutParams separatorLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        separatorLayoutParams.setMargins(10, 0, 10, 0);
        separatorLayout.setLayoutParams(separatorLayoutParams);

        separatorTextView.setText("Day history");
        separatorTextView.setTextSize(context.getResources().getDimension(R.dimen.secondaryText));

        separatorLayout.addView(separatorTextView);
        separatorLayout.addView(separationLine);
        separatorLayout.setTag("separator_layout");

        infoLayout.setLayoutParams(paramsInfo);
        infoLayout.setOrientation(LinearLayout.VERTICAL);

        mainLinear.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        mainLinear.setOrientation(LinearLayout.VERTICAL);
        mainLinear.setTag("mainLinear");

        mainCard.setLayoutParams(paramsCard);
        mainCard.setRadius(8);

        //infoLayout.addView(topLayoutGenerator(currencyList.get(index).getName(), currencyList.get(index).getSymbol(), currencyList.get(index).getValue(), currencyList.get(index).getIcon()));
        //infoLayout.addView(bottomLayoutGenerator(currencyList.get(index).getSymbol(), currencyList.get(index).getBalance(), currencyList.get(index).getValue() * currencyList.get(index).getBalance(), currencyList.get(index).getDayFluctuationPercentage(), currencyList.get(index).getDayFluctuation()));

        infoLayout.addView(topLayoutGenerator(currency.getName(), currency.getSymbol(), currency.getValue(), currency.getIcon()));
        infoLayout.addView(bottomLayoutGenerator(currency.getSymbol(), currency.getBalance(), currency.getValue() * currency.getBalance(), currency.getDayFluctuationPercentage(), currency.getDayFluctuation()));

        mainLinear.addView(infoLayout);

        mainLinear.addView(separatorLayout);

        mainLinear.addView(generateChart(currency, chartLayout, chartColor));

        mainLinear.setClickable(false);

        mainCard.addView(mainLinear);

        return mainCard;
    }

    /*public void resetCurrencyList()
    {
        currencyList = new ArrayList<>();
    }

    public void addCurrencyToList(Currency currency)
    {
        currencyList.add(currency);
    }

    public void setCurrencyList(List<Currency> currencyList)
    {
        this.currencyList = currencyList;
    }

    private void switchingView(View view)
    {
        String[] tag = view.getTag().toString().split("\\.");

        Log.d("coinfolio", "Design : " + currencyList.get(Integer.parseInt(tag[1])).getSymbol());

        if(tag[0].equals("full"))
        {
            LinearLayout mLayout = view.findViewWithTag("mainLinear");
            mLayout.removeViewAt(mLayout.getChildCount()-1);
            mLayout.addView(generateChart(currencyList.get(Integer.parseInt(tag[1])), chartLayout, currencyList.get(Integer.parseInt(tag[1])).getChartColor()));
            Log.d("coinfolio", "Data : " + currencyList.get(Integer.parseInt(tag[1])).getSymbol() + " " + currencyList.get(Integer.parseInt(tag[1])).getChartColor());
            view.setTag("half." + tag[1]);
            mLayout.findViewWithTag("separator_layout").setVisibility(View.VISIBLE);
        }
        else
        {
            LinearLayout mLayout = view.findViewWithTag("mainLinear");
            mLayout.findViewWithTag("chart_layout").setVisibility(View.GONE);
            mLayout.findViewWithTag("separator_layout").setVisibility(View.GONE);
            view.setTag("full." + tag[1]);
        }
    }*/

    private View generateChart(Currency currency, LinearLayout chartLayout, int chartColor)
    {
        View toReturn;

        if(currency.getDayPriceHistory() != null)
        {
            LineChartView lineChartView = chartGenerator(currency.getDayPriceHistory(), chartColor);
            chartLayout.setTag("chart_layout");
            chartLayout.addView(lineChartView);
            lineChartView.show();
            toReturn = chartLayout;
            //mainLinear.addView(chartLayout);
        }
        else
        {
            TextView errorTextView = new TextView(context);
            errorTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 500));
            errorTextView.setText("Error");
            errorTextView.setTag("chart_layout");
            errorTextView.setGravity(Gravity.CENTER);

            toReturn = errorTextView;
            //mainLinear.addView(errorTextView);
        }

        return toReturn;
    }

    private LinearLayout topLayoutGenerator(String name, String symbol, double value, Bitmap logo)
    {
        LinearLayout mainLayout = new LinearLayout(context);
        TextView nameTextView = new TextView(context);
        TextView symbolTextView = new TextView(context);
        TextView valueTextView = new TextView(context);
        ImageView currencyIcon = new ImageView(context);

        mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        iconParams.setMargins(5, 5, 5, 5);
        currencyIcon.setLayoutParams(iconParams);
        currencyIcon.setImageBitmap(logo);

        nameTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        nameTextView.setTextSize(context.getResources().getDimension(R.dimen.mainText));
        nameTextView.setTextColor(context.getResources().getColor(R.color.mainTextViewColor));
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setText(name);

        symbolTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        symbolTextView.setTextSize(context.getResources().getDimension(R.dimen.secondaryText));
        symbolTextView.setTextColor(context.getResources().getColor(R.color.secondaryTextViewColor));
        symbolTextView.setGravity(Gravity.LEFT);
        symbolTextView.setText(" (" + symbol + ")");

        valueTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        valueTextView.setTextSize(context.getResources().getDimension(R.dimen.mainText));
        valueTextView.setTextColor(context.getResources().getColor(R.color.secondaryTextViewColor));
        valueTextView.setGravity(Gravity.RIGHT);

        if(value != NULL)
        {
            valueTextView.setText("US$" + value);
        }
        else
        {
            valueTextView.setText("ERROR");
        }

        mainLayout.addView(currencyIcon);
        mainLayout.addView(nameTextView);
        mainLayout.addView(symbolTextView);
        mainLayout.addView(valueTextView);

        return mainLayout;
    }

    private LinearLayout bottomLayoutGenerator(String symbol, double owned, double value, float percentageFluctuation, double fluctuation)
    {
        LinearLayout mainLayout = new LinearLayout(context);
        LinearLayout secondaryLayout = new LinearLayout(context);
        TextView ownedTextView = new TextView(context);
        TextView valueTextView = new TextView(context);
        TextView percentageFluctuationTextView = new TextView(context);
        TextView fluctuationTextView = new TextView(context);

        mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        secondaryLayout.setOrientation(LinearLayout.HORIZONTAL);
        secondaryLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        secondaryLayout.setGravity(Gravity.RIGHT);

        ownedTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ownedTextView.setTextSize(context.getResources().getDimension(R.dimen.mainText));
        ownedTextView.setTextColor(context.getResources().getColor(R.color.mainTextViewColor));
        ownedTextView.setGravity(Gravity.LEFT);
        ownedTextView.setText(numberConformer(owned) + symbol);

        valueTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        valueTextView.setTextSize(context.getResources().getDimension(R.dimen.secondaryText));
        valueTextView.setTextColor(context.getResources().getColor(R.color.secondaryTextViewColor));
        valueTextView.setGravity(Gravity.LEFT);
        valueTextView.setText(" (" + numberConformer(value) + "$)");

        percentageFluctuationTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        percentageFluctuationTextView.setTextSize(context.getResources().getDimension(R.dimen.mainText));
        if(percentageFluctuation > 0)
        {
            percentageFluctuationTextView.setTextColor(context.getResources().getColor(R.color.increase));
        }
        else
        {
            percentageFluctuationTextView.setTextColor(context.getResources().getColor(R.color.decrease));
        }
        //percentageFluctuationTextView.setGravity(Gravity.RIGHT);
        percentageFluctuationTextView.setText(numberConformer(percentageFluctuation) + "%");

        fluctuationTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        fluctuationTextView.setTextSize(context.getResources().getDimension(R.dimen.secondaryText));

        if(fluctuation > 0)
        {
            fluctuationTextView.setTextColor(context.getResources().getColor(R.color.increase));
        }
        else
        {
            fluctuationTextView.setTextColor(context.getResources().getColor(R.color.decrease));
        }
        //fluctuationTextView.setGravity(Gravity.RIGHT);
        fluctuationTextView.setText(" (" + numberConformer(fluctuation) + "$)");

        secondaryLayout.addView(percentageFluctuationTextView);
        secondaryLayout.addView(fluctuationTextView);

        mainLayout.addView(ownedTextView);
        mainLayout.addView(valueTextView);
        mainLayout.addView(secondaryLayout);
        //mainLayout.addView(percentageFluctuationTextView);
        //mainLayout.addView(fluctuationTextView);

        return mainLayout;
    }

    private LineChartView chartGenerator(List<CurrencyDataChart> dataChartList, int chartColor)
    {
        LineChartView chartView = new LineChartView(context);
        LineSet lineSet = new LineSet();
        double valMin;
        double valMax;
        int counter = 0;
        Calendar calendar = Calendar.getInstance(Locale.FRANCE);
        String hour;
        String minute;

        valMin = dataChartList.get(0).getOpen();
        valMax = dataChartList.get(0).getOpen();
        for(int i = 1; i < dataChartList.size(); i++)
        {
            if(valMax < dataChartList.get(i).getOpen())
            {
                valMax = dataChartList.get(i).getOpen();
            }

            if(valMin > dataChartList.get(i).getOpen())
            {
                valMin = dataChartList.get(i).getOpen();
            }
        }

        if(valMax == valMin)
        {
            valMin = 0;
            valMax *= 2;
        }

        chartView.setAxisBorderValues((float) valMin, (float) valMax);
        chartView.setYLabels(AxisRenderer.LabelPosition.NONE);
        chartView.setYAxis(false);
        chartView.setXAxis(false);

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
        lineSet.setFill(getColorWithAplha(chartColor, 0.5f));
        lineSet.setColor(chartColor);

        chartView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 500));

        chartView.setTag("Chart");

        chartView.addData(lineSet);

        return chartView;
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
