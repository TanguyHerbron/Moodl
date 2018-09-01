package com.herbron.moodl.Activities.HomeActivityFragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daasuu.ei.Ease;
import com.daasuu.ei.EasingInterpolator;
import com.herbron.moodl.Activities.RecordTransactionActivity;
import com.herbron.moodl.BalanceUpdateInterface;
import com.herbron.moodl.DataNotifiers.CoinmarketcapNotifierInterface;
import com.herbron.moodl.DataNotifiers.CryptocompareNotifierInterface;
import com.herbron.moodl.CurrencyInfoUpdateNotifierInterface;
import com.herbron.moodl.DataManagers.BalanceManager;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.CurrencyData.CurrencyCardview;
import com.herbron.moodl.DataManagers.InfoAPIManagers.CoinmarketCapAPIManager;
import com.herbron.moodl.DataManagers.InfoAPIManagers.CryptocompareApiManager;
import com.herbron.moodl.DataManagers.PreferencesManager;
import com.herbron.moodl.BalanceSwitchManagerInterface;
import com.herbron.moodl.DataNotifiers.BalanceUpdateNotifierInterface;
import com.herbron.moodl.MoodlBox;
import com.herbron.moodl.DataNotifiers.MoodlboxNotifierInterface;
import com.herbron.moodl.Utils.PlaceholderUtils;
import com.herbron.moodl.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.herbron.moodl.MoodlBox.getColor;
import static com.herbron.moodl.MoodlBox.getDrawable;
import static com.herbron.moodl.MoodlBox.getIconDominantColor;
import static com.herbron.moodl.MoodlBox.numberConformer;
import static java.lang.Math.abs;

/**
 * Created by Tiji on 13/04/2018.
 */

public class Summary extends Fragment implements BalanceSwitchManagerInterface, BalanceUpdateNotifierInterface, CryptocompareNotifierInterface, CoinmarketcapNotifierInterface {

    private LinearLayout currencyLayout;
    private PreferencesManager preferencesManager;
    private BalanceManager balanceManager;
    private SwipeRefreshLayout refreshLayout;
    private Dialog loadingDialog;
    private String defaultCurrency;
    private CoinmarketCapAPIManager coinmarketCapAPIManager;

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

    private BalanceUpdateInterface balanceUpdateInterface;
    private CryptocompareApiManager cryptocompareApiManager;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View fragmentView = inflater.inflate(R.layout.homeactivity_fragment_summary, container, false);

        preferencesManager = new PreferencesManager(getActivity());
        balanceManager = new BalanceManager(getActivity());
        coinmarketCapAPIManager = CoinmarketCapAPIManager.getInstance(getActivity());
        cryptocompareApiManager = CryptocompareApiManager.getInstance(getActivity());

        currencyLayout = fragmentView.findViewById(R.id.currencyListLayout);
        refreshLayout = fragmentView.findViewById(R.id.swiperefreshsummary);
        toolbarSubtitle = fragmentView.findViewById(R.id.toolbarSubtitle);

        resetCounters();

        setListener((BalanceUpdateInterface) getActivity());

        defaultCurrency = preferencesManager.getDefaultCurrency();

        cryptocompareApiManager.addListener(this);
        coinmarketCapAPIManager.addListener(this);

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

    public void setListener(BalanceUpdateInterface balanceUpdateInterface)
    {
        this.balanceUpdateInterface = balanceUpdateInterface;
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
                    Log.d("moodl", "Error > Refresh out of time");
                }

                if (loadingDialog.isShowing())
                {
                    loadingDialog.dismiss();

                    showErrorSnackbar();
                    Log.d("moodl", "Error > Refresh out of time");
                }
            }
        };
    }

    private void initiateLayoutRefresherRunnable()
    {
        layoutRefresherRunnable = new Runnable() {
            @Override
            public void run() {
            }
        };
    }

    private void setupAddCurrencyButton(View fragmentView)
    {
        Button addCurrencyButton = fragmentView.findViewById(R.id.buttonAddTransaction);

        addCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addIntent = new Intent(getActivity(), RecordTransactionActivity.class);

                startActivity(addIntent);
            }
        });
    }

    private void generateSplashScreen()
    {
        loadingDialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        Random random = new Random();

        LinearLayout splashLayout = (LinearLayout) LayoutInflater.from(getActivity().getBaseContext()).inflate(R.layout.splash_screen, null, true);
        LinearLayout animatedLayout = splashLayout.findViewById(R.id.animatedViewsLayout);

        File cacheDir = new File(getActivity().getBaseContext().getCacheDir().getAbsolutePath());
        File[] cacheFiles = cacheDir.listFiles();

        if(cacheFiles.length > 4)
        {
            for(int i = 0; i < 4; i++)
            {
                File cachedIcon = null;

                while(cachedIcon == null || cachedIcon.isDirectory())
                {
                    cachedIcon = cacheFiles[random.nextInt(cacheFiles.length)];
                }

                Bitmap icon = BitmapFactory.decodeFile(cachedIcon.getAbsolutePath());

                Bitmap result = Bitmap.createBitmap(150, 150, icon.getConfig());

                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(ContextCompat.getColor(getActivity().getBaseContext(), R.color.white));

                Canvas canvas = new Canvas(result);
                canvas.drawCircle(result.getHeight()/2, result.getWidth()/2, 75, paint);
                canvas.drawBitmap(Bitmap.createScaledBitmap(icon, 100, 100, false), result.getHeight()/2 - 50, result.getWidth()/2 - 50, null);

                ((ImageView) animatedLayout.getChildAt(i)).setImageBitmap(result);

                ObjectAnimator animator = ObjectAnimator.ofFloat(animatedLayout.getChildAt(i), "translationY", 0, -100, 0);
                animator.setInterpolator(new EasingInterpolator(Ease.CIRC_IN_OUT));
                animator.setStartDelay(i*200);
                animator.setDuration(1500);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.start();
            }
        }
        else
        {
            /*ImageView appNameImageView = splashLayout.findViewById(R.id.appNameImageView);
            appNameImageView.setVisibility(View.VISIBLE);*/
            animatedLayout.setVisibility(View.GONE);
        }

        loadingDialog.setContentView(splashLayout);
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
        Snackbar.make(getActivity().findViewById(R.id.content_frame), getString(R.string.error_update_data), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.update), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .show();
    }

    private void resetCounters()
    {
        coinCounter = 0;
        iconCounter = 0;
    }

    private void adaptView(float totalValue, float totalFluctuation)
    {
        this.totalValue = totalValue;
        this.totalFluctuation = totalFluctuation;
        final List<Currency> renderedCurrencies = new ArrayList<>();

        if (balanceManager.getTotalBalance() != null)
        {
            for (int i = 0; i < balanceManager.getTotalBalance().size(); i++) {
                final Currency currency = balanceManager.getTotalBalance().get(i);

                if ((Math.abs(currency.getBalance() * currency.getValue()) >= preferencesManager.getMinimumAmount())) {
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
                    currencyLayout.addView(new CurrencyCardview(getActivity(), renderedCurrencies.get(i), getActivity(), preferencesManager.isBalanceHidden()));
                }

                if(loadingDialog.isShowing())
                {
                    loadingDialog.dismiss();
                }

                updateTitle();

                handler.removeCallbacks(updateRunnable);
            }
        });
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
            if(balanceManager.getTotalBalance().size() == 0)
            {
                updateNoBalance();
            }
            else
            {
                if(coinCounter >= balanceManager.getTotalBalance().size() && detailsChecker && tickersChecker)
                {
                    UiHeavyLoadCalculator uiHeavyLoadCalculator = new UiHeavyLoadCalculator();
                    uiHeavyLoadCalculator.setOnUiEndListener(new UiHeavyLoadCalculator.OnUiEndListener() {
                        @Override
                        public void onEnd(float totalValue, float totalFluctuation) {
                            refreshLayout.setRefreshing(false);

                            adaptView(totalValue, totalFluctuation);
                        }
                    });
                    uiHeavyLoadCalculator.execute(getActivity().getBaseContext(), balanceManager, coinmarketCapAPIManager);
                }
            }
        }
    }

    private void updateNoBalance()
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);

                currencyLayout.removeAllViews();

                if(loadingDialog.isShowing())
                {
                    loadingDialog.dismiss();
                }

                updateTitle();
            }
        });
    }

    private void computeTotalValue()
    {
        for(int i = 0; i < currencyLayout.getChildCount(); i++)
        {
            if(currencyLayout.getChildAt(i) instanceof CurrencyCardview)
            {
                ((CurrencyCardview) currencyLayout.getChildAt(i)).updateOwnedValues(totalValue, preferencesManager.isBalanceHidden());
            }
        }
    }

    protected void updateTitle()
    {
        computeTotalValue();

        float totalFluctuationPercentage = totalFluctuation / (totalValue - totalFluctuation) * 100;

        if(preferencesManager.isBalanceHidden())
        {
            if(Double.isNaN(totalFluctuationPercentage))
            {
                updateHideBalanceTitle(0);
                balanceUpdateInterface.onBalanceUpdated(0);
            }
            else
            {
                updateHideBalanceTitle(totalFluctuationPercentage);
                balanceUpdateInterface.onBalanceUpdated(totalFluctuationPercentage);
            }
        }
        else
        {
            if(Double.isNaN(totalFluctuation))
            {
                updateBalanceDisplayedTitle(0);
                balanceUpdateInterface.onBalanceUpdated(0);
            }
            else
            {
                updateBalanceDisplayedTitle(totalFluctuationPercentage);
                balanceUpdateInterface.onBalanceUpdated(totalValue);
            }
        }
    }

    public void updateBalanceDisplayedTitle(float totalFluctuationPercentage)
    {
        toolbarLayout.setTitle(PlaceholderUtils.getValueString(numberConformer(totalValue), getActivity()));
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
            toolbarSubtitle.setText(PlaceholderUtils.getValueString(numberConformer(totalValue), getActivity()));
            toolbarSubtitle.setTextColor(-1275068417);
        }
        else
        {
            toolbarSubtitle.setText(PlaceholderUtils.getValuePercentageString(numberConformer(totalFluctuation), numberConformer(totalFluctuationPercentage), getActivity()));
        }
    }

    private void updateHideBalanceTitle(float totalFluctuationPercentage)
    {
        toolbarLayout.setTitle(PlaceholderUtils.getPercentageString(numberConformer(totalFluctuationPercentage), getActivity()));
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

    @Override
    public void onBalanceDataUpdated() {
        final List<Currency> balance = balanceManager.getTotalBalance();

        if(balance != null && balance.size() > 0)
        {
            for(int i = 0; i < balance.size(); i++)
            {
                balance.get(i).updatePrice(getActivity(), defaultCurrency, new CurrencyInfoUpdateNotifierInterface() {
                    @Override
                    public void onTimestampPriceUpdated(String price) {

                    }

                    @Override
                    public void onHistoryDataUpdated() {

                    }

                    @Override
                    public void onPriceUpdated(Currency currency) {
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

    @Override
    public void onBalanceError(String error)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                generateSnackBarError(error);
            }
        });
    }

    private void generateSnackBarError(String error)
    {
            View view = getActivity().findViewById(R.id.content_frame);

            switch (error)
            {
                case "com.android.volley.AuthFailureError":
                    preferencesManager.disableHitBTC();
                    Snackbar.make(view, getString(R.string.invalid_keys_hitbtc), Snackbar.LENGTH_LONG)
                            .show();

                    refreshLayout.setRefreshing(false);

                    updateAll(true);
                    break;
                case "API-key format invalid.":
                    preferencesManager.disableBinance();
                    Snackbar.make(view, getString(R.string.invalid_keys_binance), Snackbar.LENGTH_LONG)
                            .show();

                    updateAll(true);
                    break;
                case "com.android.volley.NoConnectionError: java.net.UnknownHostException: Unable to resolve host \"api.hitbtc.com\": No address associated with hostname":
                    Snackbar.make(view, getString(R.string.cannot_resole_host), Snackbar.LENGTH_LONG)
                            .show();
                    break;
                case "com.android.volley.TimeoutError":
                    break;
                default:
                    Snackbar.make(view, R.string.unexpected, Snackbar.LENGTH_LONG)
                            .show();

                    Log.d("moodl", error);

                    updateAll(false);
            }
    }

    @Override
    public void onDetailsUpdated() {
        countCoins(false, true, false);
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
        countCoins(false, false, true);
    }

    private static class UiHeavyLoadCalculator extends AsyncTask<Object, Integer, Void>
    {

        private float totalValue = 0;
        private float totalFluctuation = 0;

        private BalanceManager balanceManager;
        private CoinmarketCapAPIManager coinmarketCapAPIManager;

        private OnUiEndListener onUiEndListener;

        public void setOnUiEndListener(OnUiEndListener onUiEndListener)
        {
            this.onUiEndListener = onUiEndListener;
        }

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

        @Override
        protected Void doInBackground(Object... params)
        {
            if(Looper.myLooper() == null)
            {
                Looper.prepare();
            }

            Context baseContext = (Context) params[0];

            balanceManager = (BalanceManager) params[1];
            coinmarketCapAPIManager = (CoinmarketCapAPIManager) params[2];

            balanceManager.sortCoins();

            for(int i = 0; i < balanceManager.getTotalBalance().size(); i++)
            {
                final Currency localCurrency = balanceManager.getTotalBalance().get(i);

                localCurrency.setTickerId(coinmarketCapAPIManager.getTickerIdForSymbol(localCurrency.getSymbol()));

                localCurrency.setChartColor(getIconDominantColor(baseContext, localCurrency.getIcon()));

                loadCurrency(localCurrency);

                totalValue += localCurrency.getValue() * localCurrency.getBalance();
                totalFluctuation += localCurrency.getValue() * localCurrency.getBalance() * (localCurrency.getDayFluctuationPercentage() / 100);

                balanceManager.getTotalBalance().set(i, localCurrency);
            }

            return null;
        }

        private void loadCurrency(Currency currency)
        {
            currency.setName(balanceManager.getCurrencyName(currency.getSymbol()));
            currency.setId(balanceManager.getCurrencyId(currency.getSymbol()));
        }

        @Override
        protected void onPostExecute(Void result)
        {
            onUiEndListener.onEnd(totalValue, totalFluctuation);
        }

        public interface OnUiEndListener
        {
            void onEnd(float totalValue, float totalFluctuation);
        }
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

    private class DataUpdater extends AsyncTask<Void, Integer, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            if(!coinmarketCapAPIManager.isUpToDate())
            {
                coinmarketCapAPIManager.updateListing();
            }
            else
            {
                countCoins(false, false, true);
            }

            if(!cryptocompareApiManager.isDetailsUpToDate())
            {
                cryptocompareApiManager.updateDetails();
            }
            else
            {
                countCoins(false, true, false);
            }

            balanceManager.updateTotalBalance();

            return null;
        }
    }

}
