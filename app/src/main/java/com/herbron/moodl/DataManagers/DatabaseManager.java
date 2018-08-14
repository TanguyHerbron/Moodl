package com.herbron.moodl.DataManagers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.CurrencyData.Transaction;
import com.herbron.moodl.DataManagers.ExchangeManager.BinanceManager;
import com.herbron.moodl.DataManagers.ExchangeManager.Exchange;
import com.herbron.moodl.DataManagers.ExchangeManager.HitBtcManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Guitoune on 14/01/2018.
 */

public class DatabaseManager extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 14;

    private static final String DATABASE_NAME = "mdn.db";

    public static final String TABLE_MANUAL_TRANSACTIONS = "ManualTransactions";
    public static final String TABLE_EXCHANGE_KEYS = "ExchangeKeys";
    public static final String TABLE_WATCHLIST = "Watchlist";

    private static final String KEY_TRANSACTION_ID = "transactionId";
    private static final String KEY_TRANSACTION_SYMBOL = "symbol";
    private static final String KEY_TRANSACTION_AMOUNT = "amount";
    private static final String KEY_TRANSACTION_PAIR = "symPair";
    private static final String KEY_TRANSACTION_DATE = "transactionDate";
    private static final String KEY_TRANSACTION_PURCHASE_PRICE = "purchasePrice";
    private static final String KEY_TRANSACTION_SOURCE = "source";
    private static final String KEY_TRANSACTION_DESTINATION = "destination";
    private static final String KEY_TRANSACTION_FEES = "fees";
    private static final String KEY_TRANSACTION_FEE_CURRENCY = "feeCurrency";
    private static final String KEY_TRANSACTION_NOTES = "notes";

    private static final String KEY_EXCHANGE_ID = "idExchange";
    private static final String KEY_EXCHANGE_NAME = "name";
    private static final String KEY_EXCHANGE_TYPE = "type";
    private static final String KEY_EXCHANGE_DESCRIPTION = "description";
    private static final String KEY_EXCHANGE_PUBLIC_KEY = "publicKey";
    private static final String KEY_EXCHANGE_SECRET_KEY = "secretKey";
    private static final String KEY_EXCHANGE_IS_ENABLED = "enabled";

    private static final String KEY_WATCHLIST_ID = "idWatchlist";
    private static final String KEY_WATCHLIST_SYMBOL = "symbol";
    private static final String KEY_WATCHLIST_NAME = "name";
    private static final String KEY_WATCHLIST_POSITION = "position";

    public static final int BINANCE_TYPE = 0;
    public static final int HITBTC_TYPE = 1;

    public DatabaseManager(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MANUAL_TRANSACTIONS + "("
                + KEY_TRANSACTION_ID + " INTEGER PRIMARY KEY,"
                + KEY_TRANSACTION_SYMBOL + " VARCHAR(4),"
                + KEY_TRANSACTION_AMOUNT + " TEXT,"
                + KEY_TRANSACTION_DATE + " TEXT,"
                + KEY_TRANSACTION_PURCHASE_PRICE + " REAL,"
                + KEY_TRANSACTION_FEES + " REAL,"
                + KEY_TRANSACTION_NOTES + " TEXT,"
                + KEY_TRANSACTION_PAIR + " VARCHAR(4),"
                + KEY_TRANSACTION_FEE_CURRENCY + " VARCHAR(4),"
                + KEY_TRANSACTION_SOURCE + " TEXT,"
                + KEY_TRANSACTION_DESTINATION + " TEXT"
                + ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EXCHANGE_KEYS + "("
                + KEY_EXCHANGE_ID + " INTEGER PRIMARY KEY,"
                + KEY_EXCHANGE_NAME + " TEXT,"
                + KEY_EXCHANGE_TYPE + " INTEGER,"
                + KEY_EXCHANGE_DESCRIPTION + " TEXT,"
                + KEY_EXCHANGE_PUBLIC_KEY + " TEXT,"
                + KEY_EXCHANGE_SECRET_KEY + " TEXT,"
                + KEY_EXCHANGE_IS_ENABLED + " INTEGER"
                + ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_WATCHLIST + "("
                + KEY_WATCHLIST_ID + " INTEGER PRIMARY KEY,"
                + KEY_WATCHLIST_SYMBOL + " VARCHAR(4),"
                + KEY_WATCHLIST_NAME + " TEXT,"
                + KEY_WATCHLIST_POSITION + " INTEGER"
                + ");");

        //loadSample(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MANUAL_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXCHANGE_KEYS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WATCHLIST);

        onCreate(db);
    }

    private boolean isCurrencyInWatchlist(String symbol)
    {
        String searchQuerry = "SELECT * FROM " + TABLE_WATCHLIST + " WHERE " + KEY_WATCHLIST_SYMBOL + "='" + symbol + "'";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor result = db.rawQuery(searchQuerry, null);

        return result.moveToFirst();
    }

    public boolean addCurrencyToWatchlist(Currency currency)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        if(!isCurrencyInWatchlist(currency.getSymbol()))
        {
            ContentValues values = new ContentValues();

            values.put(KEY_WATCHLIST_SYMBOL, currency.getSymbol());
            values.put(KEY_WATCHLIST_NAME, currency.getName());
            values.put(KEY_WATCHLIST_POSITION, getWatchlistRowCount(db));

            db.insert(TABLE_WATCHLIST, null, values);
            db.close();

            return true;
        }

        return false;
    }

    public void addTransaction(String symbol, Double amount, Date date, Double purchasedPrice, Double fees, String note, String symbolFrom, String feeCurrency, String exchange)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_TRANSACTION_SYMBOL, symbol);
        values.put(KEY_TRANSACTION_AMOUNT, amount);
        values.put(KEY_TRANSACTION_DATE, date.getTime());
        values.put(KEY_TRANSACTION_PURCHASE_PRICE, purchasedPrice);
        values.put(KEY_TRANSACTION_FEES, fees);
        values.put(KEY_TRANSACTION_NOTES, note);
        values.put(KEY_TRANSACTION_PAIR, symbolFrom);
        values.put(KEY_TRANSACTION_FEE_CURRENCY, feeCurrency);
        values.put(KEY_TRANSACTION_SOURCE, exchange);

        db.insert(TABLE_MANUAL_TRANSACTIONS, null, values);
        db.close();
    }

    public void updateWatchlistPosition(String symbol, int position)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_WATCHLIST_POSITION, position);

        db.update(TABLE_WATCHLIST, cv, KEY_WATCHLIST_SYMBOL + "='" + symbol + "'", null);
    }

    private int getWatchlistRowCount(SQLiteDatabase db)
    {
        String countQuerry = "SELECT COUNT() FROM " + TABLE_WATCHLIST;
        Cursor result = db.rawQuery(countQuerry, null);

        result.moveToFirst();

        return result.getInt(result.getColumnIndex(KEY_WATCHLIST_ID));
    }

    public void deleteExchangeAccountFromId(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_EXCHANGE_KEYS, KEY_EXCHANGE_ID + " = " + id, null);
        db.close();
    }

    public void deleteCurrencyFromWatchlist(String symbol)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_WATCHLIST, KEY_WATCHLIST_SYMBOL + " = '" + symbol + "'", null);
        db.close();
    }

    public void disableExchangeAccount(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_EXCHANGE_IS_ENABLED, 0);

        db.update(TABLE_EXCHANGE_KEYS, cv, KEY_EXCHANGE_ID + "='" + id + "'", null);
    }

    public Exchange getExchangeFromId(int exchangeId)
    {
        String selectQuerry = "SELECT * FROM " + TABLE_EXCHANGE_KEYS + " WHERE " + KEY_EXCHANGE_ID + " = " + exchangeId;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery(selectQuerry, null);

        Exchange selectedExchange = null;

        if(result.moveToFirst())
        {
            selectedExchange = new Exchange(result.getInt(result.getColumnIndex(KEY_EXCHANGE_ID))
                    , result.getString(result.getColumnIndex(KEY_EXCHANGE_NAME))
                    , result.getInt(result.getColumnIndex(KEY_EXCHANGE_TYPE))
                    , result.getString(result.getColumnIndex(KEY_EXCHANGE_DESCRIPTION))
                    , result.getString(result.getColumnIndex(KEY_EXCHANGE_PUBLIC_KEY))
                    , result.getString(result.getColumnIndex(KEY_EXCHANGE_SECRET_KEY))
                    , (result.getInt(result.getColumnIndex(KEY_EXCHANGE_IS_ENABLED)) == 1));
        }

        return selectedExchange;
    }

    public ArrayList<Exchange> getExchanges()
    {
        String selectQuerry = "SELECT * FROM " + TABLE_EXCHANGE_KEYS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery(selectQuerry, null);

        ArrayList<Exchange> exchanges = new ArrayList<>();

        while(result.moveToNext())
        {
            exchanges.add(new Exchange(result.getInt(result.getColumnIndex(KEY_EXCHANGE_ID))
                    , result.getString(result.getColumnIndex(KEY_EXCHANGE_NAME))
                    , result.getInt(result.getColumnIndex(KEY_EXCHANGE_TYPE))
                    , result.getString(result.getColumnIndex(KEY_EXCHANGE_DESCRIPTION))
                    , result.getString(result.getColumnIndex(KEY_EXCHANGE_PUBLIC_KEY))
                    , result.getString(result.getColumnIndex(KEY_EXCHANGE_SECRET_KEY))
                    , (result.getInt(result.getColumnIndex(KEY_EXCHANGE_IS_ENABLED)) == 1)));
        }

        return exchanges;
    }

    public void addExchange(String name, int type, String description, String publicKey, String privateKey)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_EXCHANGE_NAME, name);
        values.put(KEY_EXCHANGE_TYPE, type);
        values.put(KEY_EXCHANGE_DESCRIPTION, description);
        values.put(KEY_EXCHANGE_PUBLIC_KEY, publicKey);
        values.put(KEY_EXCHANGE_SECRET_KEY, privateKey);
        values.put(KEY_EXCHANGE_IS_ENABLED, 1);

        db.insert(TABLE_EXCHANGE_KEYS, null, values);
        db.close();
    }

    public JSONArray getDatabaseBackup(Context context, String table, boolean encryptData)
    {
        String selectQuerry = "SELECT * FROM " + table;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery(selectQuerry, null);

        JSONArray backupArray = new JSONArray();

        while(result.moveToNext())
        {
            JSONObject backupObject = new JSONObject();

            for(int i = 0; i < result.getColumnCount(); i++)
            {
                try {
                    if(result.getString(i) != null)
                    {
                        if(encryptData)
                        {
                            backupObject.put(result.getColumnName(i), DataCrypter.encrypt(context, result.getString(i)));
                        }
                        else
                        {
                            backupObject.put(result.getColumnName(i), result.getString(i));
                        }
                    }
                    else
                    {
                        backupObject.put(result.getColumnName(i), "");
                    }
                } catch (JSONException e) {
                    Log.d("moodl", "Error while creating a json backup");
                }
            }

            backupArray.put(backupObject);
        }

        result.close();
        db.close();

        return backupArray;
    }

    public void wipeData(String table)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+ table);
    }

    public void addRawData(Context context, JSONObject rawValues, String table, boolean decrypt)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        while(rawValues.keys().hasNext())
        {
            String key = rawValues.keys().next();

            try {
                if(decrypt)
                {
                    values.put(key, DataCrypter.decrypt(context, rawValues.getString(key)));
                }
                else
                {
                    values.put(key, rawValues.getString(key));
                }
            } catch (JSONException e) {
                Log.d("moodl", "Error while inserting " + key + " " + e.getMessage());
            }
        }

        db.insert(table, null, values);
        db.close();
    }

    public void addRowApiKeys(JSONObject rawValues, Context context, boolean decrypt)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {

            if(decrypt)
            {
                values.put(KEY_EXCHANGE_NAME, DataCrypter.decrypt(context, rawValues.getString(KEY_EXCHANGE_NAME)));
                values.put(KEY_EXCHANGE_TYPE, DataCrypter.decrypt(context, rawValues.getString(KEY_EXCHANGE_TYPE)));
                values.put(KEY_EXCHANGE_DESCRIPTION, DataCrypter.decrypt(context, rawValues.getString(KEY_EXCHANGE_DESCRIPTION)));
                values.put(KEY_EXCHANGE_PUBLIC_KEY, DataCrypter.decrypt(context, rawValues.getString(KEY_EXCHANGE_PUBLIC_KEY)));
                values.put(KEY_EXCHANGE_SECRET_KEY, DataCrypter.decrypt(context, rawValues.getString(KEY_EXCHANGE_SECRET_KEY)));
                values.put(KEY_EXCHANGE_IS_ENABLED, DataCrypter.decrypt(context, rawValues.getString(KEY_EXCHANGE_IS_ENABLED)));
            }
            else
            {
                values.put(KEY_EXCHANGE_NAME, rawValues.getString(KEY_EXCHANGE_NAME));
                values.put(KEY_EXCHANGE_TYPE, rawValues.getString(KEY_EXCHANGE_TYPE));
                values.put(KEY_EXCHANGE_DESCRIPTION, rawValues.getString(KEY_EXCHANGE_DESCRIPTION));
                values.put(KEY_EXCHANGE_PUBLIC_KEY, rawValues.getString(KEY_EXCHANGE_PUBLIC_KEY));
                values.put(KEY_EXCHANGE_SECRET_KEY, rawValues.getString(KEY_EXCHANGE_SECRET_KEY));
                values.put(KEY_EXCHANGE_IS_ENABLED, rawValues.getString(KEY_EXCHANGE_IS_ENABLED));
            }
        } catch (JSONException e) {
            Log.d("moodl", "Error while inserting api key " + e.getMessage());
        }

        db.insert(TABLE_EXCHANGE_KEYS, null, values);
        db.close();
    }

    public void addRowWatchlist(JSONObject rawValues, Context context, boolean decrypt)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            if(decrypt)
            {
                values.put(KEY_WATCHLIST_SYMBOL, DataCrypter.decrypt(context, rawValues.getString(KEY_WATCHLIST_SYMBOL)));
                values.put(KEY_WATCHLIST_NAME, DataCrypter.decrypt(context, rawValues.getString(KEY_WATCHLIST_NAME)));
                values.put(KEY_WATCHLIST_POSITION, DataCrypter.decrypt(context, rawValues.getString(KEY_WATCHLIST_POSITION)));
            }
            else
            {
                values.put(KEY_WATCHLIST_SYMBOL, rawValues.getString(KEY_WATCHLIST_SYMBOL));
                values.put(KEY_WATCHLIST_NAME, rawValues.getString(KEY_WATCHLIST_NAME));
                values.put(KEY_WATCHLIST_POSITION, rawValues.getString(KEY_WATCHLIST_POSITION));
            }
        } catch (JSONException e) {
            Log.d("moodl", "Error while inserting watchlist " + e.getMessage());
        }

        db.insert(TABLE_WATCHLIST, null, values);
        db.close();
    }

    public void addRowTransaction(JSONObject rawValues, Context context, boolean decrypt)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            if(decrypt)
            {
                values.put(KEY_TRANSACTION_SYMBOL, DataCrypter.decrypt(context, rawValues.getString(KEY_TRANSACTION_SYMBOL)));
                values.put(KEY_TRANSACTION_AMOUNT, DataCrypter.decrypt(context, rawValues.getString(KEY_TRANSACTION_AMOUNT)));
                values.put(KEY_TRANSACTION_DATE, DataCrypter.decrypt(context, rawValues.getString(KEY_TRANSACTION_DATE)));
                values.put(KEY_TRANSACTION_PURCHASE_PRICE, DataCrypter.decrypt(context, rawValues.getString(KEY_TRANSACTION_PURCHASE_PRICE)));
                values.put(KEY_TRANSACTION_FEES, DataCrypter.decrypt(context, rawValues.getString(KEY_TRANSACTION_FEES)));
            }
            else
            {
                values.put(KEY_TRANSACTION_SYMBOL, rawValues.getString(KEY_TRANSACTION_SYMBOL));
                values.put(KEY_TRANSACTION_AMOUNT, rawValues.getString(KEY_TRANSACTION_AMOUNT));
                values.put(KEY_TRANSACTION_DATE, rawValues.getString(KEY_TRANSACTION_DATE));
                values.put(KEY_TRANSACTION_PURCHASE_PRICE, rawValues.getString(KEY_TRANSACTION_PURCHASE_PRICE));
                values.put(KEY_TRANSACTION_FEES, rawValues.getString(KEY_TRANSACTION_FEES));
            }
        } catch (JSONException e) {
            Log.d("moodl", "Error while inserting transaction " + e.getMessage());
        }

        db.insert(TABLE_MANUAL_TRANSACTIONS, null, values);
        db.close();
    }

    public List<Currency> getAllCurrenciesFromWatchlist()
    {
        String searchQuerry = "SELECT * FROM " + TABLE_WATCHLIST + " ORDER BY " + KEY_WATCHLIST_POSITION + " ASC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor resultList = db.rawQuery(searchQuerry, null);

        List<Currency> currencyList = new ArrayList<>();

        while(resultList.moveToNext())
        {
            currencyList.add(new Currency(resultList.getString(resultList.getColumnIndex(KEY_WATCHLIST_NAME)), resultList.getString(resultList.getColumnIndex(KEY_WATCHLIST_SYMBOL))));
        }

        resultList.close();
        db.close();

        return currencyList;
    }

    public List<HitBtcManager> getHitBtcAccounts(Context context)
    {
        String searchQuerry = "SELECT * FROM " + TABLE_EXCHANGE_KEYS + " WHERE " + KEY_EXCHANGE_TYPE + " = " + HITBTC_TYPE + " AND " + KEY_EXCHANGE_IS_ENABLED + " = 1";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor resultList = db.rawQuery(searchQuerry, null);

        List<HitBtcManager> accountList = new ArrayList<>();

        while(resultList.moveToNext())
        {
            Exchange exchange = new Exchange(resultList.getInt(resultList.getColumnIndex(KEY_EXCHANGE_ID))
                    , resultList.getString(resultList.getColumnIndex(KEY_EXCHANGE_NAME))
                    , resultList.getInt(resultList.getColumnIndex(KEY_EXCHANGE_TYPE))
                    , resultList.getString(resultList.getColumnIndex(KEY_EXCHANGE_DESCRIPTION))
                    , resultList.getString(resultList.getColumnIndex(KEY_EXCHANGE_PUBLIC_KEY))
                    , resultList.getString(resultList.getColumnIndex(KEY_EXCHANGE_SECRET_KEY))
                    , (resultList.getInt(resultList.getColumnIndex(KEY_EXCHANGE_IS_ENABLED)) == 1));
            accountList.add(new HitBtcManager(context, exchange));
        }

        resultList.close();
        db.close();

        return accountList;
    }

    public List<BinanceManager> getBinanceAccounts()
    {
        String searchQuerry = "SELECT * FROM " + TABLE_EXCHANGE_KEYS + " WHERE " + KEY_EXCHANGE_TYPE + " = " + BINANCE_TYPE + " AND " + KEY_EXCHANGE_IS_ENABLED + " = 1";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor resultList = db.rawQuery(searchQuerry, null);

        List<BinanceManager> accountList = new ArrayList<>();

        while(resultList.moveToNext())
        {
            Exchange exchange = new Exchange(resultList.getInt(resultList.getColumnIndex(KEY_EXCHANGE_ID))
                    , resultList.getString(resultList.getColumnIndex(KEY_EXCHANGE_NAME))
                    , resultList.getInt(resultList.getColumnIndex(KEY_EXCHANGE_TYPE))
                    , resultList.getString(resultList.getColumnIndex(KEY_EXCHANGE_DESCRIPTION))
                    , resultList.getString(resultList.getColumnIndex(KEY_EXCHANGE_PUBLIC_KEY))
                    , resultList.getString(resultList.getColumnIndex(KEY_EXCHANGE_SECRET_KEY))
                    , (resultList.getInt(resultList.getColumnIndex(KEY_EXCHANGE_IS_ENABLED)) == 1));
            accountList.add(new BinanceManager(exchange));
        }

        resultList.close();
        db.close();

        return accountList;
    }

    public List<Currency> getAllCurrenciesFromManualCurrency()
    {
        String searchQuerry = "SELECT * FROM " + TABLE_MANUAL_TRANSACTIONS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor resultatList = db.rawQuery(searchQuerry, null);

        List<Currency> currencyList = new ArrayList<>();

        while(resultatList.moveToNext())
        {
            currencyList.add(new Currency(resultatList.getString(resultatList.getColumnIndex(KEY_TRANSACTION_SYMBOL))
                    , resultatList.getDouble(resultatList.getColumnIndex(KEY_TRANSACTION_AMOUNT)) - resultatList.getDouble(resultatList.getColumnIndex(KEY_TRANSACTION_FEES))));
        }

        resultatList.close();

        db.close();

        return currencyList;
    }

    public void updateTransactionWithId(int transactionId, double amount, Date time, double purchasedPrice, double fees)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_TRANSACTION_AMOUNT, amount);
        cv.put(KEY_TRANSACTION_DATE, time.getTime());
        cv.put(KEY_TRANSACTION_PURCHASE_PRICE, purchasedPrice);
        cv.put(KEY_TRANSACTION_FEES, fees);

        db.update(TABLE_MANUAL_TRANSACTIONS, cv, KEY_TRANSACTION_ID + "=" + transactionId, null);

    }

    public Transaction getCurrencyTransactionById(int id)
    {
        String searchQuerry = "SELECT * FROM " + TABLE_MANUAL_TRANSACTIONS + " WHERE " + KEY_TRANSACTION_ID + "='" + id + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor resultatList = db.rawQuery(searchQuerry, null);

        Transaction transaction = null;

        if(resultatList.moveToFirst())
        {
            transaction = new Transaction(resultatList.getInt(resultatList.getColumnIndex(KEY_TRANSACTION_ID))
                    , resultatList.getString(resultatList.getColumnIndex(KEY_TRANSACTION_SYMBOL))
                    , resultatList.getDouble(resultatList.getColumnIndex(KEY_TRANSACTION_AMOUNT))
                    , resultatList.getLong(resultatList.getColumnIndex(KEY_TRANSACTION_DATE))
                    , resultatList.getDouble(resultatList.getColumnIndex(KEY_TRANSACTION_PURCHASE_PRICE))
                    , resultatList.getDouble(resultatList.getColumnIndex(KEY_TRANSACTION_FEES)));
        }

        resultatList.close();

        db.close();

        return transaction;
    }

    public ArrayList<Transaction> getCurrencyTransactionsForSymbol(String symbol)
    {
        String searchQuerry = "SELECT * FROM " + TABLE_MANUAL_TRANSACTIONS + " WHERE " + KEY_TRANSACTION_SYMBOL + "='" + symbol.toUpperCase() + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor resultatList = db.rawQuery(searchQuerry, null);

        ArrayList<Transaction> transactionList = new ArrayList<>();

        while (resultatList.moveToNext())
        {
            transactionList.add(new Transaction(resultatList.getInt(resultatList.getColumnIndex(KEY_TRANSACTION_ID))
                    , resultatList.getString(resultatList.getColumnIndex(KEY_TRANSACTION_SYMBOL))
                    , resultatList.getDouble(resultatList.getColumnIndex(KEY_TRANSACTION_AMOUNT))
                    , resultatList.getLong(resultatList.getColumnIndex(KEY_TRANSACTION_DATE))
                    , resultatList.getLong(resultatList.getColumnIndex(KEY_TRANSACTION_PURCHASE_PRICE))
                    , resultatList.getDouble(resultatList.getColumnIndex(KEY_TRANSACTION_FEES))));
        }

        resultatList.close();

        db.close();

        return transactionList;
    }

    public void deleteTransactionFromId(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_MANUAL_TRANSACTIONS, KEY_TRANSACTION_ID + "=" + id, null);

        db.close();
    }
}
