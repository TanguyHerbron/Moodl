package com.herbron.moodl.DataManagers.CurrencyData;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.herbron.moodl.Activities.CurrencyDetailsActivity;
import com.herbron.moodl.CurrencyInfoUpdateNotifierInterface;
import com.herbron.moodl.DataManagers.DatabaseManager;
import com.herbron.moodl.DataManagers.PreferencesManager;
import com.herbron.moodl.MoodlBox;
import com.herbron.moodl.PlaceholderManager;
import com.herbron.moodl.R;

import java.util.ArrayList;
import java.util.List;

import static com.herbron.moodl.MoodlBox.collapseH;
import static com.herbron.moodl.MoodlBox.expandH;
import static com.herbron.moodl.MoodlBox.numberConformer;

/**
 * Created by Tiji on 12/05/2018.
 */

public class CurrencyCardview extends CardView implements CurrencyInfoUpdateNotifierInterface {

    private Currency currency;
    private Activity parentActivity;

    private OnClickListener detailsClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(parentActivity, CurrencyDetailsActivity.class);
            intent.putExtra(getContext().getString(R.string.currency), currency);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(parentActivity, findViewById(R.id.LineChartView), "chart");
                parentActivity.startActivity(intent, activityOptions.toBundle());
            }
            else {
                parentActivity.startActivity(intent);
            }
        }
    };

    public CurrencyCardview(@NonNull Context context) {
        super(context);
    }

    public CurrencyCardview(@NonNull final Context context, final Currency currency, final Activity activity)
    {
        super (context);

        currency.setListener(this);

        this.currency = currency;
        this.parentActivity = activity;

        LayoutInflater.from(context).inflate(R.layout.cardview_watchlist, this, true);

        ((LineChart) findViewById(R.id.LineChartView)).setNoDataTextColor(currency.getChartColor());

        setupCardView();

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PreferencesManager preferencesManager = new PreferencesManager(context);

                if (view.findViewById(R.id.collapsableLayout).getVisibility() == View.VISIBLE) {
                    collapseH(view.findViewById(R.id.collapsableLayout));
                } else {
                    view.findViewById(R.id.linearLayoutSubCharts).setVisibility(View.GONE);
                    view.findViewById(R.id.progressBarLinechartWatchlist).setVisibility(View.VISIBLE);
                    expandH(view.findViewById(R.id.collapsableLayout));

                    if (currency.getHistoryMinutes() == null) {
                        currency.updateHistoryMinutes(context, preferencesManager.getDefaultCurrency());
                    }
                    else
                    {
                        expandH(view.findViewById(R.id.collapsableLayout));
                        view.findViewById(R.id.progressBarLinechartWatchlist).setVisibility(View.GONE);
                        view.findViewById(R.id.linearLayoutSubCharts).setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        updateCardViewInfos(currency);

        findViewById(R.id.deleteCardWatchlist).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseManager databaseManager = new DatabaseManager(getContext());
                databaseManager.deleteCurrencyFromWatchlist(currency.getSymbol());
                collapseH(CurrencyCardview.this);
            }
        });

        findViewById(R.id.linearLayoutSubCharts).setOnClickListener(detailsClickListener);
        findViewById(R.id.LineChartView).setOnClickListener(detailsClickListener);

        updateColor(currency);
    }

    public CurrencyCardview(@NonNull final Context context, final Currency currency, Activity activity, float totalValue, boolean isBalanceHidden)
    {
        super(context);

        currency.setListener(this);

        this.currency = currency;
        this.parentActivity = activity;

        LayoutInflater.from(context).inflate(R.layout.cardview_currency, this, true);

        ((LineChart) findViewById(R.id.LineChartView)).setNoDataTextColor(currency.getChartColor());

        setupCardView();

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PreferencesManager preferencesManager = new PreferencesManager(context);

                if (view.findViewById(R.id.collapsableLayout).getVisibility() == View.VISIBLE) {
                    collapseH(view.findViewById(R.id.collapsableLayout));
                } else {
                    view.findViewById(R.id.linearLayoutSubCharts).setVisibility(View.GONE);
                    view.findViewById(R.id.progressBarLinechartSummary).setVisibility(View.VISIBLE);
                    expandH(view.findViewById(R.id.collapsableLayout));

                    if (currency.getHistoryMinutes() == null) {
                        currency.updateHistoryMinutes(context, preferencesManager.getDefaultCurrency());
                    }
                    else
                    {
                        expandH(view.findViewById(R.id.collapsableLayout));
                        view.findViewById(R.id.progressBarLinechartSummary).setVisibility(View.GONE);
                        view.findViewById(R.id.linearLayoutSubCharts).setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        updateCardViewInfos(currency, totalValue, isBalanceHidden);

        findViewById(R.id.linearLayoutSubCharts).setOnClickListener(detailsClickListener);
        findViewById(R.id.LineChartView).setOnClickListener(detailsClickListener);

        updateColor(currency);
    }

    public Currency getCurrency()
    {
        return currency;
    }

    private void setupCardView()
    {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.MarginLayoutParams.MATCH_PARENT, LinearLayout.MarginLayoutParams.WRAP_CONTENT);
        int margin = (int) MoodlBox.convertDpToPx(10, getResources());
        layoutParams.setMargins(margin, margin, margin, 0);

        setLayoutParams(layoutParams);

        setRadius(MoodlBox.convertDpToPx(2, getResources()));

        setClickable(false);
        setFocusable(false);

        setCardBackgroundColor(MoodlBox.getColor(R.color.white, getContext()));
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
    }

    private void updateCardViewInfos(Currency currency)
    {
        ((TextView) findViewById(R.id.currencyFluctuationTextView))
                .setText(PlaceholderManager.getValueParenthesisString(numberConformer(currency.getDayFluctuation()), getContext()));
        ((TextView) findViewById(R.id.currencyValueTextView))
                .setText(PlaceholderManager.getValueString(numberConformer(currency.getValue()), getContext()));

        ((ImageView) findViewById(R.id.currencyIcon))
                .setImageBitmap(currency.getIcon());
        ((TextView) findViewById(R.id.currencyNameTextView))
                .setText(currency.getName());
        ((TextView) findViewById(R.id.currencySymbolTextView))
                .setText(PlaceholderManager.getSymbolString(currency.getSymbol(), getContext()));
        ((TextView) findViewById(R.id.currencyFluctuationPercentageTextView))
                .setText(PlaceholderManager.getPercentageString(numberConformer(currency.getDayFluctuationPercentage()), getContext()));

        Drawable arrowDrawable = ((ImageView) findViewById(R.id.detailsArrow)).getDrawable();
        arrowDrawable.mutate();
        arrowDrawable.setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));
        arrowDrawable.invalidateSelf();

        Drawable progressDrawable = ((ProgressBar) findViewById(R.id.progressBarLinechartWatchlist)).getIndeterminateDrawable();
        progressDrawable.mutate();
        progressDrawable.setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));
        progressDrawable.invalidateSelf();
    }

    private void updateCardViewInfos(Currency currency, float totalValue, boolean isBalanceHidden)
    {
        double value = currency.getValue() * currency.getBalance();
        double percentage = value / totalValue * 100;

        ((TextView) findViewById(R.id.currencyValueOwnedTextView))
                .setText(PlaceholderManager.getValueParenthesisString(numberConformer(currency.getValue() * currency.getBalance()), getContext()));
        ((TextView) findViewById(R.id.currencyFluctuationTextView))
                .setText(PlaceholderManager.getValueParenthesisString(numberConformer(currency.getDayFluctuation()), getContext()));
        ((TextView) findViewById(R.id.currencyValueTextView))
                .setText(PlaceholderManager.getValueString(numberConformer(currency.getValue()), getContext()));

        ((ImageView) findViewById(R.id.currencyIcon))
                .setImageBitmap(currency.getIcon());
        ((TextView) findViewById(R.id.currencyNameTextView))
                .setText(currency.getName());
        ((TextView) findViewById(R.id.currencySymbolTextView))
                .setText(PlaceholderManager.getSymbolString(currency.getSymbol(), getContext()));
        ((TextView) findViewById(R.id.currencyOwnedTextView))
                .setText(PlaceholderManager.getBalanceString(numberConformer(currency.getBalance()), currency.getSymbol(), getContext()));
        ((TextView) findViewById(R.id.currencyFluctuationPercentageTextView))
                .setText(PlaceholderManager.getPercentageString(numberConformer(currency.getDayFluctuationPercentage()), getContext()));

        Drawable arrowDrawable = ((ImageView) findViewById(R.id.detailsArrow)).getDrawable();
        arrowDrawable.mutate();
        arrowDrawable.setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));
        arrowDrawable.invalidateSelf();

        Drawable progressDrawable = ((ProgressBar) findViewById(R.id.progressBarLinechartSummary)).getIndeterminateDrawable();
        progressDrawable.mutate();
        progressDrawable.setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));
        progressDrawable.invalidateSelf();

        Drawable progressBarDrawable = ((ProgressBar) findViewById(R.id.currencyPortfolioDominance)).getProgressDrawable();
        progressBarDrawable.mutate();
        progressBarDrawable.setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));
        progressBarDrawable.invalidateSelf();

        ((ProgressBar) findViewById(R.id.currencyPortfolioDominance)).setProgress((int) Math.round(percentage));
        ((TextView) findViewById(R.id.percentageOwnedTextView)).setText(PlaceholderManager.getPercentageString(numberConformer(percentage), getContext()));

        if(isBalanceHidden)
        {
            findViewById(R.id.currencyPortfolioDominance).setVisibility(View.VISIBLE);
            findViewById(R.id.percentageOwnedTextView).setVisibility(View.VISIBLE);
            findViewById(R.id.currencyOwnedInfoLayout).setVisibility(View.GONE);
        }
        else
        {
            findViewById(R.id.currencyPortfolioDominance).setVisibility(View.INVISIBLE);
            findViewById(R.id.percentageOwnedTextView).setVisibility(View.GONE);
            findViewById(R.id.currencyOwnedInfoLayout).setVisibility(View.VISIBLE);
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

        dataSet = new LineDataSet(values, "");
        dataSet.setDrawIcons(false);
        dataSet.setColor(currency.getChartColor());
        dataSet.setFillColor(getColorWithAplha(currency.getChartColor(), 0.5f));
        dataSet.setLineWidth(1);
        dataSet.setDrawFilled(true);
        dataSet.setFormLineWidth(1);
        dataSet.setFormSize(15);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setHighlightEnabled(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        return new LineData(dataSet);
    }

    private void updateColor(Currency currency)
    {
        if(currency.getDayFluctuationPercentage() >= 0)
        {
            ((TextView) findViewById(R.id.currencyFluctuationPercentageTextView))
                    .setTextColor(ContextCompat.getColor(getContext(), R.color.increase));
            ((TextView) findViewById(R.id.currencyFluctuationTextView))
                    .setTextColor(ContextCompat.getColor(getContext(), R.color.increase));
        }
        else
        {
            ((TextView) findViewById(R.id.currencyFluctuationPercentageTextView))
                    .setTextColor(ContextCompat.getColor(getContext(), R.color.decrease));
            ((TextView) findViewById(R.id.currencyFluctuationTextView))
                    .setTextColor(ContextCompat.getColor(getContext(), R.color.decrease));
        }
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

    @Override
    public void onTimestampPriceUpdated(String price) {

    }

    @Override
    public void onHistoryDataUpdated() {

        View progressWatchlistView = findViewById(R.id.progressBarLinechartWatchlist);
        View progressSummaryView = findViewById(R.id.progressBarLinechartSummary);

        if(progressWatchlistView != null)
        {
            progressWatchlistView.setVisibility(View.GONE);
        }

        if(progressSummaryView != null)
        {
            progressSummaryView.setVisibility(View.GONE);
        }

        findViewById(R.id.linearLayoutSubCharts).setVisibility(View.VISIBLE);

        if(currency.getHistoryMinutes() != null)
        {
            setupLineChart(currency);
        }
    }

    @Override
    public void onPriceUpdated(Currency currency) {

    }
}
