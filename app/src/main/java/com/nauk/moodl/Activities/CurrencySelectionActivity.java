package com.nauk.moodl.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.nauk.moodl.DataManagers.BalanceManager;
import com.nauk.moodl.DataManagers.CurrencyData.Currency;
import com.nauk.moodl.DataManagers.CurrencyData.CurrencyDetailsList;
import com.nauk.moodl.DataManagers.DatabaseManager;
import com.nauk.moodl.DataManagers.PreferencesManager;
import com.nauk.moodl.LayoutManagers.CurrencyListAdapter;
import com.nauk.moodl.R;

import java.util.ArrayList;
import java.util.List;

public class CurrencySelectionActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private CurrencyListAdapter adapter;
    private ListView listView;
    private android.widget.Filter filter;
    private CurrencyDetailsList currencyDetailsList;
    private boolean isWatchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_add_currency);

        currencyDetailsList = CurrencyDetailsList.getInstance(this);

        setTitle("Select a coin");

        Intent intent = getIntent();
        isWatchList = intent.getBooleanExtra("isWatchList", false);

        ListLoader listLoader = new ListLoader();
        listLoader.execute();
    }

    private void setupSearchView()
    {
        SearchView searchView = findViewById(R.id.search_bar);

        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(false);
        searchView.onActionViewExpanded();
    }

    private void setupAdapter()
    {
        List<String> currencyNames = currencyDetailsList.getCurrenciesName();
        List<String> currencySymbols = currencyDetailsList.getCurrenciesSymbol();

        ArrayList<Currency> currencyArrayList = new ArrayList<>();

        for(int i = 0; i < currencyNames.size(); i++)
        {
            currencyArrayList.add(new Currency(currencyNames.get(i), currencySymbols.get(i)));
        }

        adapter = new CurrencyListAdapter(this, currencyArrayList);
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

                if(isWatchList)
                {
                    PreferencesManager preferencesManager = new PreferencesManager(getApplicationContext());
                    DatabaseManager databaseManager = new DatabaseManager(getApplicationContext());

                    databaseManager.addCurrencyToWatchlist(selectedCurrency);
                    preferencesManager.setMustUpdateWatchlist(true);
                }
                else
                {
                    Intent intent = new Intent(CurrencySelectionActivity.this, RecordTransactionActivity.class);
                    intent.putExtra("coin", selectedCurrency.getName());
                    intent.putExtra("symbol", selectedCurrency.getSymbol());
                    startActivity(intent);
                }

                finish();
            }
        });

        filter = adapter.getFilter();
    }

    private static void expand(final View v) {
        v.measure(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? CardView.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    @Override
    public boolean onQueryTextChange(String text)
    {
        adapter.getFilter().filter(text);

        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        return false;
    }

    private void detailsEvent()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setupAdapter();
                setupList();
                setupSearchView();

                expand(findViewById(R.id.listContainerLayout));
                findViewById(R.id.currencyListProgressBar).setVisibility(View.GONE);
            }
        });
    }

    private class ListLoader extends AsyncTask<Void, Integer, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            if(Looper.myLooper() == null)
            {
                Looper.prepare();
            }

            if(!currencyDetailsList.isUpToDate())
            {
                currencyDetailsList.update(new BalanceManager.IconCallBack() {
                    @Override
                    public void onSuccess() {
                        detailsEvent();
                    }
                });
            }
            else
            {
                detailsEvent();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {

        }
    }
}
