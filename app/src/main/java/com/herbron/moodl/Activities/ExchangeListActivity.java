package com.herbron.moodl.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.herbron.moodl.DataManagers.DatabaseManager;
import com.herbron.moodl.CustomAdapters.ExchangeDescriptionListAdapter;
import com.herbron.moodl.R;

public class ExchangeListActivity extends AppCompatActivity {

    private DatabaseManager databaseManager;
    private ListView exchangeListView;
    private ExchangeDescriptionListAdapter exchangeDescriptionListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        databaseManager = new DatabaseManager(this);
        exchangeListView = findViewById(R.id.exchange_listView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExchangeListActivity.this, AddExchangeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        exchangeDescriptionListAdapter = new ExchangeDescriptionListAdapter(getApplicationContext(), databaseManager.getExchanges());

        exchangeListView.setAdapter(exchangeDescriptionListAdapter);
        exchangeDescriptionListAdapter.notifyDataSetChanged();
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
