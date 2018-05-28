package com.nauk.moodl.Activities.HomeActivityFragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
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
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.nauk.moodl.Activities.HomeActivity;
import com.nauk.moodl.DataManagers.CurrencyData.Currency;
import com.nauk.moodl.DataManagers.CurrencyData.CurrencyCardview;
import com.nauk.moodl.DataManagers.CurrencyData.CurrencyDetailsList;
import com.nauk.moodl.DataManagers.CurrencyData.CurrencyTickerList;
import com.nauk.moodl.DataManagers.PreferencesManager;
import com.nauk.moodl.LayoutManagers.OverviewListAdapter;
import com.nauk.moodl.MoodlBox;
import com.nauk.moodl.R;

import java.util.List;

/**
 * Created by Administrator on 27/05/2018.
 */

public class Overview extends Fragment {

    private CurrencyTickerList currencyTickerList;
    private CurrencyDetailsList currencyDetailsList;
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

        currencyTickerList = CurrencyTickerList.getInstance(getContext());
        currencyDetailsList = CurrencyDetailsList.getInstance(getContext());

        preferenceManager = new PreferencesManager(getContext());

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

    public interface UpdateCallBack
    {
        void onSuccess(List<Currency> currencyList);
    }

    private void loadingIndicatorGenerator()
    {
        loadingFooter = LayoutInflater.from(getContext()).inflate(R.layout.listview_loading_indicator, null, false);

        listLayout.addFooterView(loadingFooter);
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
            currencyTickerList.getCurrenciesFrom(listLayout.getCount(), preferenceManager.getDefaultCurrency(), new UpdateCallBack() {
                @Override
                public void onSuccess(List<Currency> currencyList)
                {
                    IconDownloader iconDownloader = new IconDownloader();
                    iconDownloader.execute(currencyList);
                }
            });
            return null;
        }
    }

    private class IconDownloader extends AsyncTask<List<Currency>, Void, Void>
    {
        private int iconCounter;

        @Override
        protected Void doInBackground(List<Currency>... currencies) {

            for(Currency currency : currencies[0])
            {
                String iconUrl = MoodlBox.getIconUrl(currency.getSymbol(), currencyDetailsList);

                if(iconUrl != null)
                {
                    MoodlBox.getBitmapFromURL(iconUrl, currency.getSymbol(), getResources(), getContext(), new HomeActivity.IconCallBack() {
                        @Override
                        public void onSuccess(Bitmap bitmap) {
                            currency.setIcon(bitmap);
                            updateChartColor(currency);
                            countIcons(currencies[0]);
                        }
                    });
                }
                else
                {
                    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_moodl);
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
                            overviewListAdapter = new OverviewListAdapter(getContext(), currencyList, getActivity());

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
