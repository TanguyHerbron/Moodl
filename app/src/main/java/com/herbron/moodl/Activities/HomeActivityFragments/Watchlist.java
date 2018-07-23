package com.herbron.moodl.Activities.HomeActivityFragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.herbron.moodl.Activities.CurrencySelectionActivity;
import com.herbron.moodl.Activities.HomeActivity;
import com.herbron.moodl.DataManagers.BalanceManager;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.CurrencyData.CurrencyCardview;
import com.herbron.moodl.DataManagers.CurrencyData.CurrencyDetailsList;
import com.herbron.moodl.DataManagers.CurrencyData.CurrencyTickerList;
import com.herbron.moodl.DataManagers.DatabaseManager;
import com.herbron.moodl.DataManagers.PreferencesManager;
import com.herbron.moodl.DataManagers.WatchlistManager;
import com.herbron.moodl.MoodlBox;
import com.herbron.moodl.R;
import com.jmedeisis.draglinearlayout.DragLinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import static com.herbron.moodl.MoodlBox.collapseW;
import static com.herbron.moodl.MoodlBox.expandW;
import static com.herbron.moodl.MoodlBox.getColor;
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
    private DragLinearLayout dragLinearLayout;
    private long lastTimestamp;
    private PreferencesManager preferencesManager;
    private String defaultCurrency;
    private CurrencyTickerList currencyTickerList;
    private boolean tickerUpdated;
    private boolean detailsUpdated;
    private boolean editModeEnabled;
    private DatabaseManager databaseManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_watchlist_homeactivity, container, false);

        refreshLayout = view.findViewById(R.id.swiperefreshwatchlist);
        dragLinearLayout = view.findViewById(R.id.linearLayoutWatchlist);
        currencyDetailsList = CurrencyDetailsList.getInstance(getContext());
        preferencesManager = new PreferencesManager(getContext());
        databaseManager = new DatabaseManager(getContext());

        lastTimestamp = 0;
        defaultCurrency = preferencesManager.getDefaultCurrency();
        currencyTickerList = CurrencyTickerList.getInstance(getActivity());
        tickerUpdated = false;
        updateTickerList();

        dragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
            @Override
            public void onSwap(View firstView, int firstPosition, View secondView, int secondPosition) {
                CurrencyCardview currencyCardviewMoved = (CurrencyCardview) firstView;
                CurrencyCardview currencyCardviewSwaped = (CurrencyCardview) secondView;

                databaseManager.updateWatchlistPosition(currencyCardviewMoved.getCurrency().getSymbol(), secondPosition);
                databaseManager.updateWatchlistPosition(currencyCardviewSwaped.getCurrency().getSymbol(), firstPosition);
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

        setupDrawerButton();

        setupEditButton();

        return view;
    }

    private void updateTickerList()
    {
        AsyncTask<Void, Integer, Void> updater = new AsyncTask<Void, Integer, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if(!currencyTickerList.isUpToDate())
                {
                    currencyTickerList.updateListing(new BalanceManager.IconCallBack() {
                        @Override
                        public void onSuccess() {
                            tickerUpdated = true;
                            checkUpdatedData();
                        }
                    });
                }
                else
                {
                    tickerUpdated = true;
                    checkUpdatedData();
                }

                return null;
            }
        };

        updater.execute();
    }

    private void setupEditButton()
    {
        final ImageButton editButton = view.findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(editModeEnabled)
                {
                    editButton.setBackground(MoodlBox.getDrawable(R.drawable.check_to_edit, getContext()));

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) editButton.getBackground();
                        animatedVectorDrawable.start();
                    }

                    editModeEnabled = false;

                    for(int i = 0; i < dragLinearLayout.getChildCount(); i++)
                    {
                        View watchlistElement = dragLinearLayout.getChildAt(i);

                        if(watchlistElement instanceof CurrencyCardview)
                        {
                            watchlistElement.setClickable(true);
                            collapseW(watchlistElement.findViewById(R.id.deleteCardWatchlist));
                            collapseW(watchlistElement.findViewById(R.id.dragCardWatchlist));
                        }
                    }
                }
                else
                {
                    editButton.setBackground(MoodlBox.getDrawable(R.drawable.edit_to_check, getContext()));

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) editButton.getBackground();
                        animatedVectorDrawable.start();
                    }

                    editModeEnabled = true;

                    for(int i = 0; i < dragLinearLayout.getChildCount(); i++)
                    {
                        View watchlistElement = dragLinearLayout.getChildAt(i);

                        if(watchlistElement instanceof CurrencyCardview)
                        {
                            watchlistElement.setClickable(false);
                            expandW(watchlistElement.findViewById(R.id.deleteCardWatchlist));
                            expandW(watchlistElement.findViewById(R.id.dragCardWatchlist));
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

                    if(!currencyDetailsList.isUpToDate())
                    {
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
                        detailsUpdated = true;
                        checkUpdatedData();
                    }
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

            if(watchlistManager.getWatchlist().size() == 0)
            {
                generateCards();
            }
        }
    }

    private void generateCards()
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dragLinearLayout.removeAllViews();
                view.findViewById(R.id.progressBarWatchlist).setVisibility(View.GONE);

                for(Currency currency : watchlistManager.getWatchlist())
                {
                    View addedView = new CurrencyCardview(getContext(), currency, getActivity());

                    dragLinearLayout.addDragView(addedView, addedView.findViewById(R.id.dragCardWatchlist));
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

            currency.setChartColor(builder.generate().getDominantColor(getColor(R.color.default_color, getContext())));
        }
        else
        {
            currency.setChartColor(getColor(R.color.default_color, getContext()));
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

                        String iconUrl = MoodlBox.getIconUrl(sucessCurrency.getSymbol(), currencyDetailsList);

                        if(iconUrl != null)
                        {
                            MoodlBox.getBitmapFromURL(iconUrl, sucessCurrency.getSymbol(), getResources(), getContext(), new HomeActivity.IconCallBack() {
                                @Override
                                public void onSuccess(Bitmap bitmapIcon) {
                                    sucessCurrency.setIcon(bitmapIcon);
                                    updateChartColor(currency);
                                    countWatchlist();
                                }
                            });
                        }
                        else
                        {
                            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_moodl);
                            icon = Bitmap.createScaledBitmap(icon, 50, 50, false);

                            sucessCurrency.setIcon(icon);
                            updateChartColor(currency);
                            countWatchlist();
                        }
                    }
                });
            }
            return null;
        }
    }
}
