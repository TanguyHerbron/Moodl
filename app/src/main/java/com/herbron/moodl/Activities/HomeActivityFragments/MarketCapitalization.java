package com.herbron.moodl.Activities.HomeActivityFragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.herbron.moodl.DataNotifiers.CoinmarketcapNotifierInterface;
import com.herbron.moodl.DataNotifiers.CryptocompareNotifierInterface;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.InfoAPIManagers.CoinmarketCapAPIManager;
import com.herbron.moodl.DataManagers.InfoAPIManagers.CryptocompareApiManager;
import com.herbron.moodl.DataManagers.PreferencesManager;
import com.herbron.moodl.CustomLayouts.CustomPieChart;
import com.herbron.moodl.MoodlBox;
import com.herbron.moodl.DataNotifiers.MoodlboxNotifierInterface;
import com.herbron.moodl.PlaceholderManager;
import com.herbron.moodl.R;

import java.util.ArrayList;
import java.util.List;

import static com.herbron.moodl.MoodlBox.getColor;
import static java.lang.Math.abs;

/**
 * Created by Tiji on 13/04/2018.
 */

public class MarketCapitalization extends Fragment implements CryptocompareNotifierInterface, CoinmarketcapNotifierInterface {

    private PreferencesManager preferencesManager;
    private CoinmarketCapAPIManager coinmarketCapAPIManager;
    private SwipeRefreshLayout refreshLayout;
    private NestedScrollView nestedScrollView;
    private long lastTimestamp;
    private String defaultCurrency;
    private CryptocompareApiManager cryptocompareApiManager;
    private boolean isDetailsUpdated;
    private boolean isTopCurrenciesUpdated;
    private boolean isMarketpCapUpdated;
    private int iconCounter;

    private View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.homeactivity_fragment_marketcap, container, false);

        preferencesManager = new PreferencesManager(getActivity().getBaseContext());
        coinmarketCapAPIManager = CoinmarketCapAPIManager.getInstance(getActivity().getBaseContext());
        coinmarketCapAPIManager.addListener(this);

        cryptocompareApiManager = CryptocompareApiManager.getInstance(getActivity().getBaseContext());
        cryptocompareApiManager.addListener(this);

        if(!cryptocompareApiManager.isDetailsUpToDate())
        {
            cryptocompareApiManager.updateDetails();
        }
        else
        {
            isDetailsUpdated = true;
            countCompletedMarketCapRequest();
        }

        defaultCurrency = preferencesManager.getDefaultCurrency();
        lastTimestamp = 0;

        nestedScrollView = view.findViewById(R.id.nestedMarketCap);

        setupRefreshLayout();

        setupDrawerButton();

        updateMarketCap(true);

        return view;
    }

    private void setupDrawerButton()
    {
        ImageButton drawerButton = view.findViewById(R.id.drawer_button);
        drawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);

                if(drawerLayout.isDrawerOpen(GravityCompat.START))
                {
                    drawerLayout.closeDrawers();
                }
                else
                {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
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

    private void updateMarketCap(boolean mustUpdate)
    {
        if(System.currentTimeMillis() / 1000 - lastTimestamp > 60 || mustUpdate)
        {
            if(!refreshLayout.isRefreshing())
            {
                refreshLayout.setRefreshing(true);
            }

            iconCounter = 0;

            isTopCurrenciesUpdated = false;
            isMarketpCapUpdated = false;

            lastTimestamp = System.currentTimeMillis() / 1000;

            coinmarketCapAPIManager.updateTopCurrencies(preferencesManager.getDefaultCurrency());

            coinmarketCapAPIManager.updateMarketCap(preferencesManager.getDefaultCurrency());
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
        List<Currency> topCurrencies = coinmarketCapAPIManager.getTopCurrencies();
        ArrayList<Integer> colors = new ArrayList<>();

        float topCurrenciesDominance = 0;

        for(int i = 0; i < topCurrencies.size(); i++)
        {
            PieEntry pieEntry = new PieEntry(topCurrencies.get(i).getDominance(coinmarketCapAPIManager.getMarketCap()), topCurrencies.get(i).getSymbol(), topCurrencies.get(i).getSymbol());

            if(pieEntry.getValue() < 3)
            {
                pieEntry.setLabel("");
            }

            entries.add(pieEntry);
            topCurrenciesDominance += topCurrencies.get(i).getDominance(coinmarketCapAPIManager.getMarketCap());
            colors.add(topCurrencies.get(i).getChartColor());
        }

        entries.add(new PieEntry(100-topCurrenciesDominance, getString(R.string.others), "others"));
        colors.add(-12369084);

        PieDataSet set = new PieDataSet(entries, getString(R.string.market_dominance));
        set.setColors(colors);
        set.setSliceSpace(1);
        set.setDrawValues(false);

        return set;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void countCompletedMarketCapRequest()
    {
        if(isTopCurrenciesUpdated && isMarketpCapUpdated && isDetailsUpdated)
        {
            updateIcons();
        }
    }

    private void countIcons()
    {
        iconCounter++;

        if(iconCounter >= coinmarketCapAPIManager.getTopCurrencies().size())
        {
            refreshDisplayedData();
        }
    }

    private void updateIcons()
    {
        for(int i = 0; i < coinmarketCapAPIManager.getTopCurrencies().size(); i++)
        {
            final Currency localCurrency = coinmarketCapAPIManager.getTopCurrencies().get(i);
            final int index = i;

            String iconUrl = MoodlBox.getIconUrl(coinmarketCapAPIManager.getTopCurrencies().get(i).getSymbol(), 500, cryptocompareApiManager);

            if(iconUrl != null)
            {
                MoodlBox.getBitmapFromURL(iconUrl, localCurrency.getSymbol(), getResources(), getActivity().getBaseContext(), new MoodlboxNotifierInterface() {
                    @Override
                    public void onBitmapDownloaded(Bitmap bitmapIcon) {
                        Palette.Builder builder = Palette.from(bitmapIcon);

                        coinmarketCapAPIManager.getTopCurrencies().get(index).setIcon(bitmapIcon);
                        coinmarketCapAPIManager.getTopCurrencies().get(index).setChartColor(builder.generate().getDominantColor(getColor(R.color.default_color, getActivity().getBaseContext())));

                        countIcons();

                    }
                });
            }
            else
            {
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_moodl);
                icon = Bitmap.createScaledBitmap(icon, 500, 500, false);

                localCurrency.setIcon(icon);
                countIcons();
            }
        }
    }

    private void setupPieChart(PieData data)
    {
        CustomPieChart pieChart = view.findViewById(R.id.marketCapPieChart);

        pieChart.setData(data);
        pieChart.setDrawSlicesUnderHole(false);
        pieChart.setUsePercentValues(false);
        pieChart.setTouchEnabled(true);
        pieChart.setEntryLabelColor(Color.WHITE);

        updateDetails(coinmarketCapAPIManager.getMarketCap(), coinmarketCapAPIManager.getDayVolume(), getString(R.string.global), 0);
        ((TextView) view.findViewById(R.id.textViewActiveCrypto))
                .setText(coinmarketCapAPIManager.getActive_crypto());
        ((TextView) view.findViewById(R.id.textViewActiveMarkets))
                .setText(coinmarketCapAPIManager.getActive_markets());

        pieChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        nestedScrollView.setEnabled(false);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    default:
                        nestedScrollView.setEnabled(true);
                        break;
                }

                view.performClick();

                return false;
            }
        });

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        view.findViewById(R.id.layoutActiveCrypto).setVisibility(View.GONE);
                        view.findViewById(R.id.layoutActiveMarkets).setVisibility(View.GONE);

                        if(!e.getData().equals("others"))
                        {
                            Currency currency = coinmarketCapAPIManager.getCurrencyFromSymbol((String) e.getData());
                            view.findViewById(R.id.currencyIcon).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.layoutPercentageDominance).setVisibility(View.VISIBLE);

                            updateDetails(currency.getMarketCapitalization(), currency.getVolume24h(), currency.getName() + " (" + currency.getSymbol() + ")", h.getY());

                            ((ImageView) view.findViewById(R.id.currencyIcon))
                                    .setImageBitmap(currency.getIcon());

                            pieChart.setDrawCenterText(false);
                        }
                        else
                        {
                            double othersMarketCap = coinmarketCapAPIManager.getMarketCap();
                            double othersVolume = coinmarketCapAPIManager.getDayVolume();

                            for(int i = 0; i < coinmarketCapAPIManager.getTopCurrencies().size(); i++)
                            {
                                othersMarketCap -= coinmarketCapAPIManager.getTopCurrencies().get(i).getMarketCapitalization();
                                othersVolume -= coinmarketCapAPIManager.getTopCurrencies().get(i).getVolume24h();
                            }

                            view.findViewById(R.id.currencyIcon).setVisibility(View.GONE);
                            view.findViewById(R.id.layoutPercentageDominance).setVisibility(View.VISIBLE);

                            updateDetails(othersMarketCap, othersVolume, getString(R.string.other_coins), h.getY());

                            pieChart.setDrawCenterText(true);
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected() {
                view.findViewById(R.id.currencyIcon).setVisibility(View.GONE);
                view.findViewById(R.id.layoutPercentageDominance).setVisibility(View.GONE);

                view.findViewById(R.id.layoutActiveCrypto).setVisibility(View.VISIBLE);
                view.findViewById(R.id.layoutActiveMarkets).setVisibility(View.VISIBLE);

                updateDetails(coinmarketCapAPIManager.getMarketCap(), coinmarketCapAPIManager.getDayVolume(), getString(R.string.global), 0);

                pieChart.setDrawCenterText(true);
            }
        });

        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setCenterText(generateCenterSpannableText());
        pieChart.invalidate();
    }

    private void updateDetails(double marketCap, double volume, String title, double percentage)
    {
        ((TextView) view.findViewById(R.id.textViewMarketCap))
                .setText(PlaceholderManager.getValueString(MoodlBox.numberConformer(marketCap), getActivity().getBaseContext()));
        ((TextView) view.findViewById(R.id.textViewVolume))
                .setText(PlaceholderManager.getValueString(MoodlBox.numberConformer(volume), getActivity().getBaseContext()));
        ((TextView) view.findViewById(R.id.textViewTitle))
                .setText(title);
        ((TextView) view.findViewById(R.id.textViewDominancePercentage))
                .setText(PlaceholderManager.getPercentageString(MoodlBox.numberConformer(percentage), getActivity().getBaseContext()));
    }

    private SpannableString generateCenterSpannableText() {

        SpannableString spannableString = new SpannableString(getString(R.string.market_dominance));
        return spannableString;
    }

    @Override
    public void onDetailsUpdated() {
        isDetailsUpdated = true;
        countCompletedMarketCapRequest();
    }

    @Override
    public void onExchangesUpdated() {

    }

    @Override
    public void onCurrenciesRetrieved(List<Currency> currencyList) {

    }

    @Override
    public void onTopCurrenciesUpdated() {
        isTopCurrenciesUpdated = true;
        countCompletedMarketCapRequest();
    }

    @Override
    public void onMarketCapUpdated() {
        isMarketpCapUpdated = true;
        countCompletedMarketCapRequest();
    }

    @Override
    public void onListingUpdated() {

    }
}
