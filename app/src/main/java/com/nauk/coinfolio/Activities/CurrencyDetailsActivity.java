package com.nauk.coinfolio.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.daimajia.swipe.SwipeLayout;
import com.db.chart.animation.Animation;
import com.db.chart.model.LineSet;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.tooltip.Tooltip;
import com.db.chart.view.LineChartView;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.DataManagers.CurrencyData.CurrencyDataChart;
import com.nauk.coinfolio.DataManagers.CurrencyData.Transaction;
import com.nauk.coinfolio.DataManagers.DatabaseManager;
import com.nauk.coinfolio.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**Create a Parcelable**/

public class CurrencyDetailsActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private LinearLayout transactionLayout;
    private LinearLayout chartLayout;
    private DatabaseManager databaseManager;
    //private String symbol;
    private Currency currency;
    private boolean hasBeenModified;
    private Tooltip tip;
    private int indexMax;
    private int indexMin;
    private final static int HOUR = 0;
    private final static int DAY = 1;
    private final static int WEEK = 2;
    private final static int MONTH = 3;
    private final static int YEAR = 4;

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
        Log.d(this.getResources().getString(R.string.debug), "Back pressed");
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("update", hasBeenModified);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_details);

        Intent intent = getIntent();

        //symbol = intent.getStringExtra("symbol");
        currency = (Currency) intent.getParcelableExtra("currency");

        databaseManager = new DatabaseManager(this);

        viewFlipper = findViewById(R.id.vfCurrencyDetails);
        transactionLayout = findViewById(R.id.listTransactions);
        chartLayout = findViewById(R.id.chartsLayout);

        drawTransactionList();

        initializeButtons();

        if(currency.getHistoryMinutes().size() > 0)
        {
            drawChart(DAY, 1);
        }
        else
        {
            TextView errorTextView = new TextView(this);
            errorTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 750));
            errorTextView.setText("Not enough data");
            errorTextView.setTag("chart_layout");
            errorTextView.setGravity(Gravity.CENTER);

            chartLayout.addView(errorTextView, 0);
        }

        setTitle(currency.getName());

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        hasBeenModified = false;
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

        LinearLayout buttonLayout = (LinearLayout) v.getParent();

        for(int i = 0; i < buttonLayout.getChildCount(); i++)
        {
            Button button = (Button) buttonLayout.getChildAt(i);

            if(button != v)
            {
                button.setEnabled(true);
            }
        }

        chartEvent((Button) v);
    }

    private void chartEvent(Button button)
    {
        findViewById(R.id.chartView).setVisibility(View.GONE);
        findViewById(R.id.progressLayoutChart).setVisibility(View.VISIBLE);

        String interval = button.getText().toString().substring(button.getText().toString().length()-2);

        switch (interval)
        {
            case "1h":
                drawChart(HOUR, 1);
                break;
            case "3h":
                drawChart(HOUR, 3);
                break;
            case "1d":
                drawChart(DAY, 1);
                break;
            case "3d":
                currency.updateHistoryHours(this, new Currency.CurrencyCallBack() {
                    @Override
                    public void onSuccess(Currency currency) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawChart(CurrencyDetailsActivity.DAY, 3);
                            }
                        });
                    }
                });
                break;
            case "1w":
                currency.updateHistoryHours(this, new Currency.CurrencyCallBack() {
                    @Override
                    public void onSuccess(Currency currency) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawChart(CurrencyDetailsActivity.WEEK, 1);
                            }
                        });
                    }
                });
                break;
            case "1M":
                currency.updateHistoryDays(this, new Currency.CurrencyCallBack() {
                    @Override
                    public void onSuccess(Currency currency) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawChart(CurrencyDetailsActivity.MONTH, 1);
                            }
                        });
                    }
                });
                break;
            case "3M":
                currency.updateHistoryDays(this, new Currency.CurrencyCallBack() {
                    @Override
                    public void onSuccess(Currency currency) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawChart(CurrencyDetailsActivity.MONTH, 3);
                            }
                        });
                    }
                });
                break;
            case "6M":
                currency.updateHistoryDays(this, new Currency.CurrencyCallBack() {
                    @Override
                    public void onSuccess(Currency currency) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawChart(CurrencyDetailsActivity.MONTH, 6);
                            }
                        });
                    }
                });
                break;
            case "1y":
                currency.updateHistoryDays(this, new Currency.CurrencyCallBack() {
                    @Override
                    public void onSuccess(Currency currency) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawChart(CurrencyDetailsActivity.YEAR, 1);
                            }
                        });
                    }
                });
                break;
        }
    }

    private void drawChart(int timeUnit, int amount)
    {
        final LineChartView chartView = findViewById(R.id.chartView);
        LineSet lineSet = generateChartSet(timeUnit, amount);

        float valMin = lineSet.getMin().getValue();
        float valMax = lineSet.getMax().getValue();

        int indexMinValue;
        int indexMaxValue;

        for(int i = 0; i < lineSet.size(); i++)
        {
            if(lineSet.getEntry(i).getValue() == valMin)
            {
                indexMinValue = i;
            }

            if(lineSet.getEntry(i).getValue() == valMax)
            {
                indexMaxValue = i;
            }
        }

        chartView.reset();

        chartView.setAxisBorderValues((float) valMin, (float) valMax);
        chartView.setYLabels(AxisRenderer.LabelPosition.NONE);
        chartView.setYAxis(false);
        chartView.setXAxis(false);

        chartView.addData(lineSet);
        chartView.setFadingEdgeLength(15);
        chartView.setLongClickable(true);

        findViewById(R.id.chartView).setVisibility(View.VISIBLE);
        findViewById(R.id.progressLayoutChart).setVisibility(View.GONE);

        //chartView.show(new Animation().fromAlpha(0).withEndAction(launchAction));
        chartView.show(new Animation().fromAlpha(0));
    }

    private LineSet generateChartSet(int timeUnit, int amount)
    {
        List<CurrencyDataChart> dataChartList = new ArrayList<>();
        LineSet lineSet = new LineSet();
        int counter = 0;
        Calendar calendar = Calendar.getInstance(Locale.FRANCE);
        String hour;
        String minute;
        String dayName = "";
        String dayNumber;
        String monthName = "";
        String monthNumber;
        int offset = 10;
        int pointFormat = HOUR;

        switch (timeUnit)
        {
            case HOUR:
                dataChartList = currency.getHistoryMinutes().subList(currency.getHistoryMinutes().size()-(60*amount), currency.getHistoryMinutes().size());
                offset = 10 * amount;
                pointFormat = HOUR;
                break;
            case DAY:
                if(amount == 1)
                {
                    dataChartList = currency.getHistoryMinutes();
                    offset = 10 * 24;
                    pointFormat = HOUR;
                }
                else
                {
                    dataChartList = currency.getHistoryHours().subList(currency.getHistoryHours().size()-(24*amount), currency.getHistoryHours().size());
                    offset = 24;
                    pointFormat = DAY;
                }
                break;
            case WEEK:
                dataChartList = currency.getHistoryHours().subList(currency.getHistoryHours().size()-168, currency.getHistoryHours().size());
                offset = 28;
                pointFormat = DAY;
                break;
            case MONTH:
                switch (amount)
                {
                    case 1:
                        dataChartList = currency.getHistoryHours();
                        offset = 124;
                        pointFormat = MONTH;
                        break;
                    case 3:
                        dataChartList = currency.getHistoryDays().subList(currency.getHistoryDays().size()-93, currency.getHistoryDays().size());
                        offset = 15;
                        pointFormat = MONTH;
                        break;
                    case 6:
                        dataChartList = currency.getHistoryDays().subList(currency.getHistoryDays().size()-186, currency.getHistoryDays().size());
                        offset = 31;
                        pointFormat = MONTH;
                        break;
                }
                break;
            case YEAR:
                dataChartList = currency.getHistoryDays();
                offset = 30;
                pointFormat = YEAR;
                break;
        }

        for(int i = 0; i < dataChartList.size(); i++)
        {
            if(counter == offset)
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
            }
        }

        lineSet.setSmooth(true);
        lineSet.setThickness(3);
        lineSet.setFill(getColorWitchAlpha(currency.getChartColor(), 0.5f));
        lineSet.setColor(currency.getChartColor());

        return lineSet;
    }

    private int getColorWitchAlpha(int color, float ratio)
    {
        int transColor;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        transColor = Color.argb(alpha, r, g, b);

        return transColor;
    }

    private void drawTransactionList()
    {
        transactionLayout.removeAllViews();

        List<Transaction> transactionList = databaseManager.getCurrencyTransactions(currency.getSymbol());

        for(int i = 0; i < transactionList.size(); i++)
        {
            View view = LayoutInflater.from(this).inflate(R.layout.custom_transaction_row, null);
            TextView amountTxtView = view.findViewById(R.id.amountPurchased);
            TextView valueTxtView = view.findViewById(R.id.puchasedValue);
            TextView dateTxtView = view.findViewById(R.id.purchaseDate);

            LinearLayout deleteLayout = view.findViewById(R.id.deleteTransactionLayout);
            deleteLayout.setTag(transactionList.get(i).getTransactionId());

            deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    databaseManager.deleteTransactionFromId(Integer.parseInt(view.getTag().toString()));
                    Log.d(CurrencyDetailsActivity.this.getResources().getString(R.string.debug), "Id : " + view.getTag());
                    drawTransactionList();
                    hasBeenModified = true;
                }
            });

            amountTxtView.setText(transactionList.get(i).getAmount() + "");

            SwipeLayout swipeLayout =  view.findViewById(R.id.swipeLayout);

            //set show mode.
            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);

            //add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
            swipeLayout.addDrag(SwipeLayout.DragEdge.Left, view.findViewById(R.id.bottom_wrapper));

            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onClose(SwipeLayout layout) {
                    //when the SurfaceView totally cover the BottomView.
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                    //you are swiping.
                }

                @Override
                public void onStartOpen(SwipeLayout layout) {

                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    //when the BottomView totally show.
                }

                @Override
                public void onStartClose(SwipeLayout layout) {

                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                    //when user's hand released.
                }
            });

            transactionLayout.addView(view);
        }

    }

}
