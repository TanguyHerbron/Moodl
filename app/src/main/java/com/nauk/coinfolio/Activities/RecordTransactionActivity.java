package com.nauk.coinfolio.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.DataManagers.DatabaseManager;
import com.nauk.coinfolio.DataManagers.PreferencesManager;
import com.nauk.coinfolio.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RecordTransactionActivity extends AppCompatActivity {

    private String coin;
    private String symbol;
    private EditText amountTxtView;
    private TextView purchasedDate;
    private Button validateButton;
    private DatabaseManager databaseManager;
    private Calendar calendar;
    private SimpleDateFormat sdf;
    private PreferencesManager preferenceManager;
    private EditText purchasedPrice;
    private Currency currency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_transaction);

        Intent intent = getIntent();
        coin = intent.getStringExtra("coin");
        symbol = intent.getStringExtra("symbol");

        setTitle("Add " + coin + " transaction");

        sdf = new SimpleDateFormat(" HH:mm dd/MM/yyyy");

        calendar = Calendar.getInstance();

        currency = new Currency(coin, symbol);

        databaseManager = new DatabaseManager(this);
        preferenceManager = new PreferencesManager(this);

        validateButton = findViewById(R.id.validateButton);
        amountTxtView = findViewById(R.id.currencyAmount);
        purchasedDate = findViewById(R.id.purchaseDate);
        purchasedPrice = findViewById(R.id.purchasePrice);

        //purchasedPrice.setText();
        purchasedDate.setText(sdf.format(calendar.getTime()));

        purchasedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDatePicker();
            }
        });

        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseManager.addCurrencyToManualCurrency(symbol, Double.parseDouble(amountTxtView.getText().toString()), calendar.getTime(), purchasedPrice.getText().toString());
                preferenceManager.setMustUpdateSummary(true);
                Intent intent = new Intent(RecordTransactionActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        });

        currency.getTimestampPrice(this, preferenceManager.getDefaultCurrency(), new Currency.PriceCallBack() {
            @Override
            public void onSuccess(String price) {
                purchasedPrice.setText(price);
            }
        }, calendar.getTimeInMillis() / 1000);
    }

    private void createDatePicker()
    {
        new android.app.DatePickerDialog(
                RecordTransactionActivity.this,
                new android.app.DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        purchasedDate.setText(sdf.format(calendar.getTime()));
                        createTimePicker();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void createTimePicker()
    {
        new android.app.TimePickerDialog(
                RecordTransactionActivity.this,
                new android.app.TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hour);
                        calendar.set(Calendar.MINUTE, minute);
                        purchasedDate.setText(sdf.format(calendar.getTime()));

                        currency.getTimestampPrice(RecordTransactionActivity.this, preferenceManager.getDefaultCurrency(), new Currency.PriceCallBack() {
                            @Override
                            public void onSuccess(String price) {
                                purchasedPrice.setText(price);
                            }
                        }, calendar.getTimeInMillis() / 1000);
                        Log.d("coinfolio", "Time : " + calendar.getTimeInMillis());
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show();
    }
}
