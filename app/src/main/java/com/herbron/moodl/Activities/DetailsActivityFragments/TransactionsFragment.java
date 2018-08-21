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
import com.herbron.moodl.CustomAdapters.TradeListAdapter;
import com.herbron.moodl.CustomAdapters.TransactionListAdapter;
import com.herbron.moodl.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Tiji on 13/05/2018.
 */

public class TransactionsFragment extends Fragment {

    private Currency currency;
    private View loadingFooter;
    private View view;
    //private ListView tradeLayout;
    private ListView transactionLayout;
    private boolean flag_loading;
    private List<BinanceManager> binanceManagerList;
    private DatabaseManager databaseManager;
    private TradeListAdapter tradeListAdapter;
    private ArrayList<com.herbron.moodl.DataManagers.CurrencyData.Trade> returnedTrades;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.detailsactivity_fragment_transactions, container, false);

        PreferencesManager preferencesManager = new PreferencesManager(getActivity().getBaseContext());

        currency = getActivity().getIntent().getParcelableExtra("currency");
        databaseManager = new DatabaseManager(getActivity().getBaseContext());
        binanceManagerList = databaseManager.getBinanceAccounts();
        //tradeLayout = view.findViewById(R.id.listTrades);
        transactionLayout = view.findViewById(R.id.listTransactions);

        flag_loading = false;

        TransactionUpdater transactionUpdater = new TransactionUpdater();
        transactionUpdater.execute();

        return view;
    }

    private class TransactionUpdater extends AsyncTask<Void, Integer, Void> implements BinanceUpdateNotifierInterface
    {
        private ArrayList<Object> displayedTransactions;
        private int nbTradeAccountUpdated;
        private int nbTransactionAccountUpdated;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            displayedTransactions = new ArrayList<>();
            nbTradeAccountUpdated = 0;
            nbTransactionAccountUpdated = 0;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(Looper.myLooper() == null)
            {
                Looper.prepare();
            }

            displayedTransactions.addAll(databaseManager.getCurrencyTransactionsForSymbol(currency.getSymbol()));

            for(int i = 0; i < binanceManagerList.size(); i++)
            {
                binanceManagerList.get(i).addListener(this);
                binanceManagerList.get(i).updateTrades(currency.getSymbol());
                binanceManagerList.get(i).updateTransactions(currency.getSymbol());
            }

            if(nbTradeAccountUpdated == binanceManagerList.size())
            {
                drawTransactionList(displayedTransactions);
            }

            return null;
        }

        @Override
        public void onBinanceTradesUpdated(List<Trade> trades) {
            nbTradeAccountUpdated++;

            displayedTransactions.addAll(trades);

            if(nbTradeAccountUpdated == binanceManagerList.size())
            {
                drawTransactionList(displayedTransactions);
            }
        }

        @Override
        public void onBinanceBalanceUpdateSuccess() {

        }

        @Override
        public void onBinanceBalanceUpdateError(int accountId, String error) {

        }

        private void drawTransactionList(ArrayList<Object> transactions)
        {
            Collections.sort(transactions, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    Long time1;
                    Long time2;

                    if(o1 instanceof Transaction)
                    {
                        time1 = ((Transaction) o1).getTimestamp();
                    }
                    else
                    {
                        time1 = ((Trade) o1).getTime();
                    }

                    if(o2 instanceof Transaction)
                    {
                        time2 = ((Transaction) o2).getTimestamp();
                    }
                    else
                    {
                        time2 = ((Trade) o2).getTime();
                    }

                    return time2.compareTo(time1);
                }
            });

            if(getActivity() != null)
            {
                TransactionListAdapter transactionListAdapter = new TransactionListAdapter(getActivity(), transactions);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        transactionLayout.setAdapter(transactionListAdapter);
                        transactionLayout.setTextFilterEnabled(false);
                    }
                });
            }
        }
    }

    /*public class TransactionUpdater extends AsyncTask<Void, Integer, Void>
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
    }*/

    /*private class TradeAdder extends AsyncTask<Void, Integer, Void>
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
            binanceManager.updateTrades(new BinanceManager.BinanceCallBack() {
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
            }, currency.getSymbol(), tradeListAdapter.getItem(tradeListAdapter.getCount() - 1).getId());

            return null;
        }
    }*/

    /*private void drawTransactionList(ArrayList<Transaction> transactions)
    {
        TransactionListAdapter transactionListAdapter = new TransactionListAdapter(getActivity(), transactions);

        transactionLayout.setAdapter(transactionListAdapter);
        transactionLayout.setTextFilterEnabled(false);
    }

    private class TradeUpdater extends AsyncTask<Void, Integer, Void> implements BinanceUpdateNotifierInterface
    {
        private List<Trade> trades;
        private int nbResponse;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            trades = new ArrayList<>();
            nbResponse = 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... params)
        {

            for(int i = 0; i < binanceManagerList.size(); i++)
            {
                binanceManagerList.get(i).addListener(this);
                binanceManagerList.get(i).updateTrades(currency.getSymbol());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {

        }

        @Override
        public void onBinanceTradesUpdated(List<Trade> newTrades) {
            trades.addAll(newTrades);

            nbResponse++;

            if(nbResponse == binanceManagerList.size())
            {
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

                            //drawTradeList(trades);
                        }
                    });
                } catch (NullPointerException e) {
                    Log.d("moodl", "TransactionsFragment do not need to be updated anymore");
                }
            }
        }

        @Override
        public void onBinanceBalanceUpdateSuccess() {

        }

        @Override
        public void onBinanceBalanceUpdateError(int accountId, String error) {

        }
    }*/

    /*private void loadingIndicatorGenerator()
    {
        loadingFooter = LayoutInflater.from(getActivity().getBaseContext()).inflate(R.layout.listview_loading_indicator, null, false);

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

        tradeListAdapter = new TradeListAdapter(getActivity().getBaseContext(), trades);

        tradeLayout.setAdapter(tradeListAdapter);
        tradeLayout.setTextFilterEnabled(false);

        view.findViewById(R.id.tradeLoaderIndicator).setVisibility(View.GONE);
    }*/
}
