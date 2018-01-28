package com.nauk.coinfolio.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nauk.coinfolio.DataManagers.DatabaseManager;
import com.nauk.coinfolio.R;

public class RecordTransactionActivity extends AppCompatActivity {

    String coin;
    String symbol;
    TextView symbolTxtView;
    Button validateButton;
    DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_transaction);

        Intent intent = getIntent();
        coin = intent.getStringExtra("coin");
        symbol = intent.getStringExtra("symbol");

        setTitle("Add " + coin + " transaction");

        databaseManager = new DatabaseManager(this);

        validateButton = findViewById(R.id.validateButton);

        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //databaseManager.addCurrencyToManualCurrency();
            }
        });
    }
}
