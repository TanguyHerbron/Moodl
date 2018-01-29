package com.nauk.coinfolio.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.nauk.coinfolio.DataManagers.DatabaseManager;
import com.nauk.coinfolio.R;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Iterator;

public class CurrencyDetailsActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private LinearLayout transactionLayout;
    private DatabaseManager databaseManager;
    private String symbol;

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

        symbol = intent.getStringExtra("symbol");

        databaseManager = new DatabaseManager(this);

        viewFlipper = findViewById(R.id.vfCurrencyDetails);
        transactionLayout = findViewById(R.id.transactionsLinearLayout);

        drawTransactionList();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void drawTransactionList()
    {
        transactionLayout.removeAllViews();

        HashMap<Integer, Double> transactionList = databaseManager.getCurrencyTransactions(symbol);

        Iterator transactionsIterator = transactionList.keySet().iterator();

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
        }

    }

}
