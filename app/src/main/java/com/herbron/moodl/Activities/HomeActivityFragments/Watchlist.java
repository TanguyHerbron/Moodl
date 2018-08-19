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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.herbron.moodl.Activities.CurrencySelectionActivity;
import com.herbron.moodl.DataNotifiers.CoinmarketcapNotifierInterface;
import com.herbron.moodl.DataNotifiers.CryptocompareNotifierInterface;
import com.herbron.moodl.CurrencyInfoUpdateNotifierInterface;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.CurrencyData.CurrencyCardview;
import com.herbron.moodl.DataManagers.InfoAPIManagers.CryptocompareApiManager;
import com.herbron.moodl.DataManagers.InfoAPIManagers.CoinmarketCapAPIManager;
import com.herbron.moodl.DataManagers.DatabaseManager;
import com.herbron.moodl.DataManagers.PreferencesManager;
import com.herbron.moodl.DataManagers.WatchlistManager;
import com.herbron.moodl.MoodlBox;
import com.herbron.moodl.DataNotifiers.MoodlboxNotifierInterface;
import com.herbron.moodl.R;
import com.jmedeisis.draglinearlayout.DragLinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.herbron.moodl.MoodlBox.collapseW;
import static com.herbron.moodl.MoodlBox.expandW;
import static com.herbron.moodl.MoodlBox.getColor;

/**
 * Created by Tiji on 13/04/2018.
 */

public class Watchlist extends Fragment implements CryptocompareNotifierInterface {

    private WatchlistManager watchlistManager;
    private View view;
    private int watchlistCounter;
    private CryptocompareApiManager cryptocompareApiManager;
    private SwipeRefreshLayout refreshLayout;
    private DragLinearLayout dragLinearLayout;
    private long lastTimestamp;
    private PreferencesManager preferencesManager;
    private String defaultCurrency;
    private CoinmarketCapAPIManager coinmarketCapAPIManager;
    private boolean tickerUpdated;
    private boolean detailsUpdated;
    private boolean editModeEnabled;
    private DatabaseManager databaseManager;
    private ImageButton editButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.homeactivity_fragment_watchlist, container, false);

        refreshLayout = view.findViewById(R.id.swiperefreshwatchlist);
        dragLinearLayout = view.findViewById(R.id.linearLayoutWatchlist);
        cryptocompareApiManager = CryptocompareApiManager.getInstance(getActivity().getBaseContext());
        preferencesManager = new PreferencesManager(getActivity().getBaseContext());
        databaseManager = new DatabaseManager(getActivity().getBaseContext());

        lastTimestamp = 0;
        defaultCurrency = preferencesManager.getDefaultCurrency();
        coinmarketCapAPIManager = CoinmarketCapAPIManager.getInstance(getActivity());
        tickerUpdated = false;
        cryptocompareApiManager.addListener(this);
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

        watchlistManager = new WatchlistManager(getActivity().getBaseContext());

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
        ListingUpdater listingUpdater = new ListingUpdater();
        listingUpdater.execute();
    }

    private class ListingUpdater extends AsyncTask<Void, Integer, Void> implements CoinmarketcapNotifierInterface {

        @Override
        protected Void doInBackground(Void... voids) {

            coinmarketCapAPIManager.addListener(this);

            if(!coinmarketCapAPIManager.isUpToDate())
            {
                coinmarketCapAPIManager.updateListing();
            }
            else
            {
                tickerUpdated = true;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkUpdatedData();
                    }
                });
            }
            return null;
        }

        @Override
        public void onCurrenciesRetrieved(List<Currency> currencyList) {

        }

        @Override
        public void onTopCurrenciesUpdated() {

        }

        @Override
        public void onMarketCapUpdated() {

        }

        @Override
        public void onListingUpdated() {
            tickerUpdated = true;
            checkUpdatedData();
        }
    }

    private void disableEdition()
    {
        editButton.setBackground(MoodlBox.getDrawable(R.drawable.check_to_edit, getActivity().getBaseContext()));

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

    private void enableEdition()
    {
        editButton.setBackground(MoodlBox.getDrawable(R.drawable.edit_to_check, getActivity().getBaseContext()));

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

    private void setupEditButton()
    {
        editButton = view.findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(editModeEnabled)
                {
                    disableEdition();
                }
                else
                {
                    enableEdition();
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
                if(editModeEnabled)
                {
                    disableEdition();
                }

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

                    if(!cryptocompareApiManager.isDetailsUpToDate())
                    {
                        cryptocompareApiManager.updateDetails();
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
                    View addedView = new CurrencyCardview(getActivity().getBaseContext(), currency, getActivity());

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

    private void updateChartColor(Currency currency)
    {
        if(currency.getIcon() != null)
        {
            Palette.Builder builder = Palette.from(currency.getIcon());

            currency.setChartColor(builder.generate().getDominantColor(getColor(R.color.default_color, getActivity().getBaseContext())));
        }
        else
        {
            currency.setChartColor(getColor(R.color.default_color, getActivity().getBaseContext()));
        }
    }

    public int getCurrencyId(String symbol)
    {
        int id = 0;

        try {
            JSONObject jsonObject = new JSONObject(cryptocompareApiManager.getCoinInfosHashmap().get(symbol));
            id = jsonObject.getInt("Id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return id;
    }

    @Override
    public void onDetailsUpdated() {
        detailsUpdated = true;
        checkUpdatedData();
    }

    @Override
    public void onExchangesUpdated() {

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
                currency.setTickerId(coinmarketCapAPIManager.getTickerIdForSymbol(currency.getSymbol()));
                currency.setId(getCurrencyId(currency.getSymbol()));
                currency.updatePrice(getActivity(), preferencesManager.getDefaultCurrency(), new CurrencyInfoUpdateNotifierInterface() {
                    @Override
                    public void onTimestampPriceUpdated(String price) {

                    }

                    @Override
                    public void onHistoryDataUpdated() {

                    }

                    @Override
                    public void onPriceUpdated(Currency successCurrency) {
                        String iconUrl = MoodlBox.getIconUrl(currency.getSymbol(), cryptocompareApiManager);

                        if(iconUrl != null)
                        {
                            MoodlBox.getBitmapFromURL(iconUrl, currency.getSymbol(), getResources(), getActivity().getBaseContext(), new MoodlboxNotifierInterface() {
                                @Override
                                public void onBitmapDownloaded(Bitmap bitmapIcon) {
                                    currency.setIcon(bitmapIcon);
                                    updateChartColor(currency);
                                    countWatchlist();

                                }
                            });
                        }
                        else
                        {
                            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_moodl);
                            icon = Bitmap.createScaledBitmap(icon, 50, 50, false);

                            currency.setIcon(icon);
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
