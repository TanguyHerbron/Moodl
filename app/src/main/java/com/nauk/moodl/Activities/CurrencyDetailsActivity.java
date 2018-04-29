package com.nauk.moodl.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.nauk.moodl.DataManagers.CurrencyData.Currency;
import com.nauk.moodl.DataManagers.CurrencyData.CurrencyDataChart;
import com.nauk.moodl.DataManagers.CurrencyData.Trade;
import com.nauk.moodl.DataManagers.CurrencyData.Transaction;
import com.nauk.moodl.DataManagers.DatabaseManager;
import com.nauk.moodl.DataManagers.ExchangeManager.BinanceManager;
import com.nauk.moodl.DataManagers.PreferencesManager;
import com.nauk.moodl.LayoutManagers.TradeListAdapter;
import com.nauk.moodl.LayoutManagers.TransactionListAdapter;
import com.nauk.moodl.PlaceholderManager;
import com.nauk.moodl.R;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.abs;

/**Create a Parcelable**/

public class CurrencyDetailsActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private ListView transactionLayout;
    private ListView tradeLayout;
    private DatabaseManager databaseManager;
    //private String symbol;
    private Currency currency;
    private boolean hasBeenModified;
    private final static int HOUR = 0;
    private final static int DAY = 1;
    private final static int WEEK = 2;
    private final static int MONTH = 3;
    private final static int YEAR = 4;
    private List<CurrencyDataChart> dataChartList;
    private LineChart lineChart;
    private CandleStickChart candleStickChart;
    private BarChart barChart;
    private PreferencesManager preferencesManager;
    private BinanceManager binanceManager;
    private TradeListAdapter tradeListAdapter;
    private TransactionListAdapter transactionListAdapter;
    private boolean flag_loading;

    private boolean isSnapshotUpdated;
    private boolean isTickerUpdated;

    private boolean displayLineChart;

    private Button lineChartButton;
    private Button candleStickChartButton;

    private ArrayList<com.nauk.moodl.DataManagers.CurrencyData.Trade> returnedTrades;

    private View loadingFooter;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewFlipper.setDisplayedChild(0);
                    return true;
                case R.id.navigation_dashboard:
                    viewFlipper.setDisplayedChild(1);
                    return true;
                case R.id.navigation_notifications:
                    viewFlipper.setDisplayedChild(2);
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("update", hasBeenModified);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_details);

        Intent intent = getIntent();

        currency = intent.getParcelableExtra("currency");

        databaseManager = new DatabaseManager(this);
        preferencesManager = new PreferencesManager(this);

        isSnapshotUpdated = false;
        isTickerUpdated = false;

        displayLineChart = true;
        flag_loading = false;

        viewFlipper = findViewById(R.id.vfCurrencyDetails);
        transactionLayout = findViewById(R.id.listTransactions);
        tradeLayout = findViewById(R.id.listTrades);
        lineChart = findViewById(R.id.chartPriceView);
        candleStickChart = findViewById(R.id.chartCandleStickView);
        barChart = findViewById(R.id.chartVolumeView);
        lineChartButton = findViewById(R.id.lineChartButton);
        candleStickChartButton = findViewById(R.id.candleStickChartButton);
        binanceManager = new BinanceManager(preferencesManager.getBinancePublicKey(), preferencesManager.getBinancePrivateKey());

        lineChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lineChartButton.setEnabled(false);
                candleStickChartButton.setEnabled(true);

                lineChart.setVisibility(View.VISIBLE);
                candleStickChart.setVisibility(View.GONE);

                displayLineChart = true;
            }
        });

        candleStickChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lineChartButton.setEnabled(true);
                candleStickChartButton.setEnabled(false);

                lineChart.setVisibility(View.GONE);
                candleStickChart.setVisibility(View.VISIBLE);

                displayLineChart = false;
            }
        });

        Drawable progressBarDrawable = ((ProgressBar) findViewById(R.id.progressBarChart)).getIndeterminateDrawable();
        progressBarDrawable.mutate();
        progressBarDrawable.setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));
        progressBarDrawable.invalidateSelf();

        setupActionBar();

        initializeButtons();
        initializeLineChart(lineChart);
        initializeCandleStickChart(candleStickChart);

        TransactionUpdater transactionUpdater = new TransactionUpdater();
        transactionUpdater.execute();

        updateInfoTab();

        updateChartTab(DAY, 1);

        BottomNavigationView navigation = findViewById(R.id.navigation_details);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        hasBeenModified = false;

        Drawable tradeDrawable = ((ProgressBar) findViewById(R.id.loadingIndicator)).getIndeterminateDrawable();
        tradeDrawable.mutate();
        tradeDrawable.setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));
        tradeDrawable.invalidateSelf();

        TradeUpdater updater = new TradeUpdater();
        updater.execute();
    }

    private void refreshInfoTab()
    {
        Drawable progressBarDrawable = ((ProgressBar) findViewById(R.id.percentageCoinEmited)).getProgressDrawable();
        progressBarDrawable.mutate();
        progressBarDrawable.setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));
        progressBarDrawable.invalidateSelf();

        ((ProgressBar) findViewById(R.id.percentageCoinEmited))
                .setProgress((int) Math.round(currency.getMinedCoinSupply() / currency.getMaxCoinSupply() * 100));

        if(currency.getAlgorithm() != null && !currency.getAlgorithm().equals(""))
        {
            ((TextView) findViewById(R.id.txtViewAlgorithm))
                    .setText(currency.getAlgorithm());
        }

        if(currency.getProofType() != null && !currency.getProofType().equals(""))
        {
            ((TextView) findViewById(R.id.txtViewProofType))
                    .setText(currency.getProofType());
        }

        if(currency.getStartDate() != null && !currency.getStartDate().equals(""))
        {
            ((TextView) findViewById(R.id.txtViewStartDate))
                    .setText(currency.getStartDate());
        }

        ((TextView) findViewById(R.id.txtViewDescription))
                .setText(Html.fromHtml(currency.getDescription()));
        ((TextView) findViewById(R.id.txtViewDescription))
                .setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.txtViewPercentageCoinEmited))
                .setText("Percentage of coin emited : " + numberConformer(currency.getMinedCoinSupply() / currency.getMaxCoinSupply() * 100) + "%");
        if(currency.getMarketCapitalization() != 0)
        {
            ((TextView) findViewById(R.id.txtViewMarketCapitalization))
                    .setText(PlaceholderManager.getValueString(numberConformer(currency.getMarketCapitalization()), this));
        }

        if(currency.getRank() != 0)
        {
            ((TextView) findViewById(R.id.txtViewRank))
                    .setText(String.valueOf(currency.getRank()));
        }

        if(currency.getMaxCoinSupply() == 0)
        {
            ((TextView) findViewById(R.id.txtViewTotalSupply))
                    .setText(PlaceholderManager.getSymbolString("Infinity", getApplication()));
        }
        else
        {
            ((TextView) findViewById(R.id.txtViewTotalSupply))
                    .setText(PlaceholderManager.getSymbolString(numberConformer(currency.getMaxCoinSupply()), getApplication()));
        }
        ((TextView) findViewById(R.id.txtViewCirculatingSupply))
                .setText(PlaceholderManager.getSymbolString(numberConformer(currency.getMinedCoinSupply()), getApplication()));
    }

    private void updateInfoTab()
    {
        currency.updateSnapshot(this, new Currency.CurrencyCallBack() {
            @Override
            public void onSuccess(final Currency currency) {
                isSnapshotUpdated = true;
                dataCounter();
            }
        });

        currency.updateTicker(this, preferencesManager.getDefaultCurrency(), new Currency.CurrencyCallBack() {
            @Override
            public void onSuccess(Currency currency) {
                isTickerUpdated = true;
                dataCounter();
            }
        });
    }

    private void dataCounter()
    {
        if(isTickerUpdated && isSnapshotUpdated)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshInfoTab();
                }
            });
        }
    }

    private void setupActionBar()
    {
        if(currency.getBalance() == 0)
        {
            setTitle(" " + currency.getName());
        }
        else
        {
            setTitle(" " + currency.getName() + " | " + numberConformer(currency.getBalance()));
        }

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO);

        Bitmap result = Bitmap.createBitmap(150, 150, currency.getIcon().getConfig());

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(this, R.color.white));

        Canvas canvas = new Canvas(result);
        canvas.drawCircle(result.getHeight()/2, result.getWidth()/2, 75, paint);
        canvas.drawBitmap(Bitmap.createScaledBitmap(currency.getIcon(), 100, 100, false), result.getHeight()/2 - 50, result.getWidth()/2 - 50, null);

        getSupportActionBar().setIcon(new BitmapDrawable(Bitmap.createScaledBitmap(result, 120, 120, false)));

    }

    private void initializeButtons()
    {
        LinearLayout buttonLayout = findViewById(R.id.layoutChartButtons);

        for(int i = 0; i < buttonLayout.getChildCount(); i++)
        {
            final Button button = (Button) buttonLayout.getChildAt(i);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonEvent(v);
                }
            });
        }
    }

    private void buttonEvent(View v)
    {
        v.setEnabled(false);
        v.setElevation(convertDpToPx(8));

        LinearLayout buttonLayout = (LinearLayout) v.getParent();

        for(int i = 0; i < buttonLayout.getChildCount(); i++)
        {
            Button button = (Button) buttonLayout.getChildAt(i);

            if(button != v)
            {
                button.setEnabled(true);
                button.setElevation(convertDpToPx(2));
            }
        }

        updateCharts((Button) v);
    }

    private float convertDpToPx(float dp)
    {
        return dp * this.getResources().getDisplayMetrics().density;
    }

    private void updateCharts(Button button)
    {
        findViewById(R.id.chartPriceView).setVisibility(View.GONE);
        findViewById(R.id.chartCandleStickView).setVisibility(View.GONE);
        findViewById(R.id.chartVolumeView).setVisibility(View.GONE);
        findViewById(R.id.progressLayoutChart).setVisibility(View.VISIBLE);

        String interval = button.getText().toString().substring(button.getText().toString().length()-2);

        switch (interval)
        {
            case "1h":
                updateChartTab(HOUR, 1);
                break;
            case "3h":
                updateChartTab(HOUR, 3);
                break;
            case "1d":
                updateChartTab(DAY, 1);
                break;
            case "3d":
                currency.updateHistoryHours(this, preferencesManager.getDefaultCurrency(), new Currency.CurrencyCallBack() {
                    @Override
                    public void onSuccess(Currency currency) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateChartTab(CurrencyDetailsActivity.DAY, 3);
                            }
                        });
                    }
                });
                break;
            case "1w":
                currency.updateHistoryHours(this, preferencesManager.getDefaultCurrency(), new Currency.CurrencyCallBack() {
                    @Override
                    public void onSuccess(Currency currency) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateChartTab(CurrencyDetailsActivity.WEEK, 11);
                            }
                        });
                    }
                });
                break;
            case "1M":
                currency.updateHistoryHours(this, preferencesManager.getDefaultCurrency(), new Currency.CurrencyCallBack() {
                    @Override
                    public void onSuccess(Currency currency) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateChartTab(CurrencyDetailsActivity.MONTH, 1);
                            }
                        });
                    }
                });
                break;
            case "3M":
                currency.updateHistoryDays(this, preferencesManager.getDefaultCurrency(), new Currency.CurrencyCallBack() {
                    @Override
                    public void onSuccess(Currency currency) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateChartTab(CurrencyDetailsActivity.MONTH, 3);
                            }
                        });
                    }
                });
                break;
            case "6M":
                currency.updateHistoryDays(this, preferencesManager.getDefaultCurrency(), new Currency.CurrencyCallBack() {
                    @Override
                    public void onSuccess(Currency currency) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateChartTab(CurrencyDetailsActivity.MONTH, 6);
                            }
                        });
                    }
                });
                break;
            case "1y":
                currency.updateHistoryDays(this, preferencesManager.getDefaultCurrency(), new Currency.CurrencyCallBack() {
                    @Override
                    public void onSuccess(Currency currency) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateChartTab(CurrencyDetailsActivity.YEAR, 1);
                            }
                        });
                    }
                });
                break;
        }
    }

    private void updateChartTab(int timeUnit, int amount)
    {
        updateChartsData(timeUnit, amount);
        drawPriceLineChart();
        drawPriceCandleStickChart();

        if(displayLineChart)
        {
            findViewById(R.id.chartPriceView).setVisibility(View.VISIBLE);
            findViewById(R.id.progressLayoutChart).setVisibility(View.GONE);
        }
        else
        {
            findViewById(R.id.chartCandleStickView).setVisibility(View.VISIBLE);
            findViewById(R.id.progressLayoutChart).setVisibility(View.GONE);
        }

        drawVolumeChart();
        updateGeneralData(lineChart.getData().getDataSets().get(0).getEntryForIndex(0).getY(), lineChart.getData().getDataSets().get(0).getEntryForIndex(lineChart.getData().getDataSets().get(0).getEntryCount() - 1).getY());
    }

    private void updateChartsData(int timeUnit, int amount)
    {
        dataChartList = new ArrayList<>();

        switch (timeUnit)
        {
            case HOUR:
                dataChartList = currency.getHistoryMinutes().subList(currency.getHistoryMinutes().size()-(60*amount), currency.getHistoryMinutes().size());
                break;
            case DAY:
                if(amount == 1)
                {
                    dataChartList = currency.getHistoryMinutes();
                }
                else
                {
                    dataChartList = currency.getHistoryHours().subList(currency.getHistoryHours().size()-(24*amount), currency.getHistoryHours().size());
                }
                break;
            case WEEK:
                dataChartList = currency.getHistoryHours().subList(currency.getHistoryHours().size()-168, currency.getHistoryHours().size());
                break;
            case MONTH:
                switch (amount)
                {
                    case 1:
                        dataChartList = currency.getHistoryHours();
                        break;
                    case 3:
                        dataChartList = currency.getHistoryDays().subList(currency.getHistoryDays().size()-93, currency.getHistoryDays().size());
                        break;
                    case 6:
                        dataChartList = currency.getHistoryDays().subList(currency.getHistoryDays().size()-186, currency.getHistoryDays().size());
                        break;
                }
                break;
            case YEAR:
                dataChartList = currency.getHistoryDays();
                break;
        }
    }

    private void drawVolumeChart()
    {
        initializeBarChart(barChart);

        barChart.setData(generateVolumeChartSet());
        barChart.invalidate();

        findViewById(R.id.chartVolumeView).setVisibility(View.VISIBLE);
    }

    private void initializeBarChart(BarChart barChart)
    {
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.setDrawMarkers(true);
        barChart.setDoubleTapToZoomEnabled(true);
        barChart.setPinchZoom(true);
        barChart.setScaleEnabled(false);
        barChart.setDragEnabled(true);
        barChart.getDescription().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getXAxis().setEnabled(false);
        barChart.setViewPortOffsets(0, 0, 0, 0);
        barChart.setFitBars(true);
    }

    private void drawPriceCandleStickChart()
    {
        candleStickChart.setData(generatePriceCandleStickChartSet());
    }

    private void drawPriceLineChart()
    {
        lineChart.setData(generatePriceLineChartSet());
        lineChart.getAxisLeft().setAxisMinValue(lineChart.getData().getYMin());

        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                valueSelectedEvent(e);
            }

            @Override
            public void onNothingSelected() {

            }
        });

        lineChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return toucheEvent(motionEvent);
            }
        });
    }

    private void initializeCandleStickChart(CandleStickChart candleStickChart)
    {
        candleStickChart.setDrawGridBackground(false);
        candleStickChart.setDrawBorders(false);
        candleStickChart.setDrawMarkers(true);
        candleStickChart.getDescription().setEnabled(false);
        candleStickChart.getAxisLeft().setEnabled(true);
        candleStickChart.getAxisRight().setEnabled(true);
        candleStickChart.getLegend().setEnabled(false);
        candleStickChart.getXAxis().setEnabled(true);
        candleStickChart.setViewPortOffsets(0, 0, 0, 0);
    }

    private void initializeLineChart(LineChart lineChart)
    {
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(false);
        lineChart.setDrawMarkers(true);
        lineChart.setDoubleTapToZoomEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setScaleEnabled(false);
        lineChart.setDragEnabled(true);
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisLeft().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getXAxis().setEnabled(false);
        lineChart.setViewPortOffsets(0, 0, 0, 0);
    }

    private void valueSelectedEvent(Entry e)
    {
        int index = lineChart.getData().getDataSets().get(0).getEntryIndex(e);
        String date;
        String volumePlaceholder;
        String pricePlaceholder;
        String timestampPlaceholder;

        barChart.highlightValue(barChart.getData().getDataSets().get(0).getEntryForIndex(index).getX(), 0, index);

        if(dataChartList.size() > 200)
        {
            date = getDate(dataChartList.get((int) Math.floor(dataChartList.size() / 200) * index).getTimestamp() * 1000);
        }
        else
        {
            date = getDate(dataChartList.get(index).getTimestamp() * 1000);
        }

        volumePlaceholder = PlaceholderManager.getVolumeString(numberConformer(barChart.getData().getDataSets().get(0).getEntryForIndex(index).getY()), this);
        pricePlaceholder = PlaceholderManager.getPriceString(numberConformer(e.getY()), this);
        timestampPlaceholder = PlaceholderManager.getTimestampString(date, this);

        ((TextView) findViewById(R.id.volumeHightlight)).setText(volumePlaceholder);
        findViewById(R.id.volumeHightlight).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.priceHightlight)).setText(pricePlaceholder);
        findViewById(R.id.priceHightlight).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.timestampHightlight)).setText(timestampPlaceholder);
        findViewById(R.id.timestampHightlight).setVisibility(View.VISIBLE);

    }

    private boolean toucheEvent(MotionEvent motionEvent)
    {
        if(motionEvent.getAction() == MotionEvent.ACTION_UP)
        {
            lineChart.highlightValue(null);
            updateFluctuation(lineChart.getData().getDataSets().get(0).getEntryForIndex(0).getY(), lineChart.getData().getDataSets().get(0).getEntryForIndex(lineChart.getData().getDataSets().get(0).getEntryCount() - 1).getY());
            barChart.highlightValues(null);
            hideDataIndicators();
        }

        return false;
    }

    private void hideDataIndicators()
    {
        ((TextView) findViewById(R.id.volumeHightlight)).setText(".\n.");
        findViewById(R.id.volumeHightlight).setVisibility(View.INVISIBLE);
        ((TextView) findViewById(R.id.priceHightlight)).setText(".\n.");
        findViewById(R.id.priceHightlight).setVisibility(View.INVISIBLE);
        ((TextView) findViewById(R.id.timestampHightlight)).setText(".\n.");
        findViewById(R.id.timestampHightlight).setVisibility(View.INVISIBLE);
    }

    private String getDate(long timeStamp){

        try{
            SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm dd/MM/yyyy", Locale.getDefault());
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }

    private BarData generateVolumeChartSet()
    {
        BarDataSet dataSet;
        ArrayList<BarEntry> values = new ArrayList<>();

        int offset = (int) Math.floor(dataChartList.size() / 200);

        if(offset < 1)
        {
            offset = 1;
        }

        for(int i = 0, j = 0; i < dataChartList.size(); i += offset, j++)
        {
            values.add(new BarEntry(j, (float) dataChartList.get(j).getVolumeTo()));
        }

        dataSet = new BarDataSet(values, "Volume");
        dataSet.setDrawIcons(false);
        dataSet.setColor(Color.GRAY);
        dataSet.setDrawValues(false);
        dataSet.setHighlightEnabled(true);
        dataSet.setHighLightColor(currency.getChartColor());

        return new BarData(dataSet);
    }

    private String numberConformer(double number)
    {
        String str;

        if(abs(number) > 1)
        {
            str = String.format( Locale.UK, "%.2f", number).replaceAll("\\.?0*$", "");
        }
        else
        {
            str = String.format( Locale.UK, "%.4f", number).replaceAll("\\.?0*$", "");
        }

        if(!str.equals("Infinity"))
        {
            int counter = 0;
            int i = str.indexOf(".");
            if(i <= 0)
            {
                i = str.length();
            }
            for(i -= 1; i > 0; i--)
            {
                counter++;
                if(counter == 3)
                {
                    str = str.substring(0, i) + " " + str.substring(i, str.length());
                    counter = 0;
                }
            }
        }

        return str;
    }

    private CandleData generatePriceCandleStickChartSet()
    {
        CandleDataSet dataSet;
        ArrayList<CandleEntry> values = new ArrayList<>();

        int offsetRange = (int) Math.floor(dataChartList.size() / 200);

        if(offsetRange < 1)
        {
            offsetRange = 1;
        }

        for(int i = 0, j = 0; i < dataChartList.size(); i+= offsetRange, j++)
        {
            values.add(new CandleEntry(j, (float) dataChartList.get(i).getHigh()
                    , (float) dataChartList.get(i).getLow()
                    , (float) dataChartList.get(i).getOpen()
                    , (float) dataChartList.get(i).getClose()));
        }

        dataSet = new CandleDataSet(values, "History");
        dataSet.setDrawIcons(false);
        dataSet.setDrawValues(false);
        dataSet.setDecreasingColor(getColor(R.color.decreaseCandle));
        dataSet.setShowCandleBar(true);
        dataSet.setShadowColorSameAsCandle(true);
        dataSet.setDecreasingPaintStyle(Paint.Style.FILL);
        dataSet.setIncreasingColor(getColor(R.color.increaseCandle));
        dataSet.setIncreasingPaintStyle(Paint.Style.STROKE);
        dataSet.setNeutralColor(getColor(R.color.increaseCandle));
        dataSet.setHighLightColor(getColor(R.color.colorAccent));
        dataSet.setDrawHorizontalHighlightIndicator(false);

        return new CandleData(dataSet);
    }

    private LineData generatePriceLineChartSet()
    {
        LineDataSet dataSet;
        ArrayList<Entry> values = new ArrayList<>();

        int offsetRange = (int) Math.floor(dataChartList.size() / 200);

        if(offsetRange < 1)
        {
            offsetRange = 1;
        }

        for(int i = 0, j = 0; i < dataChartList.size(); i += offsetRange, j++)
        {
            values.add(new Entry(j, (float) dataChartList.get(i).getOpen()));
        }

        dataSet = new LineDataSet(values, "History");
        dataSet.setDrawIcons(false);
        dataSet.setColor(currency.getChartColor());
        dataSet.setLineWidth(1);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getColorWithAlpha(currency.getChartColor(), 0.5f));
        dataSet.setFormLineWidth(1);
        dataSet.setFormSize(15);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setHighlightEnabled(true);
        dataSet.setDrawHorizontalHighlightIndicator(false);
        dataSet.setHighLightColor(currency.getChartColor());

        return new LineData(dataSet);
    }

    private void updateGeneralData(float start, float end)
    {
        double totalVolume = dataChartList.get(0).getVolumeTo();
        double highestPrice = dataChartList.get(0).getOpen();
        double lowestPrice = dataChartList.get(0).getOpen();

        updateFluctuation(start, end);

        ((TextView) findViewById(R.id.txtViewPriceStart)).setText(PlaceholderManager.getValueString(numberConformer(start), this));
        ((TextView) findViewById(R.id.txtViewPriceNow)).setText(PlaceholderManager.getValueString(numberConformer(end), this));

        for(int i = 1; i < dataChartList.size(); i++)
        {
            totalVolume += dataChartList.get(i).getVolumeTo();

            if(highestPrice < dataChartList.get(i).getOpen())
            {
                highestPrice = dataChartList.get(i).getOpen();
            }

            if(lowestPrice > dataChartList.get(i).getOpen())
            {
                lowestPrice = dataChartList.get(i).getOpen();
            }
        }

        ((TextView) findViewById(R.id.totalVolume)).setText(PlaceholderManager.getValueString(numberConformer(totalVolume), this));
        ((TextView) findViewById(R.id.highestPrice)).setText(PlaceholderManager.getValueString(numberConformer(highestPrice), this));
        ((TextView) findViewById(R.id.lowestPrice)).setText(PlaceholderManager.getValueString(numberConformer(lowestPrice), this));
    }

    private void updateFluctuation(float start, float end)
    {
        float fluctuation = end - start;
        float percentageFluctuation = (fluctuation / start * 100);

        if(percentageFluctuation < 0)
        {
            ((TextView) findViewById(R.id.txtViewPercentage)).setTextColor(getResources().getColor(R.color.red));
        }
        else
        {
            ((TextView) findViewById(R.id.txtViewPercentage)).setTextColor(getResources().getColor(R.color.green));
        }

        ((TextView) findViewById(R.id.txtViewPercentage)).setText(getResources().getString(R.string.currencyPercentagePlaceholder, numberConformer(percentageFluctuation)));
    }

    private int getColorWithAlpha(int color, float ratio)
    {
        int transColor;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        transColor = Color.argb(alpha, r, g, b);

        return transColor;
    }

    private void drawTransactionList(ArrayList<Transaction> transactions)
    {
        transactionListAdapter = new TransactionListAdapter(this, transactions);

        transactionLayout.setAdapter(transactionListAdapter);
        transactionLayout.setTextFilterEnabled(false);
    }

    private void drawTradeList(ArrayList<com.nauk.moodl.DataManagers.CurrencyData.Trade> trades)
    {
        if(returnedTrades.size() > 20)
        {
            tradeLayout.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                }

                @Override
                public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0)
                    {
                        if(!flag_loading && tradeLayout.getCount() != returnedTrades.size() - 1)
                        {
                            flag_loading = true;

                            TradeAdder tradeAdder = new TradeAdder();
                            tradeAdder.execute();
                        }
                    }
                }
            });
        }

        tradeListAdapter = new TradeListAdapter(this, trades);

        tradeLayout.setAdapter(tradeListAdapter);
        tradeLayout.setTextFilterEnabled(false);

        findViewById(R.id.tradeLoaderIndicator).setVisibility(View.GONE);
    }

    private static void expand(final View v) {
        v.measure(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? CardView.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }



    public class TransactionUpdater extends AsyncTask<Void, Integer, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {

            if(Looper.myLooper() == null)
            {
                Looper.prepare();
            }

            final ArrayList<Transaction> transactionList = databaseManager.getCurrencyTransactionsForSymbol(currency.getSymbol());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    drawTransactionList(transactionList);
                }
            });

            return null;
        }
    }

    private void loadingIndicatorGenerator()
    {
        loadingFooter = LayoutInflater.from(CurrencyDetailsActivity.this).inflate(R.layout.listview_loading_indicator, null, false);

        Drawable drawable = ((ProgressBar) loadingFooter.findViewById(R.id.progressIndicator)).getIndeterminateDrawable();
        drawable.mutate();
        drawable.setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));
        drawable.invalidateSelf();

        tradeLayout.addFooterView(loadingFooter);
    }

    private class TradeAdder extends AsyncTask<Void, Integer, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            loadingIndicatorGenerator();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            final ArrayList<Trade> trades = new ArrayList<>();
            for(int i = tradeLayout.getCount(); i < tradeLayout.getCount() + 20 && i < returnedTrades.size(); i++)
            {
                trades.add(returnedTrades.get(i));
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tradeListAdapter.addAll(trades);
                    tradeListAdapter.notifyDataSetChanged();
                    flag_loading = false;

                    tradeLayout.removeFooterView(loadingFooter);
                }
            });
            /*binanceManager.updateTrades(new BinanceManager.BinanceCallBack() {
                @Override
                public void onSuccess() {
                    ArrayList<com.nauk.moodl.DataManagers.CurrencyData.Trade> trades = binanceManager.getTrades();
                    final ArrayList<com.nauk.moodl.DataManagers.CurrencyData.Trade> returnedTrades = new ArrayList<>();

                    for(int i = trades.size() - 1; i > 0 ; i--)
                    {
                        returnedTrades.add(trades.get(i));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tradeListAdapter.addAll(returnedTrades);
                            tradeListAdapter.notifyDataSetChanged();
                            flag_loading = false;

                            tradeLayout.removeFooterView(loadingFooter);
                        }
                    });
                }

                @Override
                public void onError(String error) {

                }
            }, currency.getSymbol(), tradeListAdapter.getItem(tradeListAdapter.getCount() - 1).getId());*/

            return null;
        }
    }

    private class TradeUpdater extends AsyncTask<Void, Integer, Void>
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
            binanceManager.updateTrades(new BinanceManager.BinanceCallBack() {
                @Override
                public void onSuccess() {
                    ArrayList<com.nauk.moodl.DataManagers.CurrencyData.Trade> trades = binanceManager.getTrades();
                    returnedTrades = new ArrayList<>();

                    for(int i = trades.size() - 1; i >= 0 ; i--)
                    {
                        returnedTrades.add(trades.get(i));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<Trade> trades = new ArrayList<>();

                            for(int i = 0; i < 20 && i < returnedTrades.size(); i++)
                            {
                                trades.add(returnedTrades.get(i));
                            }

                            drawTradeList(trades);
                        }
                    });
                }

                @Override
                public void onError(String error) {

                }
            }, currency.getSymbol());

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {

        }
    }
}
/*for(int i = 0; i < dataChartList.size(); i++)
        {*/
            /*if(counter == offset)
            {
                calendar.setTimeInMillis(dataChartList.get(i).getTimestamp()*1000);

                switch (pointFormat)
                {
                    case HOUR:
                        hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                        minute = String.valueOf(calendar.get(Calendar.MINUTE));

                        if(hour.length() < 2)
                        {
                            hour = "0" + hour;
                        }

                        if(minute.length() < 2)
                        {
                            minute = "0" + minute;
                        }

                        lineSet.addPoint(hour + ":" + minute, (float) dataChartList.get(i).getOpen());
                        break;
                    case DAY:
                        int dayIndex = calendar.get(Calendar.DAY_OF_WEEK)+1;

                        switch (dayIndex)
                        {
                            case Calendar.MONDAY:
                                dayName = "Mon";
                                break;
                            case Calendar.TUESDAY:
                                dayName = "Tue";
                                break;
                            case Calendar.WEDNESDAY:
                                dayName = "Wed";
                                break;
                            case Calendar.THURSDAY:
                                dayName = "Thu";
                                break;
                            case Calendar.FRIDAY:
                                dayName = "Fri";
                                break;
                            case Calendar.SATURDAY:
                                dayName = "Sat";
                                break;
                            case Calendar.SUNDAY:
                                dayName = "Sun";
                                break;
                        }

                        lineSet.addPoint(dayName, (float) dataChartList.get(i).getOpen());
                        break;
                    case MONTH:
                        dayNumber = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)+1);
                        monthNumber = String.valueOf(calendar.get(Calendar.MONTH)+1);

                        if(dayNumber.length() < 2)
                        {
                            dayNumber = '0' + dayNumber;
                        }

                        if(monthNumber.length() < 2)
                        {
                            monthNumber = '0' + monthNumber;
                        }

                        lineSet.addPoint(dayNumber + "/" + monthNumber, (float) dataChartList.get(i).getOpen());
                        break;
                    case YEAR:
                        int mb = calendar.get(Calendar.MONTH);

                        switch (mb)
                        {
                            case Calendar.JANUARY:
                                monthName = "Jan";
                                break;
                            case Calendar.FEBRUARY:
                                monthName = "Feb";
                                break;
                            case Calendar.MARCH:
                                monthName = "Mar";
                                break;
                            case Calendar.APRIL:
                                monthName = "Apr";
                                break;
                            case Calendar.MAY:
                                monthName = "May";
                                break;
                            case Calendar.JUNE:
                                monthName = "Jun";
                                break;
                            case Calendar.JULY:
                                monthName = "Jul";
                                break;
                            case Calendar.AUGUST:
                                monthName = "Aug";
                                break;
                            case Calendar.SEPTEMBER:
                                monthName = "Sep";
                                break;
                            case Calendar.OCTOBER:
                                monthName = "Oct";
                                break;
                            case Calendar.NOVEMBER:
                                monthName = "Nov";
                                break;
                            case Calendar.DECEMBER:
                                monthName = "Dec";
                                break;
                        }

                        lineSet.addPoint(monthName, (float) dataChartList.get(i).getOpen());
                        break;
                }
                counter = 0;
            }
            else
            {
                counter++;
                lineSet.addPoint("", (float) dataChartList.get(i).getOpen());
            }*/
            /*values.add(new Entry(i, (float) dataChartList.get(i).getOpen()));
        }*/