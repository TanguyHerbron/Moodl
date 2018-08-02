package com.herbron.moodl.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TimePicker;

import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.CurrencyData.CurrencyDetailsList;
import com.herbron.moodl.DataManagers.CurrencyData.Transaction;
import com.herbron.moodl.DataManagers.DatabaseManager;
import com.herbron.moodl.DataManagers.PreferencesManager;
import com.herbron.moodl.LayoutManagers.CoinSummaryListAdapter;
import com.herbron.moodl.LayoutManagers.CustomTabLayout;
import com.herbron.moodl.LayoutManagers.RecordTransactionPageAdapter;
import com.herbron.moodl.MoodlBox;
import com.herbron.moodl.PlaceholderManager;
import com.herbron.moodl.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static com.herbron.moodl.MoodlBox.getColor;

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
    private int transactionId;

    private Toolbar toolbar;
    private ImageView currencyIconImageView;

    private CurrencyDetailsList currencyDetailsList;

    private SearchView mainSearchView;

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_record:

                if(checkAmountText() && checkPriceText())
                {
                    double amount = Double.parseDouble(amountTxtView.getText().toString());
                    double purchasedPrice = Double.parseDouble(purchasedPriceEditText.getText().toString());
                    double fees = Double.parseDouble(feesTxtView.getText().toString());

                    if(!sellButton.isEnabled())
                    {
                        amount *= -1;
                    }

                    if(transactionId != -1)
                    {
                        databaseManager.updateTransactionWithId(transactionId, amount, calendar.getTime(), purchasedPrice, fees);
                    }
                    else
                    {
                        databaseManager.addCurrencyToManualCurrency(symbol, amount, calendar.getTime(), purchasedPrice, fees);
                    }

                    preferenceManager.setMustUpdateSummary(true);
                    Intent intent = new Intent(RecordTransactionActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    finish();
                }
                break;
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                break;
            default:
                break;
        }
        return true;
    }*/

    private boolean checkPriceText()
    {
        String purchasedPriceText = purchasedPriceEditText.getText().toString();
        double purchasedPrice;

        try {
            purchasedPrice = Double.parseDouble(purchasedPriceText);

            if(purchasedPrice < 0)
            {
                purchasedPriceEditText.setError(getResources().getString(R.string.field_negative));
            }
        } catch (NumberFormatException e) {
            purchasedPriceEditText.setError(getResources().getString(R.string.field_nan));

            return false;
        }

        if(purchasedPriceText.equals(""))
        {
            purchasedPriceEditText.setError(getResources().getString(R.string.field_empty));

            return false;
        }

        return true;
    }

    private boolean checkAmountText()
    {
        String amountText = amountTxtView.getText().toString();

        try {
            Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            amountTxtView.setError(getResources().getString(R.string.field_nan));

            return false;
        }

        if(amountText.equals(""))
        {
            amountTxtView.setError(getResources().getString(R.string.field_empty));

            return false;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_transaction);

        Intent intent = getIntent();

        sdf = new SimpleDateFormat(" HH:mm dd/MM/yyyy", Locale.UK);

        calendar = Calendar.getInstance();

        databaseManager = new DatabaseManager(this);
        preferenceManager = new PreferencesManager(this);

        //initializeViewElements();

        coin = intent.getStringExtra("coin");
        symbol = intent.getStringExtra("symbol");

        transactionId = intent.getIntExtra("transactionId", -1);

        toolbar = findViewById(R.id.toolbar);

        currencyIconImageView = findViewById(R.id.currencyIconImageView);

        setSupportActionBar(toolbar);

        setupAutoCompleteTextView();

        setupBackButton();

        CustomTabLayout tabLayout = findViewById(R.id.transactionsTabLayout);
        tabLayout.addTab(0, "Buy");
        tabLayout.addTab(1, "Sell");
        tabLayout.addTab(2, "Transfer");
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = findViewById(R.id.transactionsViewPager);
        final RecordTransactionPageAdapter pageAdapter = new RecordTransactionPageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pageAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setupAutoCompleteTextView()
    {
        currencyDetailsList = CurrencyDetailsList.getInstance(this);

        CoinSummaryListAdapter adapter = new CoinSummaryListAdapter(this, R.layout.custom_summary_coin_row, new ArrayList<>(currencyDetailsList.getCurrenciesDenomination()));

        AutoCompleteTextView coin_autoCompleteTextView = findViewById(R.id.coin_autoCompleteTextView);
        coin_autoCompleteTextView.setThreshold(0);
        coin_autoCompleteTextView.setAdapter(adapter);
        coin_autoCompleteTextView.setTextColor(getResources().getColor(R.color.white));
        coin_autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currencyIconImageView.setImageBitmap(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        coin_autoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                {
                    coin_autoCompleteTextView.showDropDown();
                }
                else
                {
                    coin_autoCompleteTextView.dismissDropDown();
                }
            }
        });

        coin_autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Currency currency = (Currency) coin_autoCompleteTextView.getAdapter().getItem(position);

                coin_autoCompleteTextView.setText(PlaceholderManager.getDenomination(currency.getName(), currency.getSymbol(), getBaseContext()));
                toolbar.requestFocus();
                hideSoftKeyboard(RecordTransactionActivity.this);

                RecordTransactionActivity.this.currency = currency;

                IconDownloaderTask iconDownloaderTask = new IconDownloaderTask();
                iconDownloaderTask.execute();
            }
        });
    }

    private class IconDownloaderTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            String iconUrl = MoodlBox.getIconUrl(currency.getSymbol(), 500, currencyDetailsList);

            if(iconUrl != null)
            {
                MoodlBox.getBitmapFromURL(iconUrl, currency.getSymbol(), getResources(), getBaseContext(), new HomeActivity.IconCallBack() {
                    @Override
                    public void onSuccess(Bitmap bitmapIcon) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currencyIconImageView.setImageBitmap(bitmapIcon);
                            }
                        });
                    }
                });
            }
            else
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currencyIconImageView.setBackground(getResources().getDrawable(R.mipmap.ic_launcher_moodl));
                    }
                });
            }
            return null;
        }
    }

        /*if(transactionId != -1)
        {
            setTitle(PlaceholderManager.getEditTransactionString(coin, getBaseContext()));

            DatabaseManager databaseManager = new DatabaseManager(this);
            Transaction transaction = databaseManager.getCurrencyTransactionById(transactionId);

            symbolTxtView.setText(transaction.getSymbol());
            amountTxtView.setText(String.valueOf(transaction.getAmount()));
            purchaseDate.setText(sdf.format(transaction.getTimestamp()));
            feesTxtView.setText(String.valueOf(transaction.getFees()));
        }
        else
        {
            setTitle(getString(R.string.new_transaction));

            purchaseDate.setText(sdf.format(calendar.getTime()));
            symbolTxtView.setText(symbol);
            feesTxtView.setText(String.valueOf(0));
        }

        currency = new Currency(coin, symbol);

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

        //initializeButtons();

        currency.getTimestampPrice(this, preferenceManager.getDefaultCurrency(), new Currency.PriceCallBack() {
            @Override
            public void onSuccess(String price) {
                purchasedPriceEditText.setText(price);
            }
        }, calendar.getTimeInMillis() / 1000);*/

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    /*private void initializeButtons()
    {
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
    }

    private void initializeViewElements()
    {
        symbolTxtView = findViewById(R.id.currencySymbol);
        amountTxtView = findViewById(R.id.currencyAmount);
        feesTxtView = findViewById(R.id.feesTextView);
        purchasedDateLayout = findViewById(R.id.input_purchase_date);
        purchaseDate = findViewById(R.id.purchaseDate);
        purchasedPriceEditText = findViewById(R.id.purchasePrice);
        buyButton = findViewById(R.id.buyButton);
        sellButton = findViewById(R.id.sellButton);
        transferButton = findViewById(R.id.transfertButton);
    }*/

    private void setupBackButton()
    {
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show();
    }
}
