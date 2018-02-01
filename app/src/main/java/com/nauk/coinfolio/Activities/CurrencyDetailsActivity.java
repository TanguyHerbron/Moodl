package com.nauk.coinfolio.Activities;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.daimajia.swipe.SwipeLayout;
import com.db.chart.model.LineSet;
import com.db.chart.renderer.AxisRenderer;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_details);

        Intent intent = getIntent();

        //symbol = intent.getStringExtra("symbol");
        currency = (Currency) intent.getParcelableExtra("currency");

        databaseManager = new DatabaseManager(this);

        viewFlipper = findViewById(R.id.vfCurrencyDetails);
        transactionLayout = findViewById(R.id.listTransactions);
        chartLayout = findViewById(R.id.chartLayout);

        drawTransactionList();

        drawChart();

        setTitle(currency.getName());

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Log.d("coinfolio", "Color received : " + currency.getChartColor());
    }

    private void drawChart()
    {
        LineChartView chartView = new LineChartView(this);
        LineSet lineSet = new LineSet();
        double valMin;
        double valMax;
        int counter = 0;
        Calendar calendar = Calendar.getInstance(Locale.FRANCE);
        String hour;
        String minute;

        List<CurrencyDataChart> dataChartList = currency.getDayPriceHistory();

        valMin = dataChartList.get(0).getOpen();
        valMax = dataChartList.get(0).getOpen();

        for(int i = 1; i < dataChartList.size(); i++)
        {
            if(valMax < dataChartList.get(i).getOpen())
            {
                valMax = dataChartList.get(i).getOpen();
            }

            if(valMin > dataChartList.get(i).getOpen())
            {
                valMin = dataChartList.get(i).getOpen();
            }
        }

        if(valMax == valMin)
        {
            valMin = 0;
            valMax *= 2;
        }

        chartView.setAxisBorderValues((float) valMin, (float) valMax);
        chartView.setYLabels(AxisRenderer.LabelPosition.OUTSIDE);
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

        chartView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 500));

        chartView.addData(lineSet);

        chartLayout.addView(chartView);

        Log.d("coinfolio", "Color : " + currency.getChartColor());

        chartView.show();
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
