package com.nauk.moodl.Activities.HomeActivityFragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.nauk.moodl.Activities.CurrencySelectionActivity;
import com.nauk.moodl.Activities.HomeActivity;
import com.nauk.moodl.Activities.SettingsActivity;
import com.nauk.moodl.DataManagers.BalanceManager;
import com.nauk.moodl.DataManagers.CurrencyData.Currency;
import com.nauk.moodl.DataManagers.CurrencyData.CurrencyCardview;
import com.nauk.moodl.DataManagers.CurrencyData.CurrencyDetailsList;
import com.nauk.moodl.DataManagers.CurrencyData.CurrencyTickerList;
import com.nauk.moodl.DataManagers.PreferencesManager;
import com.nauk.moodl.DataManagers.WatchlistManager;
import com.nauk.moodl.MoodlBox;
import com.nauk.moodl.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import static com.nauk.moodl.MoodlBox.collapseW;
import static com.nauk.moodl.MoodlBox.expandW;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_watchlist_homeactivity, container, false);

        refreshLayout = view.findViewById(R.id.swiperefreshwatchlist);
        currencyDetailsList = new CurrencyDetailsList(getContext());
        preferencesManager = new PreferencesManager(getContext());

        lastTimestamp = 0;
        defaultCurrency = preferencesManager.getDefaultCurrency();
        currencyTickerList = new CurrencyTickerList(getActivity());
        tickerUpdated = false;
        updateTickerList();

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

        setupDrawerButton();

        setupEditButton();

        return view;
    }

    private void updateTickerList()
    {
        AsyncTask<Void, Integer, Void> updater = new AsyncTask<Void, Integer, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                currencyTickerList.update(new BalanceManager.IconCallBack() {
                    @Override
                    public void onSuccess() {
                        tickerUpdated = true;
                        checkUpdatedData();
                    }
                });
                return null;
            }
        };

        updater.execute();
    }

    private void setupEditButton()
    {
        ImageButton editButton = view.findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout watchlistLayout = Watchlist.this.view.findViewById(R.id.linearLayoutWatchlist);

                if(editModeEnabled)
                {
                    editModeEnabled = false;

                    for(int i = 0; i < watchlistLayout.getChildCount(); i++)
                    {
                        View watchlistElement = watchlistLayout.getChildAt(i);

                        if(watchlistElement instanceof CurrencyCardview)
                        {
                            watchlistElement.setClickable(true);
                            collapseW(watchlistElement.findViewById(R.id.deleteCardWatchlist));
                        }
                    }
                }
                else
                {
                    editModeEnabled = true;

                    for(int i = 0; i < watchlistLayout.getChildCount(); i++)
                    {
                        View watchlistElement = watchlistLayout.getChildAt(i);

                        if(watchlistElement instanceof CurrencyCardview)
                        {
                            watchlistElement.setClickable(false);
                            expandW(watchlistElement.findViewById(R.id.deleteCardWatchlist));
                        }
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

            AsyncTask<Void, Integer, Void> watchlistUpdater = new AsyncTask<Void, Integer, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    watchlistManager.updateWatchlist();

                    currencyDetailsList.update(new BalanceManager.IconCallBack() {
                        @Override
                        public void onSuccess() {
                            detailsUpdated = true;
                            checkUpdatedData();
                        }
                    });
                    return null;
                }
            };

            watchlistUpdater.execute();
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
                    ((LinearLayout) view.findViewById(R.id.linearLayoutWatchlist)).addView(new CurrencyCardview(getContext(), currency, getActivity()));
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
                        MoodlBox.getBitmapFromURL(getIconUrl(sucessCurrency.getSymbol()), sucessCurrency.getSymbol(), getResources(), getContext(), new HomeActivity.IconCallBack() {
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
