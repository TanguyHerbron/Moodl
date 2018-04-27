package com.nauk.moodl.DataManagers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nauk.moodl.DataManagers.CurrencyData.Currency;
import com.nauk.moodl.DataManagers.CurrencyData.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Guitoune on 14/01/2018.
 */

public class DatabaseManager extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 6;

    private static final String DATABASE_NAME = "Currencies.db";

    private static final String TABLE_MANUAL_CURRENCIES = "ManualCurrencies";
    private static final String TABLE_EXCHANGE_KEYS = "ExchangeKeys";
    private static final String TABLE_WATCHLIST = "Watchlist";

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
    private static final String KEY_EXCHANGE_PUBLIC_KEY = "publicKey";
    private static final String KEY_EXCHANGE_SECRET_KEY = "secretKey";

    private static final String KEY_WATCHLIST_ID = "idWatchlist";
    private static final String KEY_WATCHLIST_SYMBOL = "symbol";
    private static final String KEY_WATCHLIST_NAME = "name";
    private static final String KEY_WATCHLIST_POSITION = "position";

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
                + KEY_EXCHANGE_PUBLIC_KEY + " TEXT,"
                + KEY_EXCHANGE_SECRET_KEY + " TEXT"
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MANUAL_CURRENCIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXCHANGE_KEYS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WATCHLIST);

        onCreate(db);
    }

    public void addCurrencyToWatchlist(Currency currency)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_WATCHLIST_SYMBOL, currency.getSymbol());
        values.put(KEY_WATCHLIST_NAME, currency.getName());

        db.insert(TABLE_WATCHLIST, null, values);
        db.close();
    }

    public int deleteCurrencyFromWatchlist(String symbol)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(TABLE_WATCHLIST, KEY_WATCHLIST_SYMBOL + " = '" + symbol + "'", null);
    }

    public List<Currency> getAllCurrenciesFromWatchlist()
    {
        String searchQuerry = "SELECT * FROM " + TABLE_WATCHLIST;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor resultatList = db.rawQuery(searchQuerry, null);

        List<Currency> currencyList = new ArrayList<>();

        while(resultatList.moveToNext())
        {
            currencyList.add(new Currency(resultatList.getString(2), resultatList.getString(1)));
        }

        return currencyList;
    }

    public void addCurrencyToManualCurrency(String symbol, double balance, Date date, double purchasedPrice, double fees)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_CURRENCY_SYMBOL, symbol);
        values.put(KEY_CURRENCY_BALANCE, balance);
        values.put(KEY_CURRENCY_DATE, date.getTime());
        values.put(KEY_CURRENCY_PURCHASED_PRICE, purchasedPrice);
        values.put(KEY_CURRENCY_FEES, fees);

        db.insert(TABLE_MANUAL_CURRENCIES, null, values);
        db.close();
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

        while(resultatList.moveToNext())
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
