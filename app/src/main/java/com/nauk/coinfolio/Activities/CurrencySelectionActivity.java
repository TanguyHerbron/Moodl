package com.nauk.coinfolio.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.LayoutManagers.CurrencyAdapter;
import com.nauk.coinfolio.R;

import java.util.ArrayList;

public class CurrencySelectionActivity extends AppCompatActivity {

    String[] currencySymbols;
    String[] currencyNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_add_currency);

        Intent intent = getIntent();

        currencySymbols = intent.getStringArrayExtra("currencyListSymbols");
        currencyNames = intent.getStringArrayExtra("currencyListNames");

        setTitle("Select a coin");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        final AutoCompleteTextView searchAutoComplete = findViewById(R.id.search_bar);

        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(CurrencySelectionActivity.this, RecordTransactionActivity.class);
                intent.putExtra("coin", searchAutoComplete.getText().toString());
                intent.putExtra("symbol", searchAutoComplete.getCompletionHint().toString());
                startActivity(intent);
                finish();
            }
        });

        String[] currencyFullname = new String[currencyNames.length];

        for(int i = 0; i < currencyFullname.length; i++)
        {
            currencyFullname[i] = currencyNames[i] + " " + currencySymbols[i];
        }

        ArrayList<Currency> currencyArrayList = new ArrayList<>();

        for(int i = 0; i < currencyNames.length; i++)
        {
            currencyArrayList.add(new Currency(currencyNames[i], currencySymbols[i]));
        }

        CurrencyAdapter adapter = new CurrencyAdapter(this, currencyArrayList);

        searchAutoComplete.setAdapter(adapter);
        searchAutoComplete.setThreshold(0);

        return true;
    }
}
