package com.herbron.moodl.Activities;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.herbron.moodl.DataManagers.DatabaseManager;
import com.herbron.moodl.DataManagers.ExchangeManager.Exchange;
import com.herbron.moodl.R;

public class AddExchangeActivity extends AppCompatActivity {

    private LinearLayout setupExchangeLayout;
    private Spinner exchangeSpinner;
    private DatabaseManager databaseManager;

    private TextInputEditText accountLabelEditText;
    private TextInputEditText accountDescriptionEditText;
    private TextInputEditText publicKeyEditText;
    private TextInputEditText secretKeyEditText;
    private AppCompatButton saveExchangeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exchange);
        getApplicationContext().setTheme(R.style.InputActivityTheme);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseManager = new DatabaseManager(getBaseContext());

        exchangeSpinner = findViewById(R.id.exchange_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.supported_exchanges, R.layout.exchange_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        setupExchangeLayout = findViewById(R.id.exchange_setup_layout);

        exchangeSpinner.setAdapter(adapter);

        Intent callingIntent = getIntent();

        if(callingIntent.getBooleanExtra("isEdit", false))
        {
            startExchangeEditionForId(callingIntent.getIntExtra("exchangeId", -1));
        }
        else
        {
            exchangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    loadLayoutFor(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        setupBackButton();
    }

    private void loadLayoutFor(int exchangeType)
    {
        setupExchangeLayout.removeAllViews();
        exchangeSpinner.setSelection(exchangeType);

        switch (exchangeType)
        {
            case DatabaseManager.BINANCE_TYPE:
                setupExchangeLayout = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.binance_exchange_setup_layout, setupExchangeLayout, true);

                bindSetupViews();
                break;
            case DatabaseManager.HITBTC_TYPE:
                setupExchangeLayout = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.hitbtc_exchange_setup_layout, setupExchangeLayout, true);

                bindSetupViews();
                break;
        }
    }

    private void startExchangeEditionForId(int exchangeId)
    {
        Exchange exchangeInfos = databaseManager.getExchangeFromId(exchangeId);

        loadLayoutFor(exchangeInfos.getType());

        exchangeSpinner.setEnabled(false);

        accountLabelEditText.setText(exchangeInfos.getName());
        accountDescriptionEditText.setText(exchangeInfos.getDescription());
        publicKeyEditText.setText(exchangeInfos.getPublicKey());
        secretKeyEditText.setText(exchangeInfos.getPrivateKey());

        saveExchangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEditTextFilled(accountLabelEditText) && isEditTextFilled(publicKeyEditText) && isEditTextFilled(secretKeyEditText))
                {
                    databaseManager.deleteExchangeAccountFromId(exchangeInfos.getId());
                    databaseManager.addExchange(accountLabelEditText.getText().toString(), exchangeSpinner.getSelectedItemPosition()
                            , accountDescriptionEditText.getText().toString(), publicKeyEditText.getText().toString()
                            , secretKeyEditText.getText().toString());
                    finish();
                }
            }
        });
    }

    private void bindSetupViews()
    {
        accountLabelEditText = findViewById(R.id.account_label_editText);
        accountDescriptionEditText = findViewById(R.id.account_description_editText);
        publicKeyEditText = findViewById(R.id.publicKey_editText);
        secretKeyEditText = findViewById(R.id.secretKey_editText);

        saveExchangeButton = findViewById(R.id.saveExchangeButton);

        saveExchangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEditTextFilled(accountLabelEditText) && isEditTextFilled(publicKeyEditText) && isEditTextFilled(secretKeyEditText))
                {
                    databaseManager.addExchange(accountLabelEditText.getText().toString(), exchangeSpinner.getSelectedItemPosition()
                            , accountDescriptionEditText.getText().toString(), publicKeyEditText.getText().toString()
                            , secretKeyEditText.getText().toString());

                    finish();
                }
            }
        });
    }

    private boolean isEditTextFilled(TextInputEditText editText)
    {
        if(editText.getText().toString().equals("") || editText.getText().toString().equals(" "))
        {
            editText.setError(getResources().getString(R.string.must_be_filled));

            return false;
        }

        return true;
    }

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
}
