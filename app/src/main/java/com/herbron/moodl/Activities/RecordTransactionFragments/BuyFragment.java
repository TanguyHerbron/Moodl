package com.herbron.moodl.Activities.RecordTransactionFragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.herbron.moodl.Activities.HomeActivity;
import com.herbron.moodl.Activities.RecordTransactionActivity;
import com.herbron.moodl.CustomLayouts.CustomRecordFragment;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.CurrencyData.Transaction;
import com.herbron.moodl.DataManagers.DatabaseManager;
import com.herbron.moodl.DataManagers.ExchangeManager.Exchange;
import com.herbron.moodl.DataManagers.InfoAPIManagers.Pair;
import com.herbron.moodl.DataManagers.PreferencesManager;
import com.herbron.moodl.PlaceholderManager;
import com.herbron.moodl.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BuyFragment extends CustomRecordFragment {

    private TextInputEditText amoutEditText;
    private TextInputEditText buyPriceEditText;
    private TextInputEditText buyDateEditText;
    private TextInputEditText totalValueEditText;
    private TextInputEditText fees_editText;
    private TextInputEditText note_editText;
    private AppCompatButton saveBuyButton;
    private static Spinner feesCurrencySpinner;
    private static View view;

    private ArrayAdapter<String> currencyAdapter;

    private SimpleDateFormat sdf;
    private Calendar calendar;
    private PreferencesManager preferenceManager;

    private static Context context;

    private static Currency fragmentCurrency;
    private static Exchange fragmentExchange;
    private static Pair fragmentPair;
    private List<String> symbolStrings;

    private int transactionId;
    private Transaction transaction;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_buy, container, false);

        context = getActivity().getApplicationContext();

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat(" HH:mm dd/MM/yyyy", Locale.UK);

        preferenceManager = new PreferencesManager(getContext());

        initializeViewElements();

        return view;
    }

    private void checkCallingIntent()
    {
        Intent intent = getActivity().getIntent();
        transactionId = intent.getIntExtra("transactionId", -1);

        if(transactionId != -1)
        {
            DatabaseManager databaseManager = new DatabaseManager(context);
            transaction = databaseManager.getCurrencyTransactionById(transactionId);

            if(transaction.getType().equals("b"))
            {
                fillFields();
            }
        }
    }

    private void fillFields()
    {
        amoutEditText.setText(String.valueOf(transaction.getAmount()));
        buyPriceEditText.setText(String.valueOf(transaction.getPurchasePrice()));
        calendar.setTimeInMillis(transaction.getTimestamp());
        buyDateEditText.setText(sdf.format(calendar.getTime()));
        totalValueEditText.setText(String.valueOf(transaction.getAmount() * transaction.getPurchasePrice()));
        fees_editText.setText(String.valueOf(transaction.getFees()));
        note_editText.setText(transaction.getNote());

        Log.d("moodl", "> " + fragmentPair);

        /*if(transaction.getSymbol().equals(fragmentPair.getFrom()))
        {
            if(transaction.getFeeFormat().equals("p"))
            {
                feesCurrencySpinner.setSelection(0);
            }
            else
            {
                feesCurrencySpinner.setSelection(1);
            }
        }
        else
        {
            if(transaction.getFeeFormat().equals("p"))
            {
                feesCurrencySpinner.setSelection(2);
            }
            else
            {
                feesCurrencySpinner.setSelection(3);
            }
        }*/
    }

    private void initializeViewElements()
    {
        totalValueEditText = view.findViewById(R.id.totalValue_editText);
        amoutEditText = view.findViewById(R.id.amount_editText);
        amoutEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(isFieldCorrectlyFilled(buyPriceEditText, false) && isFieldCorrectlyFilled(amoutEditText, false))
                {
                    if(Double.parseDouble(amoutEditText.getText().toString()) > 0)
                    {
                        Double totalValue = Double.parseDouble(buyPriceEditText.getText().toString()) * Double.parseDouble(s.toString());
                        totalValueEditText.setText(String.format("%f", totalValue));
                    }
                    else
                    {
                        totalValueEditText.setText("0");
                    }
                }
                else
                {
                    totalValueEditText.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        buyPriceEditText = view.findViewById(R.id.buyPrice_editText);
        buyDateEditText = view.findViewById(R.id.buyDate_editText);
        buyDateEditText.setText(sdf.format(calendar.getTime()));

        buyDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDatePicker();
            }
        });
        feesCurrencySpinner = view.findViewById(R.id.feesCurrency_editText);

        currencyAdapter = new ArrayAdapter<String>(getSecureContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feesCurrencySpinner.setAdapter(currencyAdapter);

        if(fragmentPair != null)
        {
            updateAdapter();
        }

        saveBuyButton = view.findViewById(R.id.saveBuyButton);
        saveBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFieldCorrectlyFilled(amoutEditText, true) && isFieldCorrectlyFilled(buyPriceEditText, true) && isFieldCorrectlyFilled(totalValueEditText, true))
                {
                    double amount = Double.parseDouble(amoutEditText.getText().toString());
                    double purchasePrice = Double.parseDouble(buyPriceEditText.getText().toString());
                    double fees;
                    String feeCurrency;

                    if(feesCurrencySpinner.getSelectedItemPosition() < 1)
                    {
                        feeCurrency = fragmentPair.getFrom();
                    }
                    else
                    {
                        feeCurrency = fragmentPair.getTo();
                    }

                    fees = getFees(feeCurrency, amount, purchasePrice);

                    String note = note_editText.getText().toString();

                    DatabaseManager databaseManager = new DatabaseManager(getContext());

                    preferenceManager.setMustUpdateSummary(true);

                    if(transactionId == -1)
                    {
                        databaseManager.addTransaction(fragmentCurrency.getSymbol()
                                , amount
                                , calendar.getTime()
                                , purchasePrice
                                , fees
                                , note
                                , fragmentPair.getFrom().equals(fragmentCurrency.getSymbol()) ? fragmentPair.getTo() : fragmentPair.getFrom()
                                , feeCurrency
                                , fragmentExchange.getName()
                                , "b"
                                , feesCurrencySpinner.getSelectedItemPosition() % 2 == 0 ? "p" : "f");

                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                    }
                    else
                    {
                        databaseManager.updateTransactionWithId(transactionId
                                , amount
                                , calendar.getTime()
                                , purchasePrice
                                , fees
                                , note
                                , fragmentPair.getFrom().equals(fragmentCurrency.getSymbol()) ? fragmentPair.getTo() : fragmentPair.getFrom()
                                , feeCurrency
                                , fragmentExchange.getName()
                                , "b"
                                , feesCurrencySpinner.getSelectedItemPosition() % 2 == 0 ? "p" : "f");
                    }

                    getActivity().finish();
                }
            }
        });

        fees_editText = view.findViewById(R.id.fees_editText);
        note_editText = view.findViewById(R.id.note_editText);

        checkCallingIntent();
    }

    private double getFees(String feeCurrency, double amount, double purchasedPrice)
    {
        double fees;

        if(fees_editText.getText().toString().equals(""))
        {
            fees = 0;
        }
        else
        {
            fees = Double.parseDouble(fees_editText.getText().toString());

            if(feesCurrencySpinner.getSelectedItemPosition() % 2 == 0)
            {
                if(fragmentCurrency.getSymbol().equals(feeCurrency))
                {
                    fees = amount * fees / 100;
                }
                else
                {
                    fees = purchasedPrice * fees / 100;
                }
            }
        }

        return fees;
    }

    private boolean isFieldCorrectlyFilled(TextInputEditText editText, boolean displayError)
    {
        String purchasedPriceText = editText.getText().toString();
        double purchasedPrice;

        try {
            purchasedPrice = Double.parseDouble(purchasedPriceText);

            if(purchasedPrice < 0)
            {
                if(displayError) editText.setError(getResources().getString(R.string.field_negative));

                return false;
            }
        } catch (NumberFormatException e) {
            if(displayError) editText.setError(getResources().getString(R.string.field_nan));

            return false;
        }

        if(purchasedPriceText.equals(""))
        {
            if(displayError) editText.setError(getResources().getString(R.string.field_empty));

            return false;
        }

        return true;
    }

    private void updateAdapter()
    {
        symbolStrings = new ArrayList<>();
        symbolStrings.addAll(PlaceholderManager.getFeeOptionsForSymbol(fragmentPair.getFrom(), getSecureContext()));
        symbolStrings.addAll(PlaceholderManager.getFeeOptionsForSymbol(fragmentPair.getTo(), getSecureContext()));

        currencyAdapter.clear();
        currencyAdapter.addAll(symbolStrings);
        currencyAdapter.notifyDataSetChanged();
    }

    private void createDatePicker()
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        buyDateEditText.setText(sdf.format(calendar.getTime()));
                        createTimePicker();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
        datePickerDialog.show();
    }

    private void createTimePicker()
    {
        new android.app.TimePickerDialog(
                getContext(),
                new android.app.TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hour);
                        calendar.set(Calendar.MINUTE, minute);
                        buyDateEditText.setText(sdf.format(calendar.getTime()));

                        if(fragmentCurrency != null)
                        {
                            fragmentCurrency.getTimestampPrice(getContext(), preferenceManager.getDefaultCurrency(),calendar.getTimeInMillis() / 1000);
                        }
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show();
    }

    private static Context getSecureContext()
    {
        return context;
    }

    @Override
    public void onCurrencyUpdated() {
        fragmentCurrency = currency;
    }

    @Override
    public void onExchangeUpdated() {
        fragmentExchange = exchange;
    }

    public void updatePair(Pair pair)
    {
        currencyAdapter = new ArrayAdapter<String>(getSecureContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feesCurrencySpinner.setAdapter(currencyAdapter);

        symbolStrings = new ArrayList<>();
        symbolStrings.addAll(PlaceholderManager.getFeeOptionsForSymbol(pair.getFrom(), getSecureContext()));
        symbolStrings.addAll(PlaceholderManager.getFeeOptionsForSymbol(pair.getTo(), getSecureContext()));
        currencyAdapter.addAll(symbolStrings);
        currencyAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPairUpdated() {
        fragmentPair = pair;

        fragmentCurrency.setOnTimestampPriceUpdatedListener(new Currency.OnTimestampPriceUpdatedListener() {
            @Override
            public void onTimeStampPriceUpdated(String price) {
                ((TextInputEditText) view.findViewById(R.id.buyPrice_editText)).setText(price);

                if(currencyAdapter != null)
                {
                    updateAdapter();
                }
            }
        });

        if(preferenceManager == null)
        {
            preferenceManager = new PreferencesManager(getSecureContext());
        }

        if(calendar == null)
        {
            calendar = Calendar.getInstance();
        }

        fragmentCurrency.getTimestampPrice(getSecureContext(), fragmentCurrency.getSymbol().equals(fragmentPair.getFrom()) ? fragmentPair.getTo() : fragmentPair.getFrom(), calendar.getTimeInMillis() / 1000);
    }

}
