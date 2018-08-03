package com.herbron.moodl.Activities.DetailsActivityFragments;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.herbron.moodl.DataNotifiers.BinanceUpdateNotifierInterface;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.CurrencyData.Trade;
import com.herbron.moodl.DataManagers.CurrencyData.Transaction;
import com.herbron.moodl.DataManagers.DatabaseManager;
import com.herbron.moodl.DataManagers.ExchangeManager.BinanceManager;
import com.herbron.moodl.DataManagers.PreferencesManager;
import com.herbron.moodl.LayoutManagers.TradeListAdapter;
import com.herbron.moodl.LayoutManagers.TransactionListAdapter;
import com.herbron.moodl.R;

import java.util.ArrayList;

/**
 * Created by Tiji on 13/05/2018.
 */

public class Transactions extends Fragment {

    private Currency currency;
    private View loadingFooter;
    private View view;
    private ListView tradeLayout;
    private ListView transactionLayout;
    private boolean flag_loading;
    private BinanceManager binanceManager;
    private DatabaseManager databaseManager;
    private TradeListAdapter tradeListAdapter;
    private ArrayList<com.herbron.moodl.DataManagers.CurrencyData.Trade> returnedTrades;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_transactions_detailsactivity, container, false);

        PreferencesManager preferencesManager = new PreferencesManager(getContext());

        currency = getActivity().getIntent().getParcelableExtra("currency");
        databaseManager = new DatabaseManager(getContext());
        //binanceManager = new BinanceManager(preferencesManager.getBinancePublicKey(), preferencesManager.getBinancePrivateKey());
        tradeLayout = view.findViewById(R.id.listTrades);
        transactionLayout = view.findViewById(R.id.listTransactions);

        flag_loading = false;

        TransactionUpdater transactionUpdater = new TransactionUpdater();
        transactionUpdater.execute();

        /*TradeUpdater updater = new TradeUpdater();
        updater.execute();*/

        return view;
    }

    private void loadingIndicatorGenerator()
    {
        loadingFooter = LayoutInflater.from(getContext()).inflate(R.layout.listview_loading_indicator, null, false);

        Drawable drawable = ((ProgressBar) loadingFooter.findViewById(R.id.progressIndicator)).getIndeterminateDrawable();
        drawable.mutate();
        drawable.setColorFilter(new PorterDuffColorFilter(currency.getChartColor(), PorterDuff.Mode.SRC_IN));
        drawable.invalidateSelf();

        tradeLayout.addFooterView(loadingFooter);
    }

    private void drawTradeList(ArrayList<com.herbron.moodl.DataManagers.CurrencyData.Trade> trades)
    {
        if(returnedTrades.size() > 20)
        {
            tradeLayout.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                }

                @Override
                public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0)
                    {
                        if(!flag_loading && tradeLayout.getCount() != returnedTrades.size() - 1)
                        {
                            flag_loading = true;

                            TradeAdder tradeAdder = new TradeAdder();
                            tradeAdder.execute();
                        }
                    }
                }
            });
        }

        tradeListAdapter = new TradeListAdapter(getContext(), trades);

        tradeLayout.setAdapter(tradeListAdapter);
        tradeLayout.setTextFilterEnabled(false);

        view.findViewById(R.id.tradeLoaderIndicator).setVisibility(View.GONE);
    }

    private class TradeAdder extends AsyncTask<Void, Integer, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            loadingIndicatorGenerator();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            final ArrayList<Trade> trades = new ArrayList<>();
            for(int i = tradeLayout.getCount(); i < tradeLayout.getCount() + 20 && i < returnedTrades.size(); i++)
            {
                trades.add(returnedTrades.get(i));
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tradeListAdapter.addAll(trades);
                    tradeListAdapter.notifyDataSetChanged();
                    flag_loading = false;

                    tradeLayout.removeFooterView(loadingFooter);
                }
            });
            /*binanceManager.updateTrades(new BinanceManager.BinanceCallBack() {
                @Override
                public void onSuccess() {
                    ArrayList<com.nauk.moodl.DataManagers.CurrencyData.Trade> trades = binanceManager.getTrades();
                    final ArrayList<com.nauk.moodl.DataManagers.CurrencyData.Trade> returnedTrades = new ArrayList<>();

                    for(int i = trades.size() - 1; i > 0 ; i--)
                    {
                        returnedTrades.add(trades.get(i));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tradeListAdapter.addAll(returnedTrades);
                            tradeListAdapter.notifyDataSetChanged();
                            flag_loading = false;

                            tradeLayout.removeFooterView(loadingFooter);
                        }
                    });
                }

                @Override
                public void onError(String error) {

                }
            }, currency.getSymbol(), tradeListAdapter.getItem(tradeListAdapter.getCount() - 1).getId());*/

            return null;
        }
    }

    private void drawTransactionList(ArrayList<Transaction> transactions)
    {
        TransactionListAdapter transactionListAdapter = new TransactionListAdapter(getContext(), transactions);

        transactionLayout.setAdapter(transactionListAdapter);
        transactionLayout.setTextFilterEnabled(false);
    }

    public class TransactionUpdater extends AsyncTask<Void, Integer, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {

            if(Looper.myLooper() == null)
            {
                Looper.prepare();
            }

            //binanceManager.updateTransactions(currency.getSymbol());

            final ArrayList<Transaction> transactionList = databaseManager.getCurrencyTransactionsForSymbol(currency.getSymbol());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    drawTransactionList(transactionList);
                }
            });

            return null;
        }
    }

    private class TradeUpdater extends AsyncTask<Void, Integer, Void> implements BinanceUpdateNotifierInterface
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

            binanceManager.updateTrades(currency.getSymbol());

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {

        }

        @Override
        public void onBinanceTradesUpdated() {
            ArrayList<Trade> trades = binanceManager.getTrades();
            returnedTrades = new ArrayList<>();

            for(int i = trades.size() - 1; i >= 0 ; i--)
            {
                returnedTrades.add(trades.get(i));
            }

            try {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<Trade> trades = new ArrayList<>();

                        for(int i = 0; i < 20 && i < returnedTrades.size(); i++)
                        {
                            trades.add(returnedTrades.get(i));
                        }

                        drawTradeList(trades);
                    }
                });
            } catch (NullPointerException e) {
                Log.d("moodl", "Transactions do not need to be updated anymore");
            }
        }

        @Override
        public void onBinanceBalanceUpdateSuccess() {

        }

        @Override
        public void onBinanceBalanceUpdateError(int accountId, String error) {

        }
    }
}
