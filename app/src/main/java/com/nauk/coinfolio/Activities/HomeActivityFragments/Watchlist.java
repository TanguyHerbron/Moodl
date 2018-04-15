package com.nauk.coinfolio.Activities.HomeActivityFragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nauk.coinfolio.Activities.CurrencySelectionActivity;
import com.nauk.coinfolio.Activities.HomeActivity;
import com.nauk.coinfolio.DataManagers.BalanceManager;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.DataManagers.CurrencyData.CurrencyDetailsList;
import com.nauk.coinfolio.DataManagers.PreferencesManager;
import com.nauk.coinfolio.DataManagers.WatchlistManager;
import com.nauk.coinfolio.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Locale;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_watchlist_homeactivity, container, false);

        refreshLayout = view.findViewById(R.id.swiperefresh);
        currencyDetailsList = new CurrencyDetailsList(getContext());
        preferencesManager = new PreferencesManager(getContext());

        lastTimestamp = 0;

        watchlistManager = new WatchlistManager(getContext());

        updateWatchlist(true);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateWatchlist(false);
            }
        });

        Button addWatchlistButton = view.findViewById(R.id.buttonAddWatchlist);
        addWatchlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent selectionIntent = new Intent(getActivity(), CurrencySelectionActivity.class);
                selectionIntent.putExtra("isWatchList", true);
                startActivity(selectionIntent);
            }
        });

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        updateWatchlist(preferencesManager.mustUpdate());
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

            watchlistManager.updateWatchlist();

            currencyDetailsList.update(new BalanceManager.IconCallBack() {
                @Override
                public void onSuccess() {
                    WatchlistUpdater watchlistUpdater = new WatchlistUpdater();
                    watchlistUpdater.execute();
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

    private void countWatchlist()
    {
        watchlistCounter++;

        if(watchlistCounter >= watchlistManager.getWatchlist().size())
        {
            ((LinearLayout) view.findViewById(R.id.linearLayoutWatchlist)).removeAllViews();

            for(Currency currency : watchlistManager.getWatchlist())
            {
                View card = LayoutInflater.from(getContext()).inflate(R.layout.cardview_watchlist, null);

                ((TextView) card.findViewById(R.id.currencyFluctuationPercentageTextView)).setText(getResources().getString(R.string.currencyPercentagePlaceholder, numberConformer(currency.getDayFluctuationPercentage())));
                ((TextView) card.findViewById(R.id.currencyFluctuationTextView)).setText(getResources().getString(R.string.currencyDollarParenthesisPlaceholder, numberConformer(currency.getDayFluctuation())));
                ((TextView) card.findViewById(R.id.currencyNameTextView)).setText(currency.getName());
                ((TextView) card.findViewById(R.id.currencySymbolTextView)).setText(getResources().getString(R.string.currencySymbolPlaceholder, currency.getSymbol()));
                ((ImageView) card.findViewById(R.id.currencyIcon)).setImageBitmap(currency.getIcon());
                ((TextView) card.findViewById(R.id.currencyValueTextView)).setText(getResources().getString(R.string.currencyDollarPlaceholder, numberConformer(currency.getValue())));

                updateColor(card, currency);

                card.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("coinfolio", "Clicked !");
                    }
                });

                ((LinearLayout) view.findViewById(R.id.linearLayoutWatchlist)).addView(card, 0);
            }

            if(refreshLayout.isRefreshing())
            {
                refreshLayout.setRefreshing(false);
            }
        }
    }

    private void updateColor(View card, Currency currency)
    {
        if(currency.getDayFluctuation() > 0)
        {
            ((TextView) card.findViewById(R.id.currencyFluctuationPercentageTextView)).setTextColor(getResources().getColor(R.color.increase));
            ((TextView) card.findViewById(R.id.currencyFluctuationTextView)).setTextColor(getResources().getColor(R.color.increase));
        }
        else
        {
            ((TextView) card.findViewById(R.id.currencyFluctuationPercentageTextView)).setTextColor(getResources().getColor(R.color.decrease));
            ((TextView) card.findViewById(R.id.currencyFluctuationTextView)).setTextColor(getResources().getColor(R.color.decrease));
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
                    R.mipmap.icon_coinfolio);
            result = Bitmap.createScaledBitmap(result, 50, 50, false);
        }

        callBack.onSuccess(result);
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
            for(Currency currency : watchlistManager.getWatchlist())
            {
                currency.updateHistoryMinutes(getActivity(), new Currency.CurrencyCallBack() {
                    @Override
                    public void onSuccess(final Currency sucessCurrency) {
                        if(getIconUrl(sucessCurrency.getSymbol()) != null)
                        {
                            getBitmapFromURL(getIconUrl(sucessCurrency.getSymbol()), new HomeActivity.IconCallBack() {
                                @Override
                                public void onSuccess(Bitmap bitmapIcon) {
                                    sucessCurrency.setIcon(bitmapIcon);
                                    countWatchlist();
                                }
                            });
                        }
                    }
                });
            }
            return null;
        }
    }

    private String numberConformer(double number)
    {
        String str;

        if(abs(number) > 1)
        {
            str = String.format( Locale.UK, "%.2f", number);
        }
        else
        {
            str = String.format( Locale.UK, "%.4f", number);
        }

        return str;
    }
}
