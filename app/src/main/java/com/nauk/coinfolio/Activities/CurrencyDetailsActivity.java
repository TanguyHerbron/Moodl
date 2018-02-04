package com.nauk.coinfolio.Activities;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.daimajia.swipe.SwipeLayout;
import com.db.chart.animation.Animation;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.tooltip.Tooltip;
import com.db.chart.util.Tools;
import com.db.chart.view.LineChartView;
import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.DataManagers.CurrencyData.CurrencyDataChart;
import com.nauk.coinfolio.DataManagers.CurrencyData.Transaction;
import com.nauk.coinfolio.DataManagers.DatabaseManager;
import com.nauk.coinfolio.R;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
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

        if(currency.getDayPriceHistory().size() > 0)
        {
            drawChart();
        }
        else
        {
            TextView errorTextView = new TextView(this);
            errorTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 500));
            errorTextView.setText("Not enough data");
            errorTextView.setTag("chart_layout");
            errorTextView.setGravity(Gravity.CENTER);

            chartLayout.addView(errorTextView);
        }

        setTitle(currency.getName());

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        hasBeenModified = false;
    }

    private void drawChart()
    {
        final LineChartView chartView = new LineChartView(this);
        LineSet lineSet = new LineSet();
        double valMin;
        double valMax;
        int counter = 0;
        Calendar calendar = Calendar.getInstance(Locale.FRANCE);
        String hour;
        String minute;

        final List<CurrencyDataChart> dataChartList = currency.getDayPriceHistory();

        indexMin = 0;
        indexMax = 0;
        valMin = dataChartList.get(0).getOpen();
        valMax = dataChartList.get(0).getOpen();

        for(int i = 1; i < dataChartList.size(); i++)
        {
            if(valMax < dataChartList.get(i).getOpen())
            {
                valMax = dataChartList.get(i).getOpen();
                indexMax = i;
            }

            if(valMin > dataChartList.get(i).getOpen())
            {
                valMin = dataChartList.get(i).getOpen();
                indexMin = i;
            }
        }

        if(valMax == valMin)
        {
            valMin = 0;
            valMax *= 2;
        }

        chartView.setAxisBorderValues((float) valMin, (float) valMax);
        chartView.setYLabels(AxisRenderer.LabelPosition.NONE);
        chartView.setYAxis(false);
        chartView.setXAxis(false);

        for(int i = 0; i < dataChartList.size(); i+=10)
        {
            if(counter == 30)
            {
                calendar.setTimeInMillis(dataChartList.get(i).getTimestamp()*1000);

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
                counter = 0;
            }
            else
            {
                counter++;
                lineSet.addPoint("", (float) dataChartList.get(i).getOpen());
            }
        }

        lineSet.setSmooth(true);
        lineSet.setThickness(4);
        lineSet.setFill(getColorWitchAlpha(currency.getChartColor(), 0.5f));
        lineSet.setColor(currency.getChartColor());

        final LinearLayout.LayoutParams chartParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 500);
        chartParams.setMargins(0, 15, 0, 15);

        chartView.setLayoutParams(chartParams);

        tip = new Tooltip(this, R.layout.tooltip_layout, R.id.value);

        RelativeLayout.LayoutParams tipParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        tip.setLayoutParams(tipParams);
        tip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
        tip.setDimensions((int) Tools.fromDpToPx(75), (int) Tools.fromDpToPx(25));
        tip.setClickable(false);
        /*tip.setVerticalAlignment(Tooltip.Alignment.CENTER);
        tip.setHorizontalAlignment(Tooltip.Alignment.CENTER);
        tip.setDimensions((int) Tools.fromDpToPx(4), (int) Tools.fromDpToPx(4));
        tip.setClickable(false);*/

        final Tooltip tip2 = tip;

        chartView.addData(lineSet);
        chartView.setFadingEdgeLength(15);
        chartView.setLongClickable(true);

        //tip.prepare(chartView.getEntriesArea(0).get(0), (float) dataChartList.get(0).getOpen());

        chartView.setTooltips(tip);
        chartView.setTooltips(tip2);

        Runnable launchAction = new Runnable() {
            @Override
            public void run() {
                tip.prepare(chartView.getEntriesArea(0).get((int) indexMin/10), (float) dataChartList.get(indexMin).getOpen());
                tip2.prepare(chartView.getEntriesArea(0).get((int) indexMax/10), (float) dataChartList.get(indexMax).getOpen());
                chartView.showTooltip(tip, true);
                //chartView.showTooltip(tip2, true);
            }
        };

        chartView.show(new Animation().fromAlpha(0).withEndAction(launchAction));

        chartLayout.addView(chartView);
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

        //final ArrayAdapter<HashMap<Integer, Double>> transactionAdapter = new ArrayAdapter<HashMap<Integer, Double>>(CurrencyDetailsActivity.this, android.R.layout.simple_list_item_1, transactionList);

        /*Iterator transactionsIterator = transactionList.keySet().iterator();

        transactionList.se

        while(transactionsIterator.hasNext())
        {
            final TextView txtView = new TextView(this);
            Integer key = (Integer) transactionsIterator.next();

            txtView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            txtView.setTag(key);
            txtView.setText("Amount : " + transactionList.get(key));

            txtView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView thisTxtView = (TextView) view;
                    databaseManager.deleteTransactionFromId(Integer.parseInt(thisTxtView.getTag().toString()));
                    Log.d(CurrencyDetailsActivity.this.getResources().getString(R.string.debug), "Id : " + thisTxtView.getTag());
                    drawTransactionList();
                }
            });

            transactionLayout.addView(txtView);
        }*/

    }

}
