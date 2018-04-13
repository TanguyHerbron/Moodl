package com.nauk.coinfolio.Activities.HomeActivityFragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nauk.coinfolio.Activities.CurrencySelectionActivity;
import com.nauk.coinfolio.Activities.HomeActivity;
import com.nauk.coinfolio.Activities.SettingsActivity;
import com.nauk.coinfolio.DataManagers.BalanceManager;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.DataManagers.PreferencesManager;
import com.nauk.coinfolio.LayoutManagers.HomeLayoutGenerator;
import com.nauk.coinfolio.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * Created by Tiji on 13/04/2018.
 */

public class Summary extends Fragment {

    private boolean isDetailed;
    private LinearLayout currencyLayout;
    private PreferencesManager preferencesManager;
    private BalanceManager balanceManager;
    private HomeLayoutGenerator layoutGenerator;
    private View view;
    private SwipeRefreshLayout refreshLayout;
    private Dialog loadingDialog;

    private TextView toolbarSubtitle;
    private CollapsingToolbarLayout toolbarLayout;
    private Handler handler;

    private Runnable updateRunnable;

    private int coinCounter;
    private int iconCounter;
    private float totalValue;
    private boolean detailsChecker;
    protected float totalFluctuation;
    private long lastTimestamp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_summary_homeactivity, container, false);

        currencyLayout = view.findViewById(R.id.currencyListLayout);
        preferencesManager = new PreferencesManager(getActivity());
        balanceManager = new BalanceManager(getActivity());
        layoutGenerator = new HomeLayoutGenerator(getActivity());
        refreshLayout = view.findViewById(R.id.swiperefresh);
        toolbarSubtitle = getActivity().findViewById(R.id.toolbarSubtitle);
        toolbarLayout = getActivity().findViewById(R.id.toolbar_layout);

        isDetailed = preferencesManager.getDetailOption();

        totalValue = 0;
        totalFluctuation = 0;
        lastTimestamp = 0;

        handler = new Handler();
        updateRunnable = new  Runnable() {
            @Override
            public void run() {
                if (refreshLayout.isRefreshing())
                {
                    refreshLayout.setRefreshing(false);

                    showErrorSnackbar();
                }

                if (loadingDialog.isShowing())
                {
                    loadingDialog.dismiss();

                    showErrorSnackbar();
                }
            }
        };

        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateAll(false);
                    }

                }
        );

        handler.postDelayed(updateRunnable, 10000);

        Button addCurrencyButton = view.findViewById(R.id.buttonAddTransaction);

        addCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addIntent = new Intent(getActivity(), CurrencySelectionActivity.class);

                startActivity(addIntent);
            }
        });

        ImageButton detailsButton = getActivity().findViewById(R.id.switch_button);
        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchView();
            }
        });

        displayBalance(preferencesManager.isBalanceHidden());

        updateTitle();

        generateSplashScreen();

        updateAll(true);

        return view;
    }

    private void updateAll(boolean mustUpdate)
    {
        if(System.currentTimeMillis()/1000 - lastTimestamp > 60 || mustUpdate)
        {
            if(!refreshLayout.isRefreshing())
            {
                refreshLayout.setRefreshing(true);
            }

            lastTimestamp = System.currentTimeMillis() / 1000;
            balanceManager.updateExchangeKeys();
            refreshLayout.setRefreshing(true);

            resetCounters();
            DataUpdater updater = new DataUpdater();
            updater.execute();

        }
        else
        {
            if(refreshLayout.isRefreshing())
            {
                refreshLayout.setRefreshing(false);
            }
        }
    }

    private void showErrorSnackbar()
    {
        /*Snackbar.make(getActivity().findViewById(R.id.viewFlipperSummary), "Error while updating data", Snackbar.LENGTH_LONG)
                .setAction("Update", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .show();*/
    }

    private void resetCounters()
    {
        coinCounter = 0;
        iconCounter = 0;
        detailsChecker = false;

        totalValue = 0;
        totalFluctuation = 0;
    }

    private void generateSplashScreen()
    {
        LinearLayout loadingLayout = new LinearLayout(getActivity());

        loadingLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        loadingLayout.setGravity(Gravity.CENTER);
        loadingLayout.setOrientation(LinearLayout.VERTICAL);

        loadingDialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        TextView txtView = new TextView(getActivity());
        txtView.setText("Loading data...");
        txtView.setTextSize(20);
        txtView.setGravity(Gravity.CENTER);
        txtView.setTextColor(this.getResources().getColor(R.color.cardview_light_background));

        ProgressBar progressBar = new ProgressBar(getActivity());
        progressBar.setIndeterminate(true);

        loadingLayout.setBackgroundColor(this.getResources().getColor(R.color.colorPrimaryDark));
        loadingLayout.addView(txtView);
        loadingLayout.addView(progressBar);

        loadingDialog.setContentView(loadingLayout);
        loadingDialog.show();
    }

    private void switchView()
    {
        if(isDetailed)
        {
            isDetailed = false;

            adaptView();
        }
        else
        {
            isDetailed = true;

            adaptView();
        }
    }

    private void adaptView()
    {
        currencyLayout.removeAllViews();

        for(int i = 0; i < balanceManager.getTotalBalance().size(); i++)
        {
            final Currency currency = balanceManager.getTotalBalance().get(i);

            if(!currency.getSymbol().equals("USD") && ((currency.getBalance() * currency.getValue()) > 0.001 || currency.getHistoryMinutes() == null))
            {
                currencyLayout.addView(layoutGenerator.getInfoLayout(currency, isDetailed, totalValue, preferencesManager.isBalanceHidden()));
            }
        }
    }

    private void countCoins(boolean isCoin, boolean isDetails)
    {
        if(isCoin)
        {
            coinCounter++;
        }

        if(isDetails)
        {
            detailsChecker = true;
        }

        if(balanceManager.getTotalBalance() != null)
        {
            if(coinCounter == balanceManager.getTotalBalance().size() && detailsChecker)
            {
                IconDownloader iconDownloader = new IconDownloader();
                iconDownloader.execute();
            }
            else
            {
                if(balanceManager.getTotalBalance().size() == 0)
                {
                    countIcons();
                }
            }
        }
    }


    private void countIcons()
    {
        int offset = 0;

        for(int i = 0; i < balanceManager.getTotalBalance().size(); i++)
        {
            if(balanceManager.getTotalBalance().get(i).getSymbol().equals("USD"))
            {
                offset++;
            }
        }

        iconCounter++;

        if(balanceManager.getTotalBalance() != null)
        {
            if(balanceManager.getTotalBalance().size() == 0)
            {
                updateNoBalance();
            }
            else
            {
                if(iconCounter == balanceManager.getTotalBalance().size() - offset)
                {
                    Log.d(getResources().getString(R.string.debug), "Loading heavy");

                    UiHeavyLoadCalculator uiHeavyLoadCalculator = new UiHeavyLoadCalculator();
                    uiHeavyLoadCalculator.execute();
                }
            }
        }
    }

    private void updateNoBalance()
    {
        refreshLayout.setRefreshing(false);

        currencyLayout.removeAllViews();

        if(loadingDialog.isShowing())
        {
            loadingDialog.dismiss();
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                updateTitle();
            }
        });
    }

    protected void updateTitle()
    {
        float totalFluctuationPercentage = totalFluctuation / (totalValue - totalFluctuation) * 100;

        if(preferencesManager.isBalanceHidden())
        {
            toolbarLayout.setTitle(getResources().getString(R.string.currencyPercentagePlaceholder, String.format("%.2f", totalFluctuationPercentage)));
            toolbarSubtitle.setVisibility(View.GONE);

            if(totalFluctuation > 0)
            {
                toolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.increase));
                toolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.increase));
            }
            else
            {
                toolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.decrease));
                toolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.decrease));
            }
        }
        else
        {
            toolbarLayout.setTitle(getResources().getString(R.string.currencyDollarPlaceholder, String.format("%.2f", totalValue)));
            toolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
            toolbarLayout.setExpandedTitleColor(Color.WHITE);

            toolbarSubtitle.setVisibility(View.VISIBLE);

            if(totalFluctuation > 0)
            {
                toolbarSubtitle.setTextColor(getResources().getColor(R.color.increase));
            }
            else
            {
                toolbarSubtitle.setTextColor(getResources().getColor(R.color.decrease));
            }

            if(totalFluctuation == 0)
            {
                toolbarSubtitle.setText(getResources().getString(R.string.currencyDollarPlaceholder, "0.00"));
                toolbarSubtitle.setTextColor(-1275068417);

            }
            else
            {
                toolbarSubtitle.setText("US$" + String.format("%.2f", totalFluctuation) + " (" + String.format("%.2f", totalFluctuationPercentage) + "%)");
            }
        }
    }

    private class UiHeavyLoadCalculator extends AsyncTask<Void, Integer, Void>
    {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            totalValue = 0;
            totalFluctuation = 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
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

        private void loadCurrency(Currency currency)
        {
            if(!currency.getSymbol().equals("USD") && (currency.getBalance() * currency.getValue()) > 0.001)
            {
                currency.setName(balanceManager.getCurrencyName(currency.getSymbol()));
                currency.setId(balanceManager.getCurrencyId(currency.getSymbol()));
                totalValue += currency.getValue() * currency.getBalance();
                totalFluctuation += (currency.getValue() * currency.getBalance()) * (currency.getDayFluctuationPercentage() / 100);
            }
        }

        private void refreshCurrencyList()
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currencyLayout.removeAllViews();

                    for(int i = 0; i < balanceManager.getTotalBalance().size(); i++)
                    {
                        Currency currency = balanceManager.getTotalBalance().get(i);

                        if(!currency.getSymbol().equals("USD") && (currency.getBalance() * currency.getValue()) > 0.001) {
                            currencyLayout.addView(layoutGenerator.getInfoLayout(currency, isDetailed, totalValue, preferencesManager.isBalanceHidden()));
                        }
                    }
                }
            });
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            if(Looper.myLooper() == null)
            {
                Looper.prepare();
            }

            balanceManager.sortCoins();

            for(int i = 0; i < balanceManager.getTotalBalance().size(); i++)
            {
                final Currency localCurrency = balanceManager.getTotalBalance().get(i);

                updateChartColor(localCurrency);

                loadCurrency(localCurrency);

                balanceManager.getTotalBalance().set(i, localCurrency);
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateTitle();
                }
            });

            if(loadingDialog.isShowing())
            {
                loadingDialog.dismiss();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            refreshLayout.setRefreshing(false);
            refreshCurrencyList();
            totalValue = totalValue;
            handler.removeCallbacks(updateRunnable);
        }
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

    private void displayBalance(boolean hideBalance)
    {
        updateTitle();

        if(hideBalance)
        {
            for(int i = 0; i < currencyLayout.getChildCount(); i++)
            {
                currencyLayout.getChildAt(i).findViewById(R.id.currencyPortfolioDominance).setVisibility(View.VISIBLE);
                currencyLayout.getChildAt(i).findViewById(R.id.percentageOwnedTextView).setVisibility(View.VISIBLE);
                currencyLayout.getChildAt(i).findViewById(R.id.currencyOwnedInfoLayout).setVisibility(View.GONE);
            }
        }
        else
        {
            for(int i = 0; i < currencyLayout.getChildCount(); i++)
            {
                currencyLayout.getChildAt(i).findViewById(R.id.currencyPortfolioDominance).setVisibility(View.INVISIBLE);
                currencyLayout.getChildAt(i).findViewById(R.id.percentageOwnedTextView).setVisibility(View.GONE);
                currencyLayout.getChildAt(i).findViewById(R.id.currencyOwnedInfoLayout).setVisibility(View.VISIBLE);
            }
        }
    }

    private class IconDownloader extends AsyncTask<Void, Integer, Void>
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
            for (int i = 0; i < balanceManager.getTotalBalance().size(); i++)
            {
                final Currency localCurrency = balanceManager.getTotalBalance().get(i);

                if(balanceManager.getIconUrl(localCurrency.getSymbol()) != null)
                {
                    getBitmapFromURL(balanceManager.getIconUrl(localCurrency.getSymbol()), new HomeActivity.IconCallBack() {
                        @Override
                        public void onSuccess(Bitmap bitmapIcon) {
                            localCurrency.setIcon(bitmapIcon);
                            countIcons();
                        }
                    });
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {

        }
    }


    private class DataUpdater extends AsyncTask<Void, Integer, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            balanceManager.updateDetails(new BalanceManager.IconCallBack() {
                @Override
                public void onSuccess()
                {
                    countCoins(false, true);
                }
            });

            balanceManager.updateTotalBalance(new BalanceManager.VolleyCallBack() {
                @Override
                public void onSuccess() {

                    final List<Currency> balance = balanceManager.getTotalBalance();

                    if(balanceManager.getTotalBalance().size() > 0)
                    {
                        for(int i = 0; i < balanceManager.getTotalBalance().size(); i++)
                        {
                            balance.get(i).updateHistoryMinutes(getActivity(), new Currency.CurrencyCallBack() {
                                @Override
                                public void onSuccess(Currency currency) {
                                    countCoins(true, false);
                                }
                            });
                        }
                    }
                    else
                    {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                countCoins(false, false);
                            }
                        });
                    }
                }

                public void onError(String error)
                {
                    /*switch (error)
                    {
                        case "com.android.volley.AuthFailureError":
                            preferencesManager.disableHitBTC();
                            Snackbar.make(findViewById(R.id.viewFlipperSummary), "HitBTC synchronization error : Invalid keys", Snackbar.LENGTH_LONG)
                                    .show();
                            refreshLayout.setRefreshing(false);
                            updateAll(true);
                            break;
                        case "API-key format invalid.":
                            preferencesManager.disableBinance();
                            Snackbar.make(findViewById(R.id.viewFlipperSummary), "Binance synchronization error : Invalid keys", Snackbar.LENGTH_LONG)
                                    .show();
                            updateAll(true);
                            break;
                        default:
                            Snackbar.make(findViewById(R.id.viewFlipperSummary), "Unexpected error", Snackbar.LENGTH_LONG)
                                    .show();
                            Log.d("coinfolio", error);
                            updateAll(true);
                    }*/
                }
            });

            return null;
        }
    }

}
