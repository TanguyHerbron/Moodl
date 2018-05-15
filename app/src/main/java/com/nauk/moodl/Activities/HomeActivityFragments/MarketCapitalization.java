package com.nauk.moodl.Activities.HomeActivityFragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.nauk.moodl.Activities.SettingsActivity;
import com.nauk.moodl.DataManagers.CurrencyData.Currency;
import com.nauk.moodl.DataManagers.MarketCapManager;
import com.nauk.moodl.DataManagers.PreferencesManager;
import com.nauk.moodl.LayoutManagers.CustomViewPager;
import com.nauk.moodl.PlaceholderManager;
import com.nauk.moodl.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.nauk.moodl.MoodlBox.numberConformer;
import static java.lang.Math.abs;

/**
 * Created by Tiji on 13/04/2018.
 */

public class MarketCapitalization extends Fragment {

    private int marketCapCounter;

    private PreferencesManager preferencesManager;
    private MarketCapManager marketCapManager;
    private HashMap<String, Integer> dominantCurrenciesColors;
    private SwipeRefreshLayout refreshLayout;
    private long lastTimestamp;
    private String defaultCurrency;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_marketcap_homeactivity, container, false);

        setupDominantCurrenciesColors();

        preferencesManager = new PreferencesManager(getContext());
        marketCapManager = new MarketCapManager(getContext());

        defaultCurrency = preferencesManager.getDefaultCurrency();
        lastTimestamp = 0;

        setupRefreshLayout();

        setupSettingsButton();

        updateMarketCap(true);

        return view;
    }

    private void setupRefreshLayout()
    {
        refreshLayout = view.findViewById(R.id.swiperefreshmarketcap);

        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateMarketCap(false);
                    }

                }
        );
    }

    private void setupSettingsButton()
    {
        ImageButton settingsButton = view.findViewById(R.id.settings_button);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingIntent);
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if(!defaultCurrency.equals(preferencesManager.getDefaultCurrency()))
        {
            defaultCurrency = preferencesManager.getDefaultCurrency();
            updateMarketCap(true);
        }
        else
        {
            updateMarketCap(false);
        }

    }

    private void setupDominantCurrenciesColors()
    {
        dominantCurrenciesColors = new HashMap<>();

        dominantCurrenciesColors.put("BTC", -489456);
        dominantCurrenciesColors.put("ETH", -13619152);
        dominantCurrenciesColors.put("XRP", -16744256);
        dominantCurrenciesColors.put("BCH", -1011696);
        dominantCurrenciesColors.put("LTC", -4671304);
        dominantCurrenciesColors.put("EOS", -1513240);
        dominantCurrenciesColors.put("ADA", -16773080);
        dominantCurrenciesColors.put("XLM", -11509656);
        dominantCurrenciesColors.put("MIOTA", -1513240);
        dominantCurrenciesColors.put("NEO", -9390048);
        dominantCurrenciesColors.put("XMR", -499712);
        dominantCurrenciesColors.put("DASH", -15175496);
        dominantCurrenciesColors.put("XEM", -7829368);
        dominantCurrenciesColors.put("TRX", -7829360);
        dominantCurrenciesColors.put("ETC", -10448784);
    }

    private void updateMarketCap(boolean mustUpdate)
    {
        if(System.currentTimeMillis() / 1000 - lastTimestamp > 60 || mustUpdate)
        {
            if(!refreshLayout.isRefreshing())
            {
                refreshLayout.setRefreshing(true);
            }

            marketCapCounter = 0;

            lastTimestamp = System.currentTimeMillis() / 1000;

            marketCapManager.updateTopCurrencies(new MarketCapManager.VolleyCallBack() {
                @Override
                public void onSuccess()
                {
                    countCompletedMarketCapRequest();
                }
            }, preferencesManager.getDefaultCurrency());

            marketCapManager.updateMarketCap(new MarketCapManager.VolleyCallBack() {
                @Override
                public void onSuccess() {
                    countCompletedMarketCapRequest();
                }
            }, preferencesManager.getDefaultCurrency());
        }
        else
        {
            if(refreshLayout.isRefreshing())
            {
                refreshLayout.setRefreshing(false);
            }
        }
    }

    private void refreshDisplayedData()
    {
        setupTextViewMarketCap();

        view.findViewById(R.id.progressBarMarketCap).setVisibility(View.GONE);
        view.findViewById(R.id.layoutProgressMarketCap).setVisibility(View.VISIBLE);

        PieData data = new PieData(getMarketDominanceDataSet());
        data.setValueTextSize(10);
        data.setValueFormatter(new PercentFormatter());

        setupPieChart(data);

        if(refreshLayout.isRefreshing())
        {
            refreshLayout.setRefreshing(false);
        }
    }

    private PieDataSet getMarketDominanceDataSet()
    {
        List<PieEntry> entries = new ArrayList<>();
        List<Currency> topCurrencies = marketCapManager.getTopCurrencies();
        ArrayList<Integer> colors = new ArrayList<>();

        float topCurrenciesDominance = 0;

        for(int i = 0; i < topCurrencies.size(); i++)
        {
            PieEntry pieEntry = new PieEntry(topCurrencies.get(i).getDominance(marketCapManager.getMarketCap()), topCurrencies.get(i).getSymbol());

            if(pieEntry.getValue() < 3)
            {
                pieEntry.setLabel("");
            }

            entries.add(pieEntry);
            topCurrenciesDominance += topCurrencies.get(i).getDominance(marketCapManager.getMarketCap());
            colors.add(dominantCurrenciesColors.get(topCurrencies.get(i).getSymbol()));
        }

        entries.add(new PieEntry(100-topCurrenciesDominance, "Others"));
        colors.add(-12369084);

        PieDataSet set = new PieDataSet(entries, "Market Cap Dominance");
        set.setColors(colors);
        set.setSliceSpace(1);
        set.setDrawValues(false);

        return set;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void countCompletedMarketCapRequest()
    {
        marketCapCounter++;

        if(marketCapCounter == 2)
        {
            refreshDisplayedData();
        }
    }

    private void setupPieChart(PieData data)
    {
        PieChart pieChart = view.findViewById(R.id.marketCapPieChart);

        pieChart.setData(data);
        pieChart.setDrawSlicesUnderHole(false);
        pieChart.setUsePercentValues(false);
        pieChart.setTouchEnabled(true);
        pieChart.setEntryLabelColor(Color.WHITE);

        pieChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        refreshLayout.setEnabled(false);
                        ((CustomViewPager) view.getParent().getParent().getParent().getParent().getParent()).setPagingEnabled(false);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    default:
                        refreshLayout.setEnabled(true);
                        ((CustomViewPager) view.getParent().getParent().getParent().getParent().getParent()).setPagingEnabled(true);
                        break;
                }
                return false;
            }
        });

        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setCenterText(generateCenterSpannableText());
        pieChart.invalidate();
    }

    private SpannableString generateCenterSpannableText() {

        SpannableString spannableString = new SpannableString("Market Capitalization Dominance");
        return spannableString;
    }

    private void setupTextViewMarketCap()
    {
        ((TextView) view.findViewById(R.id.marketCapTextView))
                .setText(PlaceholderManager.getValueString(numberConformer(marketCapManager.getMarketCap()), getActivity()));
        ((TextView) view.findViewById(R.id.dayVolumeTotalMarketCap))
                .setText(PlaceholderManager.getValueString(numberConformer(marketCapManager.getDayVolume()), getActivity()));
    }
}
