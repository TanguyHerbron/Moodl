package com.herbron.moodl.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.herbron.moodl.DataManagers.DatabaseManager;
import com.herbron.moodl.DataManagers.ExchangeManager.Exchange;
import com.herbron.moodl.LayoutManagers.ExchangeListAdapter;
import com.herbron.moodl.R;

import java.util.ArrayList;
import java.util.List;

import static com.herbron.moodl.DataManagers.DatabaseManager.BINANCE_TYPE;
import static com.herbron.moodl.DataManagers.DatabaseManager.HITBTC_TYPE;

public class ExchangeListActivity extends AppCompatActivity {

    private DatabaseManager databaseManager;
    private ListView exchangeListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        databaseManager = new DatabaseManager(this);

        ArrayList<Exchange> exchangeList = new ArrayList<>();
        exchangeList.add(new Exchange(0, "Main account", BINANCE_TYPE, "Account with main balance & trading bot", "0000", "0000"));
        exchangeList.add(new Exchange(1, "Hit account", HITBTC_TYPE, "BCN account and HIT", "0001", "0001"));

        ExchangeListAdapter exchangeListAdapter = new ExchangeListAdapter(getApplicationContext(), exchangeList);

        exchangeListView = findViewById(R.id.exchange_listView);
        exchangeListView.setAdapter(exchangeListAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
