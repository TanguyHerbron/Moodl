package com.nauk.coinfolio.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;
import com.nauk.coinfolio.DataManagers.BalanceManager;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.DataManagers.PreferencesManager;
import com.nauk.coinfolio.LayoutManagers.HomeLayoutGenerator;
import com.nauk.coinfolio.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

//Use WilliamChart for charts https://github.com/diogobernardino/WilliamChart

//Auto refresh with predefined intervals
//Adding manually currencies (date, purchased price)
//Multiple portfolio (exchanges & custom)
//Add currency details (market cap, 1h, 3h, 1d, 3d, 1w, 1m, 3m, 1y)
//Add roadmap to buy a coin
//Add reddit link ?
//

public class HomeActivity extends AppCompatActivity {

    private PreferencesManager preferencesManager;
    private HomeLayoutGenerator layoutGenerator;
    private BalanceManager balanceManager;

    private int coinCounter;
    private int iconCounter;
    private long lastTimestamp;
    private boolean detailsChecker;
    private boolean isDetailed;

    private CollapsingToolbarLayout toolbarLayout;
    private SwipeRefreshLayout refreshLayout;
    private LinearLayout currencyLayout;
    private TextView toolbarSubtitle;
    private Dialog loadingDialog;
    private Handler handler;
    private Runnable updateRunnable;
    private ViewFlipper viewFlipper;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_something:

                    //((FloatingActionButton) findViewById(R.id.floatingAddButton)).hide();
                    return true;
                case R.id.navigation_view_list:
                    //((FloatingActionButton) findViewById(R.id.floatingAddButton)).show();
                    //viewFlipper.setDisplayedChild(1);
                    return true;
                case R.id.navigation_market_cap:
                    //((FloatingActionButton) findViewById(R.id.floatingAddButton)).hide();
                    //viewFlipper.setDisplayedChild(2);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**Interface setup**/

        //Setup main interface
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_currency_summary);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        generateSplash();

        //Objects initializatoin
        preferencesManager = new PreferencesManager(this);
        layoutGenerator = new HomeLayoutGenerator(this);
        balanceManager = new BalanceManager(this);
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

        isDetailed = preferencesManager.getDetailOption();

        //Layouts setup
        refreshLayout = findViewById(R.id.swiperefresh);
        toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarSubtitle = findViewById(R.id.toolbarSubtitle);
        currencyLayout = findViewById(R.id.currencyListLayout);
        viewFlipper = findViewById(R.id.viewFlipperSummary);
        viewFlipper.setDisplayedChild(1);

        ImageButton addCurrencyButton = findViewById(R.id.floatingAddButton);
        ImageButton detailsButton = findViewById(R.id.switch_button);
        ImageButton settingsButton = findViewById(R.id.settings_button);

        toolbarLayout.setExpandedTitleGravity(Gravity.CENTER);
        toolbarLayout.setCollapsedTitleGravity(Gravity.CENTER);
        toolbarLayout.setForegroundGravity(Gravity.CENTER);
        toolbarLayout.setTitle("US$0.00");

        toolbarSubtitle.setText("US$0.00");

        /*BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation_home);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_view_list);
        navigation.setFitsSystemWindows(true);
        navigation.setItemBackgroundResource(R.color.colorAccent);*/

        //Events setup
        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchView();
            }
        });

        settingsButton.setBackground(this.getResources().getDrawable(R.drawable.ic_settings_black_24dp));
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingIntent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(settingIntent);
            }
        });

        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateAll(false);
                    }
                }
        );

        addCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addIntent = new Intent(HomeActivity.this, CurrencySelectionActivity.class);

                String[] symbolList = new String[balanceManager.getCurrenciesSymbol().size()];
                symbolList = balanceManager.getCurrenciesSymbol().toArray(symbolList);
                String[] nameList = new String[balanceManager.getCurrenciesName().size()];
                nameList = balanceManager.getCurrenciesName().toArray(nameList);

                addIntent.putExtra("currencyListSymbols", symbolList);
                addIntent.putExtra("currencyListNames", nameList);

                startActivity(addIntent);
            }
        });

        updateViewButtonIcon();

        lastTimestamp = 0;

        setupNavBar(savedInstanceState);
    }

    private void setupNavBar(Bundle savedInstanceState)
    {
        final SpaceNavigationView spaceNavigationView = (SpaceNavigationView) findViewById(R.id.space);
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem("Charts", R.drawable.ic_show_chart_black_24dp));
        spaceNavigationView.addSpaceItem(new SpaceItem("Market Cap.", R.drawable.ic_pie_chart_black_24dp));
        spaceNavigationView.setSpaceBackgroundColor(getResources().getColor(R.color.colorPrimary));
        spaceNavigationView.setCentreButtonIcon(R.drawable.ic_view_list_white_24dp);
        spaceNavigationView.setCentreButtonColor(getResources().getColor(R.color.colorAccent));
        spaceNavigationView.setCentreButtonIconColorFilterEnabled(false);
        spaceNavigationView.changeCurrentItem(-1);

        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                //Toast.makeText(MainActivity.this,"onCentreButtonClick", Toast.LENGTH_SHORT).show();
                ((FloatingActionButton) findViewById(R.id.floatingAddButton)).show();
                SpaceNavigationView nav = findViewById(R.id.space);

                nav.changeCurrentItem(-1);

                ((NestedScrollView) findViewById(R.id.nestedScrollViewLayout)).setNestedScrollingEnabled(true);
                ((AppBarLayout) findViewById(R.id.app_bar)).setExpanded(true, true);

                findViewById(R.id.switch_button).setVisibility(View.VISIBLE);

                viewFlipper.setDisplayedChild(1);
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                ((FloatingActionButton) findViewById(R.id.floatingAddButton)).hide();
                ((SpaceNavigationView) findViewById(R.id.space)).setCentreButtonIcon(R.drawable.ic_view_list_white_24dp);

                //0 : Unknown
                //1 : Market cap
                ((NestedScrollView) findViewById(R.id.nestedScrollViewLayout)).setNestedScrollingEnabled(false);
                ((AppBarLayout) findViewById(R.id.app_bar)).setExpanded(false, true);

                findViewById(R.id.switch_button).setVisibility(View.GONE);



                viewFlipper.setDisplayedChild(itemIndex * 2);
            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {
                //Toast.makeText(MainActivity.this, itemIndex + " " + itemName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ((SpaceNavigationView) findViewById(R.id.space)).onSaveInstanceState(outState);
    }

    private void showErrorSnackbar()
    {
        Snackbar.make(findViewById(R.id.currencyListLayout), "Error while updating data", Snackbar.LENGTH_LONG)
                .setAction("Update", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateAll(true);
                    }
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();

        updateAll(intent.getBooleanExtra("update", false));
        ((SpaceNavigationView) findViewById(R.id.space)).changeCenterButtonIcon(R.drawable.ic_view_list_white_24dp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_currency_summary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id)
        {
            /*case R.id.action_settings:
                Log.d(this.getResources().getString(R.string.debug), "Setting button toggled");
                break;*/
        }

        return super.onOptionsItemSelected(item);
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
                //currencyLayout.addView(layoutGenerator.getInfoLayout(currency));
                currencyLayout.addView(layoutGenerator.getInfoLayout(currency, isDetailed));
            }
        }

        updateViewButtonIcon();
    }



    private void updateAll(boolean mustUpdate)
    {
        if(System.currentTimeMillis()/1000 - lastTimestamp > 60 || mustUpdate)
        {
            lastTimestamp = System.currentTimeMillis() / 1000;
            balanceManager.updateExchangeKeys();
            refreshLayout.setRefreshing(true);

            resetCounters();
            DataUpdater updater = new DataUpdater();
            updater.execute();

            handler.postDelayed(updateRunnable, 10000);
        }
        else
        {
            if(refreshLayout.isRefreshing())
            {
                refreshLayout.setRefreshing(false);
            }
        }
    }

    private void resetCounters()
    {
        coinCounter = 0;
        iconCounter = 0;
        detailsChecker = false;
    }

    private void getBitmapFromURL(String src, IconCallBack callBack) {
        Bitmap result;

        Log.d("coinfolio", "Downloading bitmap");

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
            if(iconCounter == balanceManager.getTotalBalance().size() - offset)
            {
                if(balanceManager.getTotalBalance().size() == 0)
                {
                    updateNoBalance();
                }
                else
                {
                    Log.d("coinfolio", "Loading heavy");

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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toolbarLayout.setTitle("US$0.00");

                toolbarSubtitle.setText("US$0.00");

                toolbarSubtitle.setTextColor(-1275068417);
            }
        });
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
                for (int i = 0; i < balanceManager.getTotalBalance().size(); i++)
                {
                    final Currency localCurrency = balanceManager.getTotalBalance().get(i);

                    if(balanceManager.getIconUrl(localCurrency.getSymbol()) != null)
                    {
                        getBitmapFromURL(balanceManager.getIconUrl(localCurrency.getSymbol()), new IconCallBack() {
                            @Override
                            public void onSuccess(Bitmap bitmapIcon) {
                                localCurrency.setIcon(bitmapIcon);
                                countIcons();
                            }
                        });
                    }
                }
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

    private void updateViewButtonIcon()
    {
        ImageButton imgButton = findViewById(R.id.switch_button);

        imgButton.setBackgroundColor(this.getResources().getColor(R.color.buttonColor));

        if(isDetailed)
        {
            imgButton.setBackground(this.getResources().getDrawable(R.drawable.ic_unfold_less_black_24dp));
            preferencesManager.setDetailOption(true);
        }
        else
        {
            imgButton.setBackground(this.getResources().getDrawable(R.drawable.ic_details_black_24dp));
            preferencesManager.setDetailOption(false);
        }
    }

    private void generateSplash()
    {
        LinearLayout loadingLayout = new LinearLayout(this);

        loadingLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        loadingLayout.setGravity(Gravity.CENTER);
        loadingLayout.setOrientation(LinearLayout.VERTICAL);

        loadingDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        TextView txtView = new TextView(this);
        txtView.setText("Loading data...");
        txtView.setTextSize(20);
        txtView.setGravity(Gravity.CENTER);
        txtView.setTextColor(this.getResources().getColor(R.color.cardview_light_background));

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);

        loadingLayout.setBackgroundColor(this.getResources().getColor(R.color.colorPrimaryDark));
        loadingLayout.addView(txtView);
        loadingLayout.addView(progressBar);

        loadingDialog.setContentView(loadingLayout);
        loadingDialog.show();
    }

    private class UiHeavyLoadCalculator extends AsyncTask<Void, Integer, Void>
    {

        private float totalValue;
        private float totalFluctuation;
        private float totalFluctuationPercentage;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            totalValue = 0;
            totalFluctuation = 0;
            totalFluctuationPercentage = 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            final List<View> cardList = new ArrayList<>();

            Looper.prepare();

            balanceManager.sortCoins();

            for(int i = 0; i < balanceManager.getTotalBalance().size(); i++)
            {
                final Currency localCurrency = balanceManager.getTotalBalance().get(i);

                if(localCurrency.getIcon() != null)
                {
                    Palette.Builder builder = Palette.from(localCurrency.getIcon());

                    localCurrency.setChartColor(builder.generate().getDominantColor(0));
                }
                else
                {
                    localCurrency.setChartColor(12369084);
                }

                if(!localCurrency.getSymbol().equals("USD") && (localCurrency.getBalance() * localCurrency.getValue()) > 0.001)
                {
                    localCurrency.setName(balanceManager.getCurrencyName(localCurrency.getSymbol()));
                    localCurrency.setId(balanceManager.getCurrencyId(localCurrency.getSymbol()));
                    totalValue += localCurrency.getValue() * localCurrency.getBalance();
                    totalFluctuation += (localCurrency.getValue() * localCurrency.getBalance()) * (localCurrency.getDayFluctuationPercentage() / 100);

                    cardList.add(layoutGenerator.getInfoLayout(localCurrency, true));
                }

                if(!localCurrency.getSymbol().equals("USD") && localCurrency.getHistoryMinutes() == null)
                {
                    cardList.add(layoutGenerator.getInfoLayout(localCurrency, true));
                }

                balanceManager.getTotalBalance().set(i, localCurrency);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(false);
                    currencyLayout.removeAllViews();

                    for(int i = 0; i < cardList.size(); i++)
                    {
                        currencyLayout.addView(cardList.get(i));
                    }

                    adaptView();
                }
            });

            toolbarLayout.setTitle("US$" + String.format("%.2f", totalValue));

            if(totalFluctuation > 0)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toolbarSubtitle.setTextColor(getResources().getColor(R.color.increase));
                    }
                });
            }
            else
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toolbarSubtitle.setTextColor(getResources().getColor(R.color.decrease));
                    }
                });
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    totalFluctuationPercentage = totalFluctuation / (totalValue - totalFluctuation) *100;

                    toolbarSubtitle.setText("US$" + String.format("%.2f", totalFluctuation) + " (" + String.format("%.2f", totalFluctuationPercentage) + "%)");

                    if(loadingDialog.isShowing())
                    {
                        loadingDialog.dismiss();
                    }
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            handler.removeCallbacks(updateRunnable);
        }
    }

    private class DataUpdater extends AsyncTask<Void, Integer, Void>
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
                            balance.get(i).updateHistoryMinutes(getApplicationContext(), new Currency.CurrencyCallBack() {
                                @Override
                                public void onSuccess(Currency currency) {
                                    countCoins(true, false);
                                    /*currency.updateName(getApplicationContext(), new Currency.CurrencyCallBack() {
                                        @Override
                                        public void onSuccess(Currency currency) {
                                            countCoins(true, false);
                                        }
                                    });*/
                                }
                            });
                        }
                    }
                    else
                    {
                        countCoins(false, false);
                    }
                }

                public void onError(String error)
                {
                    switch (error)
                    {
                        case "com.android.volley.AuthFailureError":
                            preferencesManager.disableHitBTC();
                            Snackbar.make(findViewById(R.id.currencyListLayout), "HitBTC synchronization error : Invalid keys", Snackbar.LENGTH_LONG)
                                    .show();
                            refreshLayout.setRefreshing(false);
                            updateAll(true);
                            break;
                        default:
                            updateAll(true);
                    }
                    //updateAll();
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {

        }
    }

    public interface IconCallBack
    {
        void onSuccess(Bitmap bitmap);
    }
}
