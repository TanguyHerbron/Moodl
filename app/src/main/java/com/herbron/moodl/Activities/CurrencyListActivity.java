package com.herbron.moodl.Activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.herbron.moodl.DataManagers.InfoAPIManagers.CoinmarketCapAPIManager;
import com.herbron.moodl.DataNotifiers.CoinmarketcapNotifierInterface;
import com.herbron.moodl.DataNotifiers.CryptocompareNotifierInterface;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.InfoAPIManagers.CryptocompareApiManager;
import com.herbron.moodl.DataManagers.DatabaseManager;
import com.herbron.moodl.DataManagers.PreferencesManager;
import com.herbron.moodl.CustomAdapters.CoinWatchlistAdapter;
import com.herbron.moodl.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class CurrencyListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, CryptocompareNotifierInterface, CoinmarketcapNotifierInterface {

    private CoinWatchlistAdapter adapter;
    private ListView listView;
    private android.widget.Filter filter;
    private CryptocompareApiManager cryptocompareApiManager;
    private CoinmarketCapAPIManager coinmarketCapAPIManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_currency_list);

        cryptocompareApiManager = CryptocompareApiManager.getInstance(this);
        cryptocompareApiManager.addListener(this);

        coinmarketCapAPIManager = CoinmarketCapAPIManager.getInstance(this);
        coinmarketCapAPIManager.addListener(this);

        setTitle(getString(R.string.select_coin));

        ListLoader listLoader = new ListLoader();
        listLoader.execute();
    }

    private void setupSearchView()
    {
        SearchView searchView = findViewById(R.id.search_bar);

        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);
        searchView.onActionViewExpanded();
    }

    private void setupAdapter()
    {
        adapter = new CoinWatchlistAdapter(this, new ArrayList<>(coinmarketCapAPIManager.getTotalListing()));
    }

    private void setupList()
    {
        listView = findViewById(R.id.coinsPreview);

        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(false);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Currency selectedCurrency = (Currency) adapterView.getItemAtPosition(i);

                PreferencesManager preferencesManager = new PreferencesManager(getApplicationContext());
                DatabaseManager databaseManager = new DatabaseManager(getApplicationContext());

                if(databaseManager.addCurrencyToWatchlist(selectedCurrency))
                {
                    preferencesManager.setMustUpdateWatchlist(true);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), getString(R.string.already_watchlist), Toast.LENGTH_SHORT).show();
                }

                finish();
            }
        });

        filter = adapter.getFilter();
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

    @Override
    public boolean onQueryTextChange(String text)
    {
        adapter.getFilter().filter(text);

        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        return false;
    }

    private void detailsEvent()
    {
        if(coinmarketCapAPIManager.isUpToDate() && cryptocompareApiManager.isDetailsUpToDate())
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupAdapter();
                    setupList();
                    setupSearchView();

                    expand(findViewById(R.id.listContainerLayout));
                    findViewById(R.id.currencyListProgressBar).setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onDetailsUpdated() {
        detailsEvent();
    }

    @Override
    public void onExchangesUpdated() {

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
        detailsEvent();
    }

    private class ListLoader extends AsyncTask<Void, Integer, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            if(Looper.myLooper() == null)
            {
                Looper.prepare();
            }

            if(!cryptocompareApiManager.isDetailsUpToDate() || !coinmarketCapAPIManager.isUpToDate())
            {
                if(!cryptocompareApiManager.isDetailsUpToDate())
                {
                    cryptocompareApiManager.updateDetails();
                }

                if(!coinmarketCapAPIManager.isUpToDate())
                {
                    coinmarketCapAPIManager.updateListing();
                }
            }
            else
            {
                detailsEvent();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {

        }
    }
}
