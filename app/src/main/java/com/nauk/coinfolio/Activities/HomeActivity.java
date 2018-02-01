package com.nauk.coinfolio.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.db.chart.view.LineChartView;
import com.nauk.coinfolio.DataManagers.BalanceManager;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.DataManagers.DatabaseManager;
import com.nauk.coinfolio.LayoutManagers.HomeLayoutGenerator;
import com.nauk.coinfolio.DataManagers.PreferencesManager;
import com.nauk.coinfolio.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

//Use WilliamChart for charts https://github.com/diogobernardino/WilliamChart

//Auto refresh with predefined intervals
//Adding manually currencies (date, purchased price)
//Multiple portfolio (exchanges & custom)
//Add currency details (market cap, 1h, 3h, 3d, 1w, 1m, 3m, 1y)
//Add roadmap to buy a coin
//Add reddit link ?
//

public class HomeActivity extends AppCompatActivity {

    private BalanceManager balanceManager;
    private int coinCounter;
    private int iconCounter;
    private HomeLayoutGenerator layoutGenerator;
    private LinearLayout currencyLayout;
    private Toolbar toolbar;
    private CollapsingToolbarLayout toolbarLayout;
    private SwipeRefreshLayout refreshLayout;
    private TextView toolbarSubtitle;
    private boolean view;
    private Dialog loadingDialog;
    private boolean iconChecker;
    private PreferencesManager preferencesManager;
    private DatabaseManager databaseManager;
    private long lastTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_currency_summary);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferencesManager = new PreferencesManager(this);

        view = preferencesManager.getDetailOption();

        generateSplash();

        ImageButton detailsButton = findViewById(R.id.switch_button);
        ImageButton settingsButton = findViewById(R.id.settings_button);

        refreshLayout = findViewById(R.id.swiperefresh);

        toolbarLayout = findViewById(R.id.toolbar_layout);

        toolbarSubtitle = findViewById(R.id.toolbarSubtitle);

        toolbarLayout.setExpandedTitleGravity(Gravity.CENTER);
        toolbarLayout.setCollapsedTitleGravity(Gravity.CENTER);
        toolbarLayout.setForegroundGravity(Gravity.CENTER);
        toolbarLayout.setTitle("US$0.00");

        toolbarSubtitle.setText("US$0.00");

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

        layoutGenerator = new HomeLayoutGenerator(this);

        balanceManager = new BalanceManager(this);

        currencyLayout = findViewById(R.id.currencyListLayout);

        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateAll();
                    }
                }
        );

        final ImageButton addCurrencyButton = findViewById(R.id.addCurrencyButton);

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


                /*Snackbar.make(findViewById(R.id.currencyListLayout), "This feature is not yet available...", Snackbar.LENGTH_LONG)
                        .show();*/
            }
        });

        databaseManager = new DatabaseManager(this);

        updateViewButtonIcon();

        lastTimestamp = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(System.currentTimeMillis()/1000 - lastTimestamp > 60)
        {
            lastTimestamp = System.currentTimeMillis()/1000;

            updateAll();
        }
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
        if(!view)
        {
            view = true;

            adaptView();
        }
        else
        {
            view = false;

            adaptView();
        }
    }

    private void adaptView()
    {
        if(!view)
        {
            for(int i = 0; i < currencyLayout.getChildCount(); i++)
            {
                currencyLayout.getChildAt(i).findViewWithTag("chart_layout").setVisibility(View.GONE);
                currencyLayout.getChildAt(i).findViewWithTag("separator_layout").setVisibility(View.GONE);
            }
        }
        else
        {
            currencyLayout.removeAllViews();

            //layoutGenerator.setCurrencyList(balanceManager.getTotalBalance());
            //layoutGenerator.resetCurrencyList();

            for(int i = 0; i < balanceManager.getTotalBalance().size(); i++)
            {
                final Currency currency = balanceManager.getTotalBalance().get(i);

                if(!currency.getSymbol().equals("USD") && ((currency.getBalance() * currency.getValue()) > 0.001 || currency.getDayPriceHistory() == null))
                {
                    currencyLayout.addView(layoutGenerator.getInfoLayout(currency, currency.getChartColor()));
                }
            }
        }

        updateViewButtonIcon();
    }



    private void updateAll()
    {
        resetCounter();
        balanceManager.updateExchangeKeys();
        DataUpdater updater = new DataUpdater();
        updater.execute();
        refreshLayout.setRefreshing(true);
    }

    private void resetCounter()
    {
        coinCounter = 0;
        iconCounter = 0;
        iconChecker = false;
    }

    private void getBitmapFromURL(String src, IconCallBack callBack) {
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
            result = null;
        }

        callBack.onSuccess(result);
    }

    private void countIcons()
    {
        float totalValue = 0;
        float totalFluctuation = 0;

        iconCounter++;

        if(iconCounter == balanceManager.getTotalBalance().size())
        {

            if(balanceManager.getTotalBalance() != null)
            {
                if(coinCounter == balanceManager.getTotalBalance().size()-1 && iconChecker)
                {
                    refreshLayout.setRefreshing(false);

                    balanceManager.sortCoins();

                    currencyLayout.removeAllViews();

                    //layoutGenerator.setCurrencyList(balanceManager.getTotalBalance());

                    for(int i = 0; i < balanceManager.getTotalBalance().size(); i++)
                    {
                        if(balanceManager.getTotalBalance().get(i).getIcon() != null)
                        {
                            //balanceManager.getTotalBalance().get(i).setIcon(getBitmapFromURL(balanceManager.getIconUrl(balanceManager.getTotalBalance().get(i).getSymbol())));

                            Palette.Builder builder = Palette.from(balanceManager.getTotalBalance().get(i).getIcon());

                            balanceManager.getTotalBalance().get(i).setChartColor(builder.generate().getDominantColor(0));

                            //layoutGenerator.addCurrencyToList(currency);
                            //currencyLayout.addView(layoutGenerator.getInfoLayout(i));
                        }
                        else
                        {
                            //currency.setChartColor(12369084);
                            balanceManager.getTotalBalance().get(i).setChartColor(12369084);
                            //currencyLayout.addView(layoutGenerator.getInfoLayout(i));
                        }

                        if(!balanceManager.getTotalBalance().get(i).getSymbol().equals("USD") && (balanceManager.getTotalBalance().get(i).getBalance() * balanceManager.getTotalBalance().get(i).getValue()) > 0.001)
                        {
                            balanceManager.getTotalBalance().get(i).setName(balanceManager.getCurrencyName(balanceManager.getTotalBalance().get(i).getSymbol()));
                            totalValue += balanceManager.getTotalBalance().get(i).getValue() * balanceManager.getTotalBalance().get(i).getBalance();
                            totalFluctuation += (balanceManager.getTotalBalance().get(i).getValue() * balanceManager.getTotalBalance().get(i).getBalance()) * (balanceManager.getTotalBalance().get(i).getDayFluctuationPercentage() / 100);
                            //balanceManager.getTotalBalance().get(i).setIcon(getBitmapFromURL(balanceManager.getIconUrl(balanceManager.getTotalBalance().get(i).getSymbol())));
                            //currencyLayout.addView(layoutGenerator.getInfoLayout(i));
                            currencyLayout.addView(layoutGenerator.getInfoLayout(balanceManager.getTotalBalance().get(i), 0));
                        }

                        if(!balanceManager.getTotalBalance().get(i).getSymbol().equals("USD") && balanceManager.getTotalBalance().get(i).getDayPriceHistory() == null)
                        {
                            //balanceManager.getTotalBalance().get(i).setIcon(getBitmapFromURL(balanceManager.getIconUrl(balanceManager.getTotalBalance().get(i).getSymbol())));
                            //currencyLayout.addView(layoutGenerator.getInfoLayout(i));
                            currencyLayout.addView(layoutGenerator.getInfoLayout(balanceManager.getTotalBalance().get(i), 0));
                        }
                    }

                    adaptView();

                    toolbarLayout.setTitle("US$" + String.format("%.2f", totalValue));

                    if(totalFluctuation > 0)
                    {
                        toolbarSubtitle.setTextColor(getResources().getColor(R.color.increase));
                    }
                    else
                    {
                        toolbarSubtitle.setTextColor(getResources().getColor(R.color.decrease));
                    }

                    toolbarSubtitle.setText("US$" + String.format("%.2f", totalFluctuation));


                    if(loadingDialog.isShowing())
                    {
                        loadingDialog.dismiss();
                    }
                }

                if(balanceManager.getTotalBalance().size() == 0)
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
            iconChecker = true;
        }

        if(coinCounter == balanceManager.getTotalBalance().size()-1)
        {
            for (int i = 0; i < balanceManager.getTotalBalance().size(); i++)
            {
                final int index = i;

                getBitmapFromURL(balanceManager.getIconUrl(balanceManager.getTotalBalance().get(i).getSymbol()), new IconCallBack() {
                    @Override
                    public void onSuccess(Bitmap bitmapIcon) {
                        balanceManager.getTotalBalance().get(index).setIcon(bitmapIcon);
                        countIcons();
                    }
                });
            }
        }
    }

    private void updateViewButtonIcon()
    {
        ImageButton imgButton = findViewById(R.id.switch_button);

        if(view)
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
            balanceManager.updateTotalBalance(new BalanceManager.VolleyCallBack() {
                @Override
                public void onSuccess() {

                    final List<Currency> balance = balanceManager.getTotalBalance();

                    if(balanceManager.getTotalBalance().size() > 0)
                    {
                        for(int i = 0; i < balanceManager.getTotalBalance().size(); i++)
                        {
                            balance.get(i).updateDayPriceHistory(getApplicationContext(), new Currency.CurrencyCallBack() {
                                @Override
                                public void onSuccess(Currency currency) {
                                    currency.updateName(getApplicationContext(), new Currency.CurrencyCallBack() {
                                        @Override
                                        public void onSuccess(Currency currency) {
                                            countCoins(true, false);
                                        }
                                    });
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
                            updateAll();
                            break;
                        default:
                            updateAll();
                    }
                    //updateAll();
                }
            });

            balanceManager.updateDetails(new BalanceManager.IconCallBack() {
                @Override
                public void onSuccess()
                {
                    countCoins(false, true);
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
