package com.nauk.moodl.Activities.HomeActivityFragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.nauk.moodl.Activities.CurrencyDetailsActivity;
import com.nauk.moodl.Activities.CurrencySelectionActivity;
import com.nauk.moodl.Activities.HomeActivity;
import com.nauk.moodl.Activities.SettingsActivity;
import com.nauk.moodl.DataManagers.BalanceManager;
import com.nauk.moodl.DataManagers.CurrencyData.Currency;
import com.nauk.moodl.DataManagers.CurrencyData.CurrencyCardview;
import com.nauk.moodl.DataManagers.CurrencyData.CurrencyDataChart;
import com.nauk.moodl.DataManagers.CurrencyData.CurrencyDetailsList;
import com.nauk.moodl.DataManagers.CurrencyData.CurrencyTickerList;
import com.nauk.moodl.DataManagers.DatabaseManager;
import com.nauk.moodl.DataManagers.PreferencesManager;
import com.nauk.moodl.DataManagers.WatchlistManager;
import com.nauk.moodl.PlaceholderManager;
import com.nauk.moodl.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.nauk.moodl.MoodlBox.collapseH;
import static com.nauk.moodl.MoodlBox.collapseW;
import static com.nauk.moodl.MoodlBox.expandH;
import static com.nauk.moodl.MoodlBox.expandW;
import static com.nauk.moodl.MoodlBox.getVerticalExpandAnimation;
import static com.nauk.moodl.MoodlBox.numberConformer;
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
    private String defaultCurrency;
    private CurrencyTickerList currencyTickerList;
    private boolean tickerUpdated;
    private boolean detailsUpdated;
    private boolean editModeEnabled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_watchlist_homeactivity, container, false);

        refreshLayout = view.findViewById(R.id.swiperefreshwatchlist);
        currencyDetailsList = new CurrencyDetailsList(getContext());
        preferencesManager = new PreferencesManager(getContext());

        lastTimestamp = 0;
        defaultCurrency = preferencesManager.getDefaultCurrency();
        currencyTickerList = new CurrencyTickerList(getActivity());
        tickerUpdated = false;
        currencyTickerList.update(new BalanceManager.IconCallBack() {
            @Override
            public void onSuccess() {
                tickerUpdated = true;
                checkUpdatedData();
            }
        });

        editModeEnabled = false;

        watchlistManager = new WatchlistManager(getContext());

        updateWatchlist(true);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateWatchlist(false);
            }
        });

        setupAddWatchlistButton();

        setupSettingsButton();

        setupEditButton();

        return view;
    }

    private void setupEditButton()
    {
        ImageButton editButton = view.findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editModeEnabled)
                {
                    editModeEnabled = false;

                    for(int i = 0; i < ((LinearLayout) Watchlist.this.view.findViewById(R.id.linearLayoutWatchlist)).getChildCount(); i++)
                    {
                        ((LinearLayout) Watchlist.this.view.findViewById(R.id.linearLayoutWatchlist)).getChildAt(i).setClickable(true);
                        collapseW(((LinearLayout) Watchlist.this.view.findViewById(R.id.linearLayoutWatchlist)).getChildAt(i).findViewById(R.id.deleteCardWatchlist));
                    }
                }
                else
                {
                    editModeEnabled = true;

                    LinearLayout watchlistLayout = Watchlist.this.view.findViewById(R.id.linearLayoutWatchlist);

                    Animation anim = getVerticalExpandAnimation(watchlistLayout.getChildAt(0));

                    for(int i = 0; i < ((LinearLayout) Watchlist.this.view.findViewById(R.id.linearLayoutWatchlist)).getChildCount(); i++)
                    {
                        View watchlistElement = watchlistLayout.getChildAt(i);

                        watchlistElement.setClickable(false);
                        expandW(watchlistElement.findViewById(R.id.deleteCardWatchlist), anim);
                    }
                }
            }
        });
    }

    private void setupAddWatchlistButton()
    {
        Button addWatchlistButton = view.findViewById(R.id.buttonAddWatchlist);
        addWatchlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent selectionIntent = new Intent(getActivity(), CurrencySelectionActivity.class);
                selectionIntent.putExtra("isWatchList", true);
                startActivity(selectionIntent);
            }
        });
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
            updateWatchlist(true);
        }
        else
        {
            updateWatchlist(preferencesManager.mustUpdateWatchlist());
        }
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
            detailsUpdated = false;

            watchlistManager.updateWatchlist();

            currencyDetailsList.update(new BalanceManager.IconCallBack() {
                @Override
                public void onSuccess() {
                    detailsUpdated = true;
                    checkUpdatedData();
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

    private void checkUpdatedData()
    {
        if(tickerUpdated && detailsUpdated)
        {
            WatchlistUpdater watchlistUpdater = new WatchlistUpdater();
            watchlistUpdater.execute();
        }
    }

    private void generateCards()
    {
        ((LinearLayout) view.findViewById(R.id.linearLayoutWatchlist)).removeAllViews();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(Currency currency : watchlistManager.getWatchlist())
                {
                    ((LinearLayout) view.findViewById(R.id.linearLayoutWatchlist)).addView(new CurrencyCardview(getContext(), currency));
                }
            }
        });

        if(refreshLayout.isRefreshing())
        {
            refreshLayout.setRefreshing(false);
        }
    }

    private void countWatchlist()
    {
        watchlistCounter++;

        if(watchlistCounter >= watchlistManager.getWatchlist().size())
        {
            generateCards();
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
                    R.mipmap.ic_launcher_moodl);
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

    public int getCurrencyId(String symbol)
    {
        int id = 0;

        try {
            JSONObject jsonObject = new JSONObject(currencyDetailsList.getCoinInfosHashmap().get(symbol));
            id = jsonObject.getInt("Id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return id;
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
                currency.setTickerId(currencyTickerList.getTickerIdForSymbol(currency.getSymbol()));
                currency.setId(getCurrencyId(currency.getSymbol()));
                currency.updatePrice(getActivity(), preferencesManager.getDefaultCurrency(), new Currency.CurrencyCallBack() {
                    @Override
                    public void onSuccess(final Currency sucessCurrency) {
                        getBitmapFromURL(getIconUrl(sucessCurrency.getSymbol()), new HomeActivity.IconCallBack() {
                            @Override
                            public void onSuccess(Bitmap bitmapIcon) {
                                sucessCurrency.setIcon(bitmapIcon);
                                updateChartColor(currency);
                                countWatchlist();
                            }
                        });
                    }
                });
            }
            return null;
        }
    }
}
