package com.nauk.moodl.DataManagers;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nauk.moodl.DataManagers.CurrencyData.Currency;

import java.util.List;

/**
 * Created by Tiji on 13/04/2018.
 */

public class WatchlistManager {

    private android.content.Context context;
    private RequestQueue requestQueue;
    private DatabaseManager databaseManager;
    private List<Currency> watchlistCurrencies;

    public WatchlistManager(android.content.Context context)
    {
        this.context = context;

        requestQueue = Volley.newRequestQueue(context);
        databaseManager = new DatabaseManager(context);
    }

    public void updateWatchlist()
    {
        watchlistCurrencies = databaseManager.getAllCurrenciesFromWatchlist();
    }

    public List<Currency> getWatchlist()
    {
        return watchlistCurrencies;
    }
}
