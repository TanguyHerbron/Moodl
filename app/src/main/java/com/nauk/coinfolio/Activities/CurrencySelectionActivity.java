package com.nauk.coinfolio.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.nauk.coinfolio.DataManagers.CurrencyData.Currency;
import com.nauk.coinfolio.LayoutManagers.CurrencyAdapter;
import com.nauk.coinfolio.R;

import java.util.ArrayList;

public class CurrencySelectionActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private String[] currencySymbols;
    private String[] currencyNames;
    private CurrencyAdapter adapter;
    private ListView listView;
    private android.widget.Filter filter;

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

        setupAdapter();

        setupList();

        SearchView searchView = findViewById(R.id.search_bar);

        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);
        searchView.onActionViewExpanded();
    }

    private void setupAdapter()
    {
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

        adapter = new CurrencyAdapter(this, currencyArrayList);
    }

    private void setupList()
    {
        listView = findViewById(R.id.coinsPreview);

        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(false);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Currency selectedCurrency = (Currency) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(CurrencySelectionActivity.this, RecordTransactionActivity.class);
                intent.putExtra("coin", selectedCurrency.getName());
                intent.putExtra("symbol", selectedCurrency.getSymbol());
                startActivity(intent);
                finish();
            }
        });

        filter = adapter.getFilter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        /*final AutoCompleteTextView searchAutoComplete = findViewById(R.id.search_bar);

        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Currency selectedCurrency = (Currency) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(CurrencySelectionActivity.this, RecordTransactionActivity.class);
                intent.putExtra("coin", selectedCurrency.getName());
                intent.putExtra("symbol", selectedCurrency.getSymbol());
                startActivity(intent);
                finish();
            }
        });

        searchAutoComplete.setAdapter(adapter);
        searchAutoComplete.setThreshold(0);*/

        return true;
    }

    @Override
    public boolean onQueryTextChange(String text)
    {
        filter.filter(text);

        if (TextUtils.isEmpty(text)) {
            listView.clearTextFilter();
        } else {
            listView.setFilterText(text);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        return false;
    }
}
