package com.nauk.coinfolio.Activities.HomeActivityFragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.nauk.coinfolio.Activities.CurrencyDetailsActivity;
import com.nauk.coinfolio.Activities.CurrencySelectionActivity;
import com.nauk.coinfolio.Activities.HomeActivity;
import com.nauk.coinfolio.Activities.SettingsActivity;
import com.nauk.coinfolio.DataManagers.BalanceManager;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.DataManagers.CurrencyData.CurrencyDataChart;
import com.nauk.coinfolio.DataManagers.CurrencyData.CurrencyDetailsList;
import com.nauk.coinfolio.DataManagers.PreferencesManager;
import com.nauk.coinfolio.DataManagers.WatchlistManager;
import com.nauk.coinfolio.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.abs;

/**
 * Created by Tiji on 13/04/2018.
 */

public class Watchlist extends Fragment {

    private WatchlistManager watchlistManager;
    private View view;
    private int watchlistCounter;
    private CurrencyDetailsList currencyDetailsList;
    private SwipeRefreshLayout refreshLayout;
    private long lastTimestamp;
    private PreferencesManager preferencesManager;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_watchlist_homeactivity, container, false);

        refreshLayout = view.findViewById(R.id.swiperefreshwatchlist);
        currencyDetailsList = new CurrencyDetailsList(getContext());
        preferencesManager = new PreferencesManager(getContext());

        lastTimestamp = 0;

        watchlistManager = new WatchlistManager(getContext());

        updateWatchlist(true);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateWatchlist(false);
            }
        });
        toolbar = view.findViewById(R.id.toolbar);

        Button addWatchlistButton = view.findViewById(R.id.buttonAddWatchlist);
        addWatchlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent selectionIntent = new Intent(getActivity(), CurrencySelectionActivity.class);
                selectionIntent.putExtra("isWatchList", true);
                startActivity(selectionIntent);
            }
        });

        ImageButton settingsButton = view.findViewById(R.id.settings_button);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingIntent);
            }
        });

        return view;
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

    private static void expand(final View v) {
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

    private static void collapse(final View v) {
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


    @Override
    public void onResume()
    {
        super.onResume();

        updateWatchlist(preferencesManager.mustUpdateWatchlist());
    }

    private void updateWatchlist(boolean mustUpdate)
    {
        if(System.currentTimeMillis()/1000 - lastTimestamp > 60 || mustUpdate)
        {
            if(!refreshLayout.isRefreshing())
            {
                refreshLayout.setRefreshing(true);
            }

            lastTimestamp = System.currentTimeMillis()/1000;

            watchlistManager.updateWatchlist();

            currencyDetailsList.update(new BalanceManager.IconCallBack() {
                @Override
                public void onSuccess() {
                    WatchlistUpdater watchlistUpdater = new WatchlistUpdater();
                    watchlistUpdater.execute();
                }
            });
        }
        else
        {
            if(refreshLayout.isRefreshing())
            {
                refreshLayout.setRefreshing(false);
            }
        }
    }

    private void countWatchlist()
    {
        watchlistCounter++;

        if(watchlistCounter >= watchlistManager.getWatchlist().size())
        {
            final List<View> watchlistViews = new ArrayList<View>();

            ((LinearLayout) view.findViewById(R.id.linearLayoutWatchlist)).removeAllViews();

            Runnable newRunnable = new Runnable() {
                @Override
                public void run() {
                    for(final Currency currency : watchlistManager.getWatchlist())
                    {
                        View card = LayoutInflater.from(getContext()).inflate(R.layout.cardview_watchlist, null);

                        ((TextView) card.findViewById(R.id.currencyFluctuationPercentageTextView)).setText(getResources().getString(R.string.currencyPercentagePlaceholder, numberConformer(currency.getDayFluctuationPercentage())));
                        ((TextView) card.findViewById(R.id.currencyFluctuationTextView)).setText(getResources().getString(R.string.currencyDollarParenthesisPlaceholder, numberConformer(currency.getDayFluctuation())));
                        ((TextView) card.findViewById(R.id.currencyNameTextView)).setText(currency.getName());
                        ((TextView) card.findViewById(R.id.currencySymbolTextView)).setText(getResources().getString(R.string.currencySymbolPlaceholder, currency.getSymbol()));
                        ((ImageView) card.findViewById(R.id.currencyIcon)).setImageBitmap(currency.getIcon());
                        ((TextView) card.findViewById(R.id.currencyValueTextView)).setText(getResources().getString(R.string.currencyDollarPlaceholder, numberConformer(currency.getValue())));

                        Drawable arrowDrawable = ((ImageView) card.findViewById(R.id.detailsArrow)).getDrawable();
                        arrowDrawable.mutate();
                        arrowDrawable.setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));
                        arrowDrawable.invalidateSelf();

                        updateColor(card, currency);

                        card.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View view) {
                                if(view.findViewById(R.id.collapsableLayout).getVisibility() == View.VISIBLE)
                                {
                                    collapseView(view);
                                }
                                else
                                {
                                    if (currency.getHistoryMinutes() == null) {
                                        currency.updateHistoryMinutes(getActivity(), new Currency.CurrencyCallBack() {
                                            @Override
                                            public void onSuccess(Currency currency) {
                                                extendView(view);
                                                setupLineChart(view, currency);
                                            }
                                        });
                                    }
                                    else
                                    {
                                        extendView(view);
                                    }
                                }
                            }
                        });

                        card.findViewById(R.id.LineChartView).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getActivity(), CurrencyDetailsActivity.class);
                                intent.putExtra("currency", currency);
                                getActivity().getApplicationContext().startActivity(intent);
                            }
                        });

                        if(currency.getHistoryMinutes() != null)
                        {
                            setupLineChart(card, currency);
                        }

                        watchlistViews.add(card);
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for(int i = 0; i < watchlistViews.size(); i++)
                            {
                                ((LinearLayout) view.findViewById(R.id.linearLayoutWatchlist)).addView(watchlistViews.get(i), 0);
                            }
                        }
                    });
                }
            };

            newRunnable.run();

            if(refreshLayout.isRefreshing())
            {
                refreshLayout.setRefreshing(false);
            }
        }
    }
    private LineData generateData(Currency currency)
    {
        LineDataSet dataSet;
        List<CurrencyDataChart> dataChartList = currency.getHistoryMinutes();
        ArrayList<Entry> values = new ArrayList<>();

        Log.d("coinfolio", "Generating data for " + currency.getSymbol());
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
    }

    private void updateColor(View card, Currency currency)
    {
        if(currency.getDayFluctuation() > 0)
        {
            ((TextView) card.findViewById(R.id.currencyFluctuationPercentageTextView)).setTextColor(getResources().getColor(R.color.increase));
            ((TextView) card.findViewById(R.id.currencyFluctuationTextView)).setTextColor(getResources().getColor(R.color.increase));
        }
        else
        {
            ((TextView) card.findViewById(R.id.currencyFluctuationPercentageTextView)).setTextColor(getResources().getColor(R.color.decrease));
            ((TextView) card.findViewById(R.id.currencyFluctuationTextView)).setTextColor(getResources().getColor(R.color.decrease));
        }
    }

    private String getIconUrl(String symbol)
    {
        String url;

        try {
            JSONObject jsonObject = new JSONObject(currencyDetailsList.getCoinInfosHashmap().get(symbol));
            url = "https://www.cryptocompare.com" + jsonObject.getString("ImageUrl") + "?width=50";
        } catch (NullPointerException e) {
            Log.d(getContext().getResources().getString(R.string.debug), symbol + " has no icon URL");
            url = null;
        } catch (JSONException e) {
            Log.d(getContext().getResources().getString(R.string.debug), "Url parsing error for " + symbol);
            url = null;
        }

        return url;
    }

    private void getBitmapFromURL(String src, HomeActivity.IconCallBack callBack) {
        Bitmap result;

        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            result = BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            result = BitmapFactory.decodeResource(this.getResources(),
                    R.mipmap.icon_coinfolio);
            result = Bitmap.createScaledBitmap(result, 50, 50, false);
        }

        callBack.onSuccess(result);
    }

    private void updateChartColor(Currency currency)
    {
        if(currency.getIcon() != null)
        {
            Palette.Builder builder = Palette.from(currency.getIcon());

            currency.setChartColor(builder.generate().getDominantColor(0));
        }
        else
        {
            currency.setChartColor(12369084);
        }
    }

    private class WatchlistUpdater extends AsyncTask<Void, Integer, Void>
    {
        @Override
        protected void onPreExecute()
        {
            watchlistCounter = 0;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for(final Currency currency : watchlistManager.getWatchlist())
            {
                currency.updatePrice(getActivity(), new Currency.CurrencyCallBack() {
                    @Override
                    public void onSuccess(final Currency sucessCurrency) {
                        if(getIconUrl(sucessCurrency.getSymbol()) != null)
                        {
                            getBitmapFromURL(getIconUrl(sucessCurrency.getSymbol()), new HomeActivity.IconCallBack() {
                                @Override
                                public void onSuccess(Bitmap bitmapIcon) {
                                    sucessCurrency.setIcon(bitmapIcon);
                                    updateChartColor(currency);
                                    countWatchlist();
                                }
                            });
                        }
                    }
                });
            }
            return null;
        }
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
