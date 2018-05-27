package com.nauk.moodl.Activities.HomeActivityFragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

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

        updateList();

        return fragmentView;
    }

    private void updateList()
    {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                currencyTickerList.getCurrenciesFrom(listLayout.getCount(), preferenceManager.getDefaultCurrency(), new UpdateCallBack() {
                    @Override
                    public void onSuccess(List<Currency> currencyList)
                    {
                        for(Currency currency : currencyList)
                        {
                            String iconUrl = MoodlBox.getIconUrl(currency.getSymbol(), currencyDetailsList);

                            if(iconUrl != null)
                            {
                                MoodlBox.getBitmapFromURL(iconUrl, currency.getSymbol(), getResources(), getContext(), new HomeActivity.IconCallBack() {
                                    @Override
                                    public void onSuccess(Bitmap bitmap) {
                                        currency.setIcon(bitmap);
                                        updateChartColor(currency);
                                    }
                                });
                            }
                            else
                            {
                                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_moodl);
                                icon = Bitmap.createScaledBitmap(icon, 50, 50, false);

                                currency.setIcon(icon);
                                updateChartColor(currency);
                            }
                        }

                        if(overviewListAdapter == null)
                        {
                            overviewListAdapter = new OverviewListAdapter(getContext(), currencyList);

                            listLayout.setAdapter(overviewListAdapter);
                            listLayout.setTextFilterEnabled(false);
                        }
                        else
                        {
                            overviewListAdapter.addAll(currencyList);
                            overviewListAdapter.notifyDataSetChanged();
                        }

                        flag_loading = false;
                    }
            });
                return null;
            }
        }.execute();

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
}
