package com.nauk.moodl.Activities.HomeActivityFragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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

import com.nauk.moodl.Activities.CurrencySelectionActivity;
import com.nauk.moodl.Activities.HomeActivity;
import com.nauk.moodl.DataManagers.BalanceManager;
import com.nauk.moodl.DataManagers.CurrencyData.Currency;
import com.nauk.moodl.DataManagers.CurrencyData.CurrencyCardview;
import com.nauk.moodl.DataManagers.CurrencyData.CurrencyTickerList;
import com.nauk.moodl.DataManagers.PreferencesManager;
import com.nauk.moodl.HideBalanceSwitch;
import com.nauk.moodl.PlaceholderManager;
import com.nauk.moodl.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static com.nauk.moodl.MoodlBox.numberConformer;
import static java.lang.Math.abs;

/**
 * Created by Tiji on 13/04/2018.
 */

public class Summary extends Fragment implements HideBalanceSwitch {

    private LinearLayout currencyLayout;
    private PreferencesManager preferencesManager;
    private BalanceManager balanceManager;
    private SwipeRefreshLayout refreshLayout;
    private Dialog loadingDialog;
    private String defaultCurrency;
    private CurrencyTickerList currencyTickerList;

    private TextView toolbarSubtitle;
    private CollapsingToolbarLayout toolbarLayout;
    private Handler handler;

    private Runnable updateRunnable;
    private Runnable layoutRefresherRunnable;

    private int coinCounter;
    private int iconCounter;
    private float totalValue;
    private boolean detailsChecker;
    private boolean tickersChecker;
    protected float totalFluctuation;
    private long lastTimestamp;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View fragmentView = inflater.inflate(R.layout.fragment_summary_homeactivity, container, false);

        preferencesManager = new PreferencesManager(getActivity());
        balanceManager = new BalanceManager(getActivity());
        currencyTickerList = new CurrencyTickerList(getActivity());

        currencyLayout = fragmentView.findViewById(R.id.currencyListLayout);
        refreshLayout = fragmentView.findViewById(R.id.swiperefreshsummary);
        toolbarSubtitle = fragmentView.findViewById(R.id.toolbarSubtitle);

        resetCounters();

        defaultCurrency = preferencesManager.getDefaultCurrency();

        handler = new Handler();

        initiateUpdateRunnable();

        initiateLayoutRefresherRunnable();

        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateAll(false);
                    }
                }
        );

        handler.postDelayed(updateRunnable, 10000);
        toolbarLayout = fragmentView.findViewById(R.id.toolbar_layout);
        toolbarLayout.setForegroundGravity(Gravity.CENTER);

        setupAddCurrencyButton(fragmentView);

        updateAll(true);

        setupDrawerButton(fragmentView);

        generateSplashScreen();

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

    private void initiateUpdateRunnable()
    {
        updateRunnable = new  Runnable() {
            @Override
            public void run() {
                lastTimestamp = 0;

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
    }

    private void initiateLayoutRefresherRunnable()
    {
        layoutRefresherRunnable = new Runnable() {
            @Override
            public void run() {
                final List<View> currencyView = new ArrayList<>();
                final List<Currency> renderedCurrencies = new ArrayList<>();

                if (balanceManager.getTotalBalance() != null)
                {
                    for (int i = 0; i < balanceManager.getTotalBalance().size(); i++) {
                        final Currency currency = balanceManager.getTotalBalance().get(i);

                        if (!currency.getSymbol().equals("USD") && ((currency.getBalance() * currency.getValue()) >= preferencesManager.getMinimumAmount())) {
                            //currencyView.add(layoutGenerator.getInfoLayout(currency, totalValue, preferencesManager.isBalanceHidden()));
                            renderedCurrencies.add(currency);
                        }
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currencyLayout.removeAllViews();

                        for(int i = 0; i < renderedCurrencies.size(); i++)
                        {
                            //currencyLayout.addView(currencyView.get(i));
                            currencyLayout.addView(new CurrencyCardview(getActivity(), renderedCurrencies.get(i), totalValue, preferencesManager.isBalanceHidden()));
                        }

                        if(loadingDialog.isShowing())
                        {
                            loadingDialog.dismiss();
                        }

                        handler.removeCallbacks(updateRunnable);
                    }
                });
            }
        };
    }

    private void setupAddCurrencyButton(View fragmentView)
    {
        Button addCurrencyButton = fragmentView.findViewById(R.id.buttonAddTransaction);

        addCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addIntent = new Intent(getActivity(), CurrencySelectionActivity.class);

                startActivity(addIntent);
            }
        });
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

        loadingLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.colorPrimaryDark));
        loadingLayout.addView(txtView);
        loadingLayout.addView(progressBar);

        loadingDialog.setContentView(loadingLayout);
        loadingDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();

        displayBalance(preferencesManager.isBalanceHidden());

        if(!defaultCurrency.equals(preferencesManager.getDefaultCurrency()))
        {
            defaultCurrency = preferencesManager.getDefaultCurrency();

            updateAll(true);
        }
        else
        {
            updateAll(preferencesManager.mustUpdateSummary());
        }
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
        /*Snackbar.make(getActivity().findViewById(R.id.snackbar_placer), "Error while updating data", Snackbar.LENGTH_LONG)
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
        tickersChecker = false;

        totalValue = 0;
        totalFluctuation = 0;
    }

    private void adaptView()
    {
        layoutRefresherRunnable.run();
    }

    private void countCoins(boolean isCoin, boolean isDetails, boolean isTickers)
    {
        if(isCoin)
        {
            coinCounter++;
        }

        if(isTickers)
        {
            tickersChecker = true;
        }

        if(isDetails)
        {
            detailsChecker = true;
        }

        if(balanceManager.getTotalBalance() != null)
        {
            if(coinCounter == balanceManager.getTotalBalance().size() && detailsChecker && tickersChecker)
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
            updateHideBalanceTitle(totalFluctuationPercentage);
        }
        else
        {
            updateBalanceDisplayedTitle(totalFluctuationPercentage);
        }
    }

    public void updateBalanceDisplayedTitle(float totalFluctuationPercentage)
    {
        toolbarLayout.setTitle(PlaceholderManager.getValueString(numberConformer(totalValue), getActivity()));
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
            toolbarSubtitle.setText(PlaceholderManager.getValueString(numberConformer(totalValue), getActivity()));
            toolbarSubtitle.setTextColor(-1275068417);
        }
        else
        {
            toolbarSubtitle.setText(PlaceholderManager.getValuePercentageString(numberConformer(totalFluctuation), numberConformer(totalFluctuationPercentage), getActivity()));
        }
    }

    private void updateHideBalanceTitle(float totalFluctuationPercentage)
    {
        toolbarLayout.setTitle(PlaceholderManager.getPercentageString(numberConformer(totalFluctuationPercentage), getActivity()));
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

    @Override
    public void buttonCheckedChange() {
        preferencesManager.switchBalanceHiddenState();
        displayBalance(preferencesManager.isBalanceHidden());
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
            if(!currency.getSymbol().equals("USD") && (currency.getBalance() * currency.getValue()) > preferencesManager.getMinimumAmount())
            {
                currency.setName(balanceManager.getCurrencyName(currency.getSymbol()));
                currency.setId(balanceManager.getCurrencyId(currency.getSymbol()));
                totalValue += currency.getValue() * currency.getBalance();
                totalFluctuation += (currency.getValue() * currency.getBalance()) * (currency.getDayFluctuationPercentage() / 100);
            }
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

                localCurrency.setTickerId(currencyTickerList.getTickerIdForSymbol(localCurrency.getSymbol()));

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

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            refreshLayout.setRefreshing(false);
            new AsyncTask<Void, Integer, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    if(Looper.myLooper() == null)
                    {
                        Looper.prepare();
                    }

                    adaptView();
                    return null;
                }
            }.execute();
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
            Log.d("moodl", "Error while downloading icon");
            result = BitmapFactory.decodeResource(this.getResources(),
                    R.mipmap.ic_launcher_moodl);
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

                getBitmapFromURL(balanceManager.getIconUrl(localCurrency.getSymbol()), new HomeActivity.IconCallBack() {
                    @Override
                    public void onSuccess(Bitmap bitmapIcon) {
                        localCurrency.setIcon(bitmapIcon);
                        countIcons();
                    }
                });
            }

            return null;
        }
    }


    private class DataUpdater extends AsyncTask<Void, Integer, Void>
    {
        private void generateSnackBarError(String error)
        {
            /*View view = getActivity().findViewById(R.id.snackbar_placer);

            switch (error)
            {
                case "com.android.volley.AuthFailureError":
                    preferencesManager.disableHitBTC();
                    Snackbar.make(view, "HitBTC synchronization error : Invalid keys", Snackbar.LENGTH_LONG)
                            .show();

                    refreshLayout.setRefreshing(false);

                    updateAll(true);
                    break;
                case "API-key format invalid.":
                    preferencesManager.disableBinance();
                    Snackbar.make(view, "Binance synchronization error : Invalid keys", Snackbar.LENGTH_LONG)
                            .show();

                    updateAll(true);
                    break;
                case "com.android.volley.NoConnectionError: java.net.UnknownHostException: Unable to resolve host \"api.hitbtc.com\": No address associated with hostname":
                    Snackbar.make(view, "Can't resolve host", Snackbar.LENGTH_LONG)
                            .show();
                    break;
                case "com.android.volley.TimeoutError":
                    break;
                default:
                    Snackbar.make(view, "Unexpected error", Snackbar.LENGTH_LONG)
                            .show();

                    Log.d("moodl", error);

                    updateAll(false);
            }*/
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            currencyTickerList.update(new BalanceManager.IconCallBack() {
                @Override
                public void onSuccess() {
                    countCoins(false, false, true);
                }
            });
            balanceManager.updateDetails(new BalanceManager.IconCallBack() {
                @Override
                public void onSuccess()
                {
                    countCoins(false, true, false);
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
                            balance.get(i).updatePrice(getActivity(), defaultCurrency, new Currency.CurrencyCallBack() {
                                @Override
                                public void onSuccess(Currency currency) {
                                    countCoins(true, false, false);
                                }
                            });
                        }
                    }
                    else
                    {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                countCoins(false, false, false);
                            }
                        });
                    }
                }

                public void onError(String error)
                {
                    generateSnackBarError(error);
                }
            });

            return null;
        }
    }

}
