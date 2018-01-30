package com.nauk.coinfolio.Activities;

import android.content.Intent;
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
import com.nauk.coinfolio.DataManagers.CurrencyData.Transaction;
import com.nauk.coinfolio.DataManagers.DatabaseManager;
import com.nauk.coinfolio.R;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
        transactionLayout = findViewById(R.id.listTransactions);

        drawTransactionList();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void drawTransactionList()
    {
        transactionLayout.removeAllViews();

        List<Transaction> transactionList = databaseManager.getCurrencyTransactions(symbol);

        for(int i = 0; i < transactionList.size(); i++)
        {
            Log.d("coinfoliobeta", "test");
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
