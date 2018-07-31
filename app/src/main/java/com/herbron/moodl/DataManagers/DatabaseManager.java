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
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Guitoune on 14/01/2018.
 */

public class DatabaseManager extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 10;

    private static final String DATABASE_NAME = "Currencies.db";

    public static final String TABLE_MANUAL_CURRENCIES = "ManualCurrencies";
    public static final String TABLE_EXCHANGE_KEYS = "ExchangeKeys";
    public static final String TABLE_WATCHLIST = "Watchlist";

    private static final String KEY_CURRENCY_ID = "idCurrency";
    private static final String KEY_CURRENCY_SYMBOL = "symbol";
    private static final String KEY_CURRENCY_NAME = "name";
    private static final String KEY_CURRENCY_BALANCE = "balance";
    private static final String KEY_CURRENCY_DATE = "addDate";
    private static final String KEY_CURRENCY_PURCHASED_PRICE = "purchasedPrice";
    private static final String KEY_CURRENCY_IS_MINED = "isMined";
    private static final String KEY_CURRENCY_FEES = "fees";

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
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MANUAL_CURRENCIES + "("
                + KEY_CURRENCY_ID + " INTEGER PRIMARY KEY,"
                + KEY_CURRENCY_SYMBOL + " VARCHAR(4),"
                + KEY_CURRENCY_NAME + " VARCHAR(45),"
                + KEY_CURRENCY_BALANCE + " TEXT,"
                + KEY_CURRENCY_DATE + " TEXT,"
                + KEY_CURRENCY_PURCHASED_PRICE + " REAL,"
                + KEY_CURRENCY_IS_MINED + " INTEGER,"
                + KEY_CURRENCY_FEES + " REAL"
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
        switch (oldVersion)
        {
            case 6:
                db.execSQL("ALTER TABLE " + TABLE_EXCHANGE_KEYS
                        + "  ADD " + KEY_EXCHANGE_DESCRIPTION + " TEXT");
            case 7:
                db.execSQL("ALTER TABLE " + TABLE_EXCHANGE_KEYS
                        + " ADD " + KEY_EXCHANGE_TYPE + " INTEGER");
            case 8:
                db.execSQL("DROP TABLE " + TABLE_EXCHANGE_KEYS);
                onCreate(db);
            case 9:
                db.execSQL("ALTER TABLE " + TABLE_EXCHANGE_KEYS
                        + " ADD " + KEY_EXCHANGE_IS_ENABLED + " INTEGER");
        }
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

        return result.getInt(0);
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
            selectedExchange = new Exchange(result.getInt(0), result.getString(1)
                    , result.getInt(2), result.getString(3)
                    , result.getString(4), result.getString(5)
                    , (result.getInt(6) == 1));
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
            exchanges.add(new Exchange(result.getInt(0), result.getString(1)
                    , result.getInt(2), result.getString(3)
                    , result.getString(4), result.getString(5)
                    , (result.getInt(6) == 1)));
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
                values.put(KEY_CURRENCY_SYMBOL, DataCrypter.decrypt(context, rawValues.getString(KEY_CURRENCY_SYMBOL)));
                values.put(KEY_CURRENCY_NAME, DataCrypter.decrypt(context, rawValues.getString(KEY_CURRENCY_NAME)));
                values.put(KEY_CURRENCY_BALANCE, DataCrypter.decrypt(context, rawValues.getString(KEY_CURRENCY_BALANCE)));
                values.put(KEY_CURRENCY_DATE, DataCrypter.decrypt(context, rawValues.getString(KEY_CURRENCY_DATE)));
                values.put(KEY_CURRENCY_PURCHASED_PRICE, DataCrypter.decrypt(context, rawValues.getString(KEY_CURRENCY_PURCHASED_PRICE)));
                values.put(KEY_CURRENCY_IS_MINED, DataCrypter.decrypt(context, rawValues.getString(KEY_CURRENCY_IS_MINED)));
                values.put(KEY_CURRENCY_FEES, DataCrypter.decrypt(context, rawValues.getString(KEY_CURRENCY_FEES)));
            }
            else
            {
                values.put(KEY_CURRENCY_SYMBOL, rawValues.getString(KEY_CURRENCY_SYMBOL));
                values.put(KEY_CURRENCY_NAME, rawValues.getString(KEY_CURRENCY_NAME));
                values.put(KEY_CURRENCY_BALANCE, rawValues.getString(KEY_CURRENCY_BALANCE));
                values.put(KEY_CURRENCY_DATE, rawValues.getString(KEY_CURRENCY_DATE));
                values.put(KEY_CURRENCY_PURCHASED_PRICE, rawValues.getString(KEY_CURRENCY_PURCHASED_PRICE));
                values.put(KEY_CURRENCY_IS_MINED, rawValues.getString(KEY_CURRENCY_IS_MINED));
                values.put(KEY_CURRENCY_FEES, rawValues.getString(KEY_CURRENCY_FEES));
            }
        } catch (JSONException e) {
            Log.d("moodl", "Error while inserting transaction " + e.getMessage());
        }

        db.insert(TABLE_MANUAL_CURRENCIES, null, values);
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
            currencyList.add(new Currency(resultList.getString(2), resultList.getString(1)));
        }

        resultList.close();
        db.close();

        return currencyList;
    }

    public List<HitBtcManager> getHitBtcAccounts(Context context)
    {
        String searchQuerry = "SELECT * FROM " + TABLE_EXCHANGE_KEYS + " WHERE " + KEY_EXCHANGE_TYPE + "='" + HITBTC_TYPE + "' AND " + KEY_EXCHANGE_IS_ENABLED + " = '1'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor resultList = db.rawQuery(searchQuerry, null);

        List<HitBtcManager> accountList = new ArrayList<>();

        while(resultList.moveToNext())
        {
            Exchange exchange = new Exchange(resultList.getInt(0), resultList.getString(1)
                    , resultList.getInt(2), resultList.getString(3)
                    , resultList.getString(4), resultList.getString(5)
                    , (resultList.getInt(6) == 1));
            accountList.add(new HitBtcManager(context, exchange));
        }

        resultList.close();
        db.close();

        return accountList;
    }

    public List<BinanceManager> getBinanceAccounts()
    {
        String searchQuerry = "SELECT * FROM " + TABLE_EXCHANGE_KEYS + " WHERE " + KEY_EXCHANGE_TYPE + "='" + BINANCE_TYPE + "' AND " + KEY_EXCHANGE_IS_ENABLED + " = '1'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor resultList = db.rawQuery(searchQuerry, null);

        List<BinanceManager> accountList = new ArrayList<>();

        while(resultList.moveToNext())
        {
            Exchange exchange = new Exchange(resultList.getInt(0), resultList.getString(1)
                    , resultList.getInt(2), resultList.getString(3)
                    , resultList.getString(4), resultList.getString(5)
                    , (resultList.getInt(6) == 1));
            accountList.add(new BinanceManager(exchange));
        }

        resultList.close();
        db.close();

        return accountList;
    }

    public List<Currency> getAllCurrenciesFromManualCurrency()
    {
        String searchQuerry = "SELECT * FROM " + TABLE_MANUAL_CURRENCIES;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor resultatList = db.rawQuery(searchQuerry, null);

        List<Currency> currencyList = new ArrayList<>();

        while(resultatList.moveToNext())
        {
            currencyList.add(new Currency(resultatList.getString(1), resultatList.getDouble(3) - resultatList.getDouble(7)));
        }

        resultatList.close();

        db.close();

        return currencyList;
    }

    public void updateTransactionWithId(int transactionId, double amount, Date time, double purchasedPrice, double fees)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_CURRENCY_BALANCE, amount);
        cv.put(KEY_CURRENCY_DATE, time.getTime());
        cv.put(KEY_CURRENCY_PURCHASED_PRICE, purchasedPrice);
        cv.put(KEY_CURRENCY_FEES, fees);

        db.update(TABLE_MANUAL_CURRENCIES, cv, KEY_CURRENCY_ID + "=" + transactionId, null);

    }

    public Transaction getCurrencyTransactionById(int id)
    {
        String searchQuerry = "SELECT * FROM " + TABLE_MANUAL_CURRENCIES + " WHERE " + KEY_CURRENCY_ID + "='" + id + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor resultatList = db.rawQuery(searchQuerry, null);

        Transaction transaction = null;

        if(resultatList.moveToFirst())
        {
            transaction = new Transaction(resultatList.getInt(0), resultatList.getString(1), resultatList.getDouble(3), resultatList.getLong(4), resultatList.getLong(5), resultatList.getDouble(7));
        }

        resultatList.close();

        db.close();

        return transaction;
    }

    public ArrayList<Transaction> getCurrencyTransactionsForSymbol(String symbol)
    {
        String searchQuerry = "SELECT * FROM " + TABLE_MANUAL_CURRENCIES + " WHERE " + KEY_CURRENCY_SYMBOL + "='" + symbol.toUpperCase() + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor resultatList = db.rawQuery(searchQuerry, null);

        ArrayList<Transaction> transactionList = new ArrayList<>();

        while (resultatList.moveToNext())
        {
            transactionList.add(new Transaction(resultatList.getInt(0), resultatList.getString(1), resultatList.getDouble(3), resultatList.getLong(4), resultatList.getLong(5), resultatList.getDouble(7)));
        }

        resultatList.close();

        db.close();

        return transactionList;
    }

    public void deleteTransactionFromId(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_MANUAL_CURRENCIES, KEY_CURRENCY_ID + "=" + id, null);

        db.close();
    }
}
