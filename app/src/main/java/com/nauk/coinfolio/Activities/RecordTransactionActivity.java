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

    private String coin;
    private String symbol;
    private TextView symbolTxtView;
    private TextView amountTxtView;
    private Button validateButton;
    private DatabaseManager databaseManager;

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

        amountTxtView = findViewById(R.id.currencyAmount);

        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseManager.addCurrencyToManualCurrency(symbol, Double.parseDouble(amountTxtView.getText().toString()));
                Intent intent = new Intent(RecordTransactionActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        });
    }
}
