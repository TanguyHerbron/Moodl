package com.nauk.moodl.Activities;

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

import com.nauk.moodl.DataManagers.CurrencyData.Currency;
import com.nauk.moodl.DataManagers.DatabaseManager;
import com.nauk.moodl.DataManagers.PreferencesManager;
import com.nauk.moodl.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RecordTransactionActivity extends AppCompatActivity {

    private String coin;
    private String symbol;
    private TextView symbolTxtView;
    private TextView purchasedDate;
    private TextView feesTxtView;
    private EditText amountTxtView;
    private Button validateButton;
    private Button receivedButton;
    private Button sentButton;
    private DatabaseManager databaseManager;
    private Calendar calendar;
    private SimpleDateFormat sdf;
    private PreferencesManager preferenceManager;
    private EditText purchasedPriceEditText;
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

        symbolTxtView = findViewById(R.id.currencySymbol);
        amountTxtView = findViewById(R.id.currencyAmount);
        feesTxtView = findViewById(R.id.feesTextView);
        purchasedDate = findViewById(R.id.purchaseDate);
        purchasedPriceEditText = findViewById(R.id.purchasePrice);
        validateButton = findViewById(R.id.validateButton);
        receivedButton = findViewById(R.id.receivedButton);
        sentButton = findViewById(R.id.sentButton);

        //purchasedPrice.setText();
        purchasedDate.setText(sdf.format(calendar.getTime()));
        symbolTxtView.setText(symbol);

        purchasedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDatePicker();
            }
        });

        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double amount = Double.parseDouble(amountTxtView.getText().toString());
                double purchasedPrice = Double.parseDouble(purchasedPriceEditText.getText().toString());
                double fees = Double.parseDouble(feesTxtView.getText().toString());

                if(!sentButton.isEnabled())
                {
                    amount *= -1;
                }

                databaseManager.addCurrencyToManualCurrency(symbol, amount, calendar.getTime(), purchasedPrice, fees);
                preferenceManager.setMustUpdateSummary(true);
                Intent intent = new Intent(RecordTransactionActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        });

        receivedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receivedButton.setEnabled(false);
                sentButton.setEnabled(true);
                purchasedPriceEditText.setVisibility(View.VISIBLE);
                feesTxtView.setVisibility(View.GONE);
            }
        });

        sentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receivedButton.setEnabled(true);
                sentButton.setEnabled(false);
                purchasedPriceEditText.setVisibility(View.GONE);
                feesTxtView.setVisibility(View.VISIBLE);
            }
        });

        currency.getTimestampPrice(this, preferenceManager.getDefaultCurrency(), new Currency.PriceCallBack() {
            @Override
            public void onSuccess(String price) {
                purchasedPriceEditText.setText(price);
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
                                purchasedPriceEditText.setText(price);
                            }
                        }, calendar.getTimeInMillis() / 1000);
                        Log.d("moodl", "Time : " + calendar.getTimeInMillis());
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show();
    }
}
