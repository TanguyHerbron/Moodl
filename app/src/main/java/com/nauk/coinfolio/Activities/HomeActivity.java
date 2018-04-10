package com.nauk.coinfolio.Activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.nauk.coinfolio.DataManagers.BalanceManager;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.DataManagers.MarketCapManager;
import com.nauk.coinfolio.DataManagers.PreferencesManager;
import com.nauk.coinfolio.LayoutManagers.HomeLayoutGenerator;
import com.nauk.coinfolio.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
    private BalanceManager balanceManager;
    private MarketCapManager marketCapManager;

    private int coinCounter;
    private int iconCounter;
    private int marketCapCounter;
    private long lastTimestamp;
    private boolean detailsChecker;
    private boolean isDetailed;
    protected float totalValue;
    protected float totalFluctuation;

    private CollapsingToolbarLayout toolbarLayout;
    private SwipeRefreshLayout refreshLayout;
    private LinearLayout currencyLayout;
    private TextView toolbarSubtitle;
    private Dialog loadingDialog;
    private Handler handler;
    private Runnable updateRunnable;
    private ViewFlipper viewFlipper;
    private HomeLayoutGenerator layoutGenerator;
    private BottomNavigationView bottomNavigationView;

    private HashMap<String, Integer> dominantCurrenciesColors;

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            item.setChecked(true);
            switch (item.getItemId())
            {
                case R.id.navigation_watchlist:
                    switchSecondaryViews(0);
                    break;
                case R.id.navigation_currencies_list:
                    switchMainView();
                    break;
                case R.id.navigation_market_cap:
                    switchSecondaryViews(2);
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**Interface setup**/

        //Setup main interface
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_currency_summary);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        generateSplashScreen();

        //Objects initialization
        preferencesManager = new PreferencesManager(this);
        balanceManager = new BalanceManager(this);
        marketCapManager = new MarketCapManager(this);
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

        bottomNavigationView = findViewById(R.id.navigationSummary);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        bottomNavigationView.setSelectedItemId(R.id.navigation_currencies_list);

        layoutGenerator = new HomeLayoutGenerator(this);

        Button addCurrencyButton = findViewById(R.id.buttonAddTransaction);
        ImageButton detailsButton = findViewById(R.id.switch_button);
        ImageButton settingsButton = findViewById(R.id.settings_button);

        toolbarLayout.setForegroundGravity(Gravity.CENTER);

        totalValue = 0;
        totalFluctuation = 0;

        updateTitle();

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
                //overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
        });

        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        switch (viewFlipper.getDisplayedChild())
                        {
                            case 0:
                                Log.d(getResources().getString(R.string.debug), "Watchlist");
                                refreshLayout.setRefreshing(false);
                                break;
                            case 1:
                                updateAll(false);
                                break;
                            case 2:
                                Log.d(getResources().getString(R.string.debug), "Market cap");
                                refreshLayout.setRefreshing(false);
                                break;
                        }

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

        setupDominantCurrenciesColors();
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

    private void setupDominantCurrenciesColors()
    {
        dominantCurrenciesColors = new HashMap<>();

        dominantCurrenciesColors.put("BTC", -489456);
        dominantCurrenciesColors.put("ETH", -13619152);
        dominantCurrenciesColors.put("XRP", -16744256);
        dominantCurrenciesColors.put("BCH", -1011696);
        dominantCurrenciesColors.put("LTC", -4671304);
        dominantCurrenciesColors.put("ADA", -16773080);
        dominantCurrenciesColors.put("NEO", -9390048);
        dominantCurrenciesColors.put("XLM", -11509656);
        dominantCurrenciesColors.put("XMR", -499712);
        dominantCurrenciesColors.put("EOS", -1513240);
        dominantCurrenciesColors.put("IOT", -1513240);
        dominantCurrenciesColors.put("DASH", -15175496);
        dominantCurrenciesColors.put("XEM", -7829368);
        dominantCurrenciesColors.put("TRX", -7829360);
        dominantCurrenciesColors.put("ETC", -10448784);
    }

    private void switchMainView()
    {
        findViewById(R.id.toolbar_layout).setFocusable(true);
        ((AppBarLayout) findViewById(R.id.app_bar)).setExpanded(true, true);
        findViewById(R.id.nestedScrollViewLayout).setNestedScrollingEnabled(true);

        findViewById(R.id.app_bar).setEnabled(true);
        findViewById(R.id.toolbar_layout).setNestedScrollingEnabled(true);
        findViewById(R.id.coordinatorLayout).setNestedScrollingEnabled(true);

        findViewById(R.id.switch_button).setVisibility(View.VISIBLE);

        viewFlipper.setDisplayedChild(1);
    }

    private void switchSecondaryViews(int itemIndex)
    {
        findViewById(R.id.toolbar_layout).setFocusable(false);
        ((AppBarLayout) findViewById(R.id.app_bar)).setExpanded(false, true);
        findViewById(R.id.nestedScrollViewLayout).setNestedScrollingEnabled(false);

        findViewById(R.id.app_bar).setEnabled(false);
        findViewById(R.id.toolbar_layout).setNestedScrollingEnabled(false);
        findViewById(R.id.coordinatorLayout).setNestedScrollingEnabled(false);

        findViewById(R.id.switch_button).setVisibility(View.GONE);

        viewFlipper.setDisplayedChild(itemIndex);
    }

    private void showErrorSnackbar()
    {
        Snackbar.make(findViewById(R.id.viewFlipperSummary), "Error while updating data", Snackbar.LENGTH_LONG)
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

        //addTestWatchlistCardview();
        displayBalance(preferencesManager.isBalanceHidden());

        updateAll(preferencesManager.mustUpdate());
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

    private void addTestWatchlistCardview()
    {
        View view = LayoutInflater.from(this).inflate(R.layout.cardview_watchlist, null);

        ((TextView) view.findViewById(R.id.currencyFluctuationPercentageTextView)).setText("3%");
        ((TextView) view.findViewById(R.id.currencyFluctuationTextView)).setText("$3");
        ((TextView) view.findViewById(R.id.currencyNameTextView)).setText("TanguyCoin");
        ((TextView) view.findViewById(R.id.currencySymbolTextView)).setText("TGC");
        ((TextView) view.findViewById(R.id.currencyValueTextView)).setText("$100");

        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("coinfolio", "Clicked !");
            }
        });

        ((LinearLayout) findViewById(R.id.linearLayoutWatchlist)).addView(view);
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
                currencyLayout.addView(layoutGenerator.getInfoLayout(currency, isDetailed, totalValue, preferencesManager.isBalanceHidden()));
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

        totalValue = 0;
        totalFluctuation = 0;
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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                updateTitle();
            }
        });
    }

    private void updateMarketCap()
    {
        marketCapCounter = 0;

        marketCapManager.updateTopCurrencies(new MarketCapManager.VolleyCallBack() {
            @Override
            public void onSuccess()
            {
                countCompletedMarketCapRequest();
            }
        });

        marketCapManager.updateMarketCap(new MarketCapManager.VolleyCallBack() {
            @Override
            public void onSuccess() {
                countCompletedMarketCapRequest();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void countCompletedMarketCapRequest()
    {
        marketCapCounter++;

        if(marketCapCounter == 2)
        {
            setupTextViewMarketCap();

            findViewById(R.id.progressBarMarketCap).setVisibility(View.GONE);
            findViewById(R.id.layoutProgressMarketCap).setVisibility(View.VISIBLE);

            List<PieEntry> entries = new ArrayList<>();

            ArrayList<Integer> colors = new ArrayList<>();

            float otherCurrenciesDominance = 0;

            for(Iterator i = marketCapManager.getDominance().keySet().iterator(); i.hasNext(); )
            {
                String key = (String) i.next();
                entries.add(new PieEntry(marketCapManager.getDominance().get(key), key));
                otherCurrenciesDominance += marketCapManager.getDominance().get(key);
                colors.add(dominantCurrenciesColors.get(key));
            }
            entries.add(new PieEntry(100-otherCurrenciesDominance, "Others"));
            colors.add(-12369084);

            PieDataSet set = new PieDataSet(entries, "Market Cap Dominance");
            set.setColors(colors);
            set.setSliceSpace(1);
            set.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            set.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            PieData data = new PieData(set);
            data.setValueTextSize(10);
            data.setValueFormatter(new PercentFormatter());

            setupPieChart(data);
        }
    }

    private void setupPieChart(PieData data)
    {
        PieChart pieChart = findViewById(R.id.marketCapPieChart);

        pieChart.setData(data);
        pieChart.setDrawSlicesUnderHole(false);
        pieChart.setUsePercentValues(true);
        pieChart.setTouchEnabled(true);

        pieChart.setEntryLabelColor(Color.parseColor("#FF000000"));

        pieChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        refreshLayout.setEnabled(false);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    default:
                        refreshLayout.setEnabled(true);
                        break;
                }
                return false;
            }
        });

        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setCenterText(generateCenterSpannableText());
        pieChart.invalidate(); // refresh

    }

    private void setupTextViewMarketCap()
    {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.UK);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();

        symbols.setGroupingSeparator(' ');
        formatter.setDecimalFormatSymbols(symbols);

        ((TextView) findViewById(R.id.marketCapTextView)).setText(getResources().getString(R.string.market_cap_textview, formatter.format(marketCapManager.getMarketCap())));

        ((TextView) findViewById(R.id.dayVolumeTotalMarketCap)).setText(getResources().getString(R.string.volume_market_cap_textview, formatter.format(marketCapManager.getDayVolume())));
    }

    private SpannableString generateCenterSpannableText() {

        SpannableString spannableString = new SpannableString("Market Capitalization Dominance");
        return spannableString;
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

    private void generateSplashScreen()
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
                    getBitmapFromURL(balanceManager.getIconUrl(localCurrency.getSymbol()), new IconCallBack() {
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
            runOnUiThread(new Runnable() {
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

            runOnUiThread(new Runnable() {
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
            HomeActivity.this.totalValue = totalValue;
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
                                }
                            });
                        }
                    }
                    else
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                countCoins(false, false);
                            }
                        });
                    }
                }

                public void onError(String error)
                {
                    switch (error)
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
                    }
                }
            });

            updateMarketCap();

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
