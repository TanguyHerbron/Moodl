package com.herbron.moodl.DataManagers.CurrencyData;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.herbron.moodl.DataManagers.InfoAPIManagers.CryptocompareApiManager;
import com.herbron.moodl.DataManagers.PreferencesManager;
import com.herbron.moodl.DataNotifiers.MoodlboxNotifierInterface;
import com.herbron.moodl.MoodlBox;
import com.herbron.moodl.Utils.PlaceholderUtils;
import com.herbron.moodl.R;

import java.util.ArrayList;
import java.util.List;

import static com.herbron.moodl.MoodlBox.collapseH;
import static com.herbron.moodl.MoodlBox.expandH;
import static com.herbron.moodl.MoodlBox.getIconDominantColor;
import static com.herbron.moodl.MoodlBox.numberConformer;

/**
 * Created by Tiji on 12/05/2018.
 */

public class CurrencyCardview extends CardView implements CurrencyInfoUpdateNotifierInterface {

    private Currency currency;
    private Activity parentActivity;
    private Context context;

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
        this.context = context;
    }

    public CurrencyCardview(@NonNull final Context context, final Currency currency, final Activity activity)
    {
        super (context);

        currency.setListener(this);

        this.currency = currency;
        this.parentActivity = activity;
        this.context = context;

        LayoutInflater.from(context).inflate(R.layout.cardview_watchlist, this, true);

        ((LineChart) findViewById(R.id.LineChartView)).setNoDataTextColor(currency.getChartColor());

        setupCardView();

        setOnClickListeners();

        updateCardviewInfos();

        findViewById(R.id.deleteCardWatchlist).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseManager databaseManager = new DatabaseManager(getContext());
                databaseManager.deleteCurrencyFromWatchlist(currency.getSymbol());
                collapseH(CurrencyCardview.this);
            }
        });

        updateColor();

        startIconUpdater();
    }

    public CurrencyCardview(@NonNull final Context context, final Currency currency, Activity activity, boolean isBalanceHidden)
    {
        super(context);

        currency.setListener(this);

        this.currency = currency;
        this.parentActivity = activity;
        this.context = context;

        LayoutInflater.from(context).inflate(R.layout.cardview_currency, this, true);

        ((LineChart) findViewById(R.id.LineChartView)).setNoDataTextColor(currency.getChartColor());

        setupCardView();

        setOnClickListeners();

        updateCardviewInfos();

        updateColor();

        startIconUpdater();
    }

    private void startIconUpdater()
    {
        IconDownloader iconDownloader = new IconDownloader();
        iconDownloader.execute(context, currency);
        iconDownloader.setOnBitmapDownloadedListener(new IconDownloader.OnBitmapDownloadedListener() {
            @Override
            public void onDownloaded(Bitmap icon) {
                currency.setIcon(icon);
                currency.setChartColor(getIconDominantColor(context, icon));

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        updateCurrencyColorRelatedLayouts();
                    }
                });
            }
        });
    }

    private void updateCurrencyColorRelatedLayouts()
    {
        ((ImageView) findViewById(R.id.currencyIcon)).setImageBitmap(currency.getIcon());

        Drawable arrowDrawable = ((ImageView) findViewById(R.id.detailsArrow)).getDrawable();
        arrowDrawable.mutate();
        arrowDrawable.setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));
        arrowDrawable.invalidateSelf();

        Drawable progressDrawable = ((ProgressBar) findViewById(R.id.progressBarLinechart)).getIndeterminateDrawable();
        progressDrawable.mutate();
        progressDrawable.setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));
        progressDrawable.invalidateSelf();

        if(findViewById(R.id.currencyPortfolioDominance) != null)
        {
            Drawable progressBarDrawable = ((ProgressBar) findViewById(R.id.currencyPortfolioDominance)).getProgressDrawable();
            progressBarDrawable.mutate();
            progressBarDrawable.setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));
            progressBarDrawable.invalidateSelf();
        }

        LineChart lineChart = findViewById(R.id.LineChartView);

        if(currency.getHistoryMinutes() != null)
        {
            lineChart.setData(generateData());
            lineChart.invalidate();
        }
    }

    private void setOnClickListeners()
    {
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PreferencesManager preferencesManager = new PreferencesManager(context);

                if (view.findViewById(R.id.collapsableLayout).getVisibility() == View.VISIBLE) {
                    collapseH(view.findViewById(R.id.collapsableLayout));
                } else {
                    view.findViewById(R.id.linearLayoutSubCharts).setVisibility(View.GONE);
                    view.findViewById(R.id.progressBarLinechart).setVisibility(View.VISIBLE);
                    expandH(view.findViewById(R.id.collapsableLayout));

                    if (currency.getHistoryMinutes() == null) {
                        currency.updateHistoryMinutes(context, preferencesManager.getDefaultCurrency());
                    }
                    else
                    {
                        expandH(view.findViewById(R.id.collapsableLayout));
                        view.findViewById(R.id.progressBarLinechart).setVisibility(View.GONE);
                        view.findViewById(R.id.linearLayoutSubCharts).setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        findViewById(R.id.linearLayoutSubCharts).setOnClickListener(detailsClickListener);
        findViewById(R.id.LineChartView).setOnClickListener(detailsClickListener);

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
        lineChart.setData(generateData());
    }

    private void updateCardviewInfos()
    {
        ((TextView) findViewById(R.id.currencyFluctuationTextView))
                .setText(PlaceholderUtils.getValueParenthesisString(numberConformer(currency.getDayFluctuation()), getContext()));
        ((TextView) findViewById(R.id.currencyValueTextView))
                .setText(PlaceholderUtils.getValueString(numberConformer(currency.getValue()), getContext()));

        ((TextView) findViewById(R.id.currencyNameTextView))
                .setText(currency.getName());
        ((TextView) findViewById(R.id.currencySymbolTextView))
                .setText(PlaceholderUtils.getSymbolString(currency.getSymbol(), getContext()));
        ((TextView) findViewById(R.id.currencyFluctuationPercentageTextView))
                .setText(PlaceholderUtils.getPercentageString(numberConformer(currency.getDayFluctuationPercentage()), getContext()));
    }

    public void updateOwnedValues(float totalValue, boolean isBalanceHidden)
    {
        double value = currency.getValue() * currency.getBalance();
        double percentage = value / totalValue * 100;

        ((TextView) findViewById(R.id.currencyValueOwnedTextView))
                .setText(PlaceholderUtils.getValueParenthesisString(numberConformer(currency.getValue() * currency.getBalance()), getContext()));
        ((TextView) findViewById(R.id.currencyOwnedTextView))
                .setText(PlaceholderUtils.getBalanceString(numberConformer(currency.getBalance()), currency.getSymbol(), getContext()));

        ((ProgressBar) findViewById(R.id.currencyPortfolioDominance)).setProgress((int) Math.round(percentage));
        ((TextView) findViewById(R.id.percentageOwnedTextView)).setText(PlaceholderUtils.getPercentageString(numberConformer(percentage), getContext()));

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

    public double getOwnedValue()
    {
        return currency.getValue() * currency.getBalance();
    }

    public double getFluctuation()
    {
        return getOwnedValue() * (currency.getDayFluctuationPercentage() / 100);
    }

    private LineData generateData()
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

    private void updateColor()
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

        View progressView = findViewById(R.id.progressBarLinechart);

        progressView.setVisibility(View.GONE);

        findViewById(R.id.linearLayoutSubCharts).setVisibility(View.VISIBLE);

        if(currency.getHistoryMinutes() != null)
        {
            setupLineChart(currency);
        }
    }

    @Override
    public void onPriceUpdated(Currency currency) {

    }

    private static class IconDownloader extends AsyncTask<Object, Integer, Void> implements MoodlboxNotifierInterface
    {
        private Bitmap icon = null;
        private OnBitmapDownloadedListener onBitmapDownloadedListener;

        public Bitmap getIcon()
        {
            return icon;
        }

        public void setOnBitmapDownloadedListener(OnBitmapDownloadedListener onBitmapDownloadedListener) {
            this.onBitmapDownloadedListener = onBitmapDownloadedListener;
        }

        @Override
        protected Void doInBackground(Object... objects) {
            Context context = (Context) objects[0];
            Currency currency = (Currency) objects[1];

            CryptocompareApiManager cryptocompareApiManager = CryptocompareApiManager.getInstance(context);

            String iconUrl = MoodlBox.getIconUrl(currency.getSymbol(), cryptocompareApiManager);

            if(iconUrl != null)
            {
                MoodlBox.getBitmapFromURL(iconUrl, currency.getSymbol(), context.getResources(), context, this);
            }

            return null;
        }

        @Override
        public void onBitmapDownloaded(Bitmap bitmap) {
            icon = bitmap;
            onBitmapDownloadedListener.onDownloaded(bitmap);
        }

        public interface OnBitmapDownloadedListener {
            void onDownloaded(Bitmap icon);
        }
    }
}
