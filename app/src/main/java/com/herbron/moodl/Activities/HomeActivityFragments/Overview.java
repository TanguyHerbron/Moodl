package com.herbron.moodl.Activities.HomeActivityFragments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.herbron.moodl.DataNotifiers.CoinmarketcapNotifierInterface;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.InfoAPIManagers.CryptocompareApiManager;
import com.herbron.moodl.DataManagers.InfoAPIManagers.CoinmarketCapAPIManager;
import com.herbron.moodl.DataManagers.PreferencesManager;
import com.herbron.moodl.CustomAdapters.OverviewListAdapter;
import com.herbron.moodl.MoodlBox;
import com.herbron.moodl.DataNotifiers.MoodlboxNotifierInterface;
import com.herbron.moodl.R;

import java.util.List;

import static com.herbron.moodl.MoodlBox.getDrawable;

/**
 * Created by Administrator on 27/05/2018.
 */

public class Overview extends Fragment implements CoinmarketcapNotifierInterface {

    private CoinmarketCapAPIManager coinmarketCapAPIManager;
    private CryptocompareApiManager cryptocompareApiManager;
    private PreferencesManager preferenceManager;
    private OverviewListAdapter overviewListAdapter;

    private View loadingFooter;

    boolean flag_loading;

    private ListView listLayout;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View fragmentView = inflater.inflate(R.layout.fragment_overview_homeactivity, container, false);

        coinmarketCapAPIManager = CoinmarketCapAPIManager.getInstance(getActivity().getBaseContext());
        cryptocompareApiManager = CryptocompareApiManager.getInstance(getActivity().getBaseContext());

        coinmarketCapAPIManager.addListener(this);

        fragmentView.findViewById(R.id.toolbar).bringToFront();

        preferenceManager = new PreferencesManager(getActivity().getBaseContext());

        listLayout = fragmentView.findViewById(R.id.linearLayoutOverview);

        listLayout.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0)
                {
                    if(!flag_loading)
                    {
                        flag_loading = true;

                        updateList();
                    }
                }
            }
        });

        flag_loading = true;

        updateList();

        setupDrawerButton(fragmentView);

        return fragmentView;
    }


    private void setupDrawerButton(View view)
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

    private void updateList()
    {
        CurrencyLoader currencyLoader = new CurrencyLoader();
        currencyLoader.execute();
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

    private void loadingIndicatorGenerator()
    {
        loadingFooter = LayoutInflater.from(getActivity().getBaseContext()).inflate(R.layout.listview_loading_indicator, null, false);

        listLayout.addFooterView(loadingFooter);
    }

    @Override
    public void onCurrenciesRetrieved(List<Currency> currencyList) {
            IconDownloader iconDownloader = new IconDownloader();
            iconDownloader.execute(currencyList);
    }

    @Override
    public void onTopCurrenciesUpdated() {

    }

    @Override
    public void onMarketCapUpdated() {

    }

    @Override
    public void onListingUpdated() {

    }

    private class CurrencyLoader extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingIndicatorGenerator();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            coinmarketCapAPIManager.getCurrenciesFrom(listLayout.getCount(), preferenceManager.getDefaultCurrency());
            return null;
        }
    }

    private class IconDownloader extends AsyncTask<List<Currency>, Void, Void>
    {
        private int iconCounter;

        @Override
        protected Void doInBackground(List<Currency>... currencies) {

            iconCounter = 0;

            for(Currency currency : currencies[0])
            {
                String iconUrl = MoodlBox.getIconUrl(currency.getSymbol(), cryptocompareApiManager);

                if(iconUrl != null)
                {
                    MoodlBox.getBitmapFromURL(iconUrl, currency.getSymbol(), getResources(), getActivity().getBaseContext(), new MoodlboxNotifierInterface() {
                        @Override
                        public void onBitmapDownloaded(Bitmap bitmap) {
                            currency.setIcon(bitmap);
                            updateChartColor(currency);
                            countIcons(currencies[0]);
                        }
                    });
                }
                else
                {
                    Drawable drawable = getDrawable(R.drawable.ic_panorama_fish_eye_24dp, getActivity().getBaseContext());

                    Bitmap icon = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

                    Canvas canvas = new Canvas(icon);
                    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    drawable.draw(canvas);

                    icon = Bitmap.createScaledBitmap(icon, 50, 50, false);

                    currency.setIcon(icon);
                    updateChartColor(currency);
                    countIcons(currencies[0]);
                }
            }

            return null;
        }

        private void countIcons(List<Currency> currencyList)
        {
            iconCounter++;

            if(iconCounter == currencyList.size())
            {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(overviewListAdapter == null)
                        {
                            overviewListAdapter = new OverviewListAdapter(Overview.this.getContext(), currencyList, getActivity());

                            listLayout.setAdapter(overviewListAdapter);
                            listLayout.setTextFilterEnabled(false);
                        }
                        else
                        {
                            overviewListAdapter.addAll(currencyList);
                            overviewListAdapter.notifyDataSetChanged();
                        }

                        listLayout.removeFooterView(loadingFooter);

                        flag_loading = false;
                    }
                });
            }
        }
    }
}
