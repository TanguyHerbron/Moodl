package com.nauk.moodl.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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
    private TextInputLayout purchasedDateLayout;
    private EditText purchaseDate;
    private TextView feesTxtView;
    private EditText amountTxtView;
    private Button buyButton;
    private Button sellButton;
    private Button transferButton;
    private DatabaseManager databaseManager;
    private Calendar calendar;
    private SimpleDateFormat sdf;
    private PreferencesManager preferenceManager;
    private EditText purchasedPriceEditText;
    private Currency currency;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_record_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_record:
                double amount = Double.parseDouble(amountTxtView.getText().toString());
                double purchasedPrice = Double.parseDouble(purchasedPriceEditText.getText().toString());
                double fees = Double.parseDouble(feesTxtView.getText().toString());

                if(!sellButton.isEnabled())
                {
                    amount *= -1;
                }

                databaseManager.addCurrencyToManualCurrency(symbol, amount, calendar.getTime(), purchasedPrice, fees);
                preferenceManager.setMustUpdateSummary(true);
                Intent intent = new Intent(RecordTransactionActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
        return true;
    }

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
        purchasedDateLayout = findViewById(R.id.input_purchase_date);
        purchaseDate = findViewById(R.id.purchaseDate);
        purchasedPriceEditText = findViewById(R.id.purchasePrice);
        buyButton = findViewById(R.id.buyButton);
        sellButton = findViewById(R.id.sellButton);
        transferButton = findViewById(R.id.transfertButton);

        //purchasedPrice.setText();
        purchaseDate.setText(sdf.format(calendar.getTime()));
        symbolTxtView.setText(symbol);
        feesTxtView.setText(String.valueOf(0));

        purchasedDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDatePicker();
            }
        });

        purchaseDate.setKeyListener(null);

        purchaseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDatePicker();
            }
        });

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyButton.setEnabled(false);
                sellButton.setEnabled(true);
                transferButton.setEnabled(true);
                findViewById(R.id.input_purchase_price).setVisibility(View.VISIBLE);
                findViewById(R.id.input_fees).setVisibility(View.GONE);
            }
        });

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyButton.setEnabled(true);
                sellButton.setEnabled(false);
                transferButton.setEnabled(true);
                findViewById(R.id.input_purchase_price).setVisibility(View.GONE);
                findViewById(R.id.input_fees).setVisibility(View.VISIBLE);
            }
        });

        transferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyButton.setEnabled(true);
                sellButton.setEnabled(true);
                transferButton.setEnabled(false);
                // Prepare transfer interface
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
                        purchaseDate.setText(sdf.format(calendar.getTime()));
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
                        purchaseDate.setText(sdf.format(calendar.getTime()));

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
