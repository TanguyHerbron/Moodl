package com.herbron.moodl.Activities.RecordTransactionFragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;

import com.herbron.moodl.Activities.HomeActivity;
import com.herbron.moodl.CustomLayouts.CustomRecordFragment;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.DataManagers.CurrencyData.Transaction;
import com.herbron.moodl.DataManagers.DatabaseManager;
import com.herbron.moodl.DataManagers.ExchangeManager.Exchange;
import com.herbron.moodl.DataManagers.PreferencesManager;
import com.herbron.moodl.R;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TransferFragment extends CustomRecordFragment {

    private static Currency fragmentCurrency;
    private static Exchange fragmentExchange;

    private Spinner fromSpinner;
    private Spinner toSpinner;
    private Spinner feeSpinner;

    private Switch deductHoldingsSwitch;

    private Button saveButton;

    private int transactionId;
    private Transaction transaction;

    private TextInputEditText transferDateEditText;
    private TextInputEditText amountEditText;
    private TextInputEditText feesEditText;
    private TextInputEditText noteEditText;

    private SimpleDateFormat sdf;
    private Calendar calendar;

    private View view;

    public static final String EXCHANGE_CODE = "stra:e";
    public static final String WALLET_CODE = "stra:mw";
    public static final String MINING_CODE = "stra:m";
    public static final String ELSE_WALLET_CODE = "stra:smew";
    public static final String AIRDROP_CODE = "stra:a";
    public static final String UNKNOWN_CODE = "stra:unk";
    public static final String FORK_CODE = "stra:fo";

    private View.OnClickListener saveButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isFieldCorrectlyFilled(amountEditText, true))
            {
                if(isTransactionPossible())
                {
                    PreferencesManager preferencesManager = new PreferencesManager(getContext());
                    DatabaseManager databaseManager = new DatabaseManager(getContext());
                    double amount = Double.valueOf(amountEditText.getText().toString());
                    double fees = getFees();

                    if(transactionId == -1)
                    {
                        databaseManager.addTransaction(fragmentCurrency.getSymbol()
                                , amount
                                , calendar.getTime()
                                , 0
                                , fees
                                , noteEditText.getText().toString()
                                , ""
                                , fragmentCurrency.getSymbol()
                                , getDestination()
                                , getSource()
                                , "t"
                                , feeSpinner.getSelectedItemPosition() == 0 ? "p" : "f"
                                , deductHoldingsSwitch.isChecked());

                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                    }
                    else
                    {
                        databaseManager.updateTransactionWithId(transactionId
                                , amount
                                , calendar.getTime()
                                , 0
                                , fees
                                , noteEditText.getText().toString()
                                , ""
                                , fragmentCurrency.getSymbol()
                                , getDestination()
                                , getSource()
                                ,"t"
                                , feeSpinner.getSelectedItemPosition() == 0 ? "p" : "f"
                                , deductHoldingsSwitch.isChecked());
                    }

                    preferencesManager.setMustUpdateSummary(true);

                    getActivity().finish();
                }
                else
                {
                    Drawable backgroundDrawableTo = toSpinner.getBackground();
                    backgroundDrawableTo.mutate();
                    backgroundDrawableTo.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.error), PorterDuff.Mode.SRC_ATOP));
                    backgroundDrawableTo.invalidateSelf();

                    Drawable backgroundDrawableFrom = fromSpinner.getBackground();
                    backgroundDrawableFrom.mutate();
                    backgroundDrawableFrom.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.error), PorterDuff.Mode.SRC_ATOP));
                    backgroundDrawableFrom.invalidateSelf();

                    view.findViewById(R.id.errorLayouts).setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.record_transaction_fragment_transfer, container, false);

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.UK);

        initializeViewElements();

        return view;
    }

    private void initializeViewElements()
    {
        fromSpinner = view.findViewById(R.id.from_transfer_spinner);
        toSpinner = view.findViewById(R.id.to_transfer_spinner);
        feeSpinner = view.findViewById(R.id.feesFormat_editText_transfer);

        setupSpinnesr();

        feesEditText = view.findViewById(R.id.fees_editText_transfer);
        noteEditText = view.findViewById(R.id.note_editText_transfer);
        amountEditText = view.findViewById(R.id.amount_editText_transfer);
        transferDateEditText = view.findViewById(R.id.transfertDate_editText);
        transferDateEditText.setText(sdf.format(calendar.getTime()));

        transferDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDatePicker();
            }
        });

        saveButton = view.findViewById(R.id.saveTransferButton);
        saveButton.setOnClickListener(saveButtonClickListener);

        deductHoldingsSwitch = view.findViewById(R.id.deductHoldingsTransfer);

        checkCallingIntent();
    }

    private void checkCallingIntent()
    {
        Intent intent = getActivity().getIntent();
        transactionId = intent.getIntExtra("transactionId", -1);

        if(transactionId != -1)
        {
            DatabaseManager databaseManager = new DatabaseManager(getContext());
            transaction = databaseManager.getCurrencyTransactionById(transactionId);

            if(transaction.getType() != null && transaction.getType().equals("t"))
            {
                fillFields();
            }
        }
    }

    private void fillFields()
    {
        setupFromSpinner();
        setupToSpinner();
        amountEditText.setText(String.valueOf(transaction.getAmount()));
        calendar.setTimeInMillis(transaction.getTimestamp());
        transferDateEditText.setText(sdf.format(calendar.getTime()));
        feesEditText.setText(String.format(Locale.UK, "%f", transaction.getFees()));
        noteEditText.setText(transaction.getNote());
        feeSpinner.setSelection(transaction.getFeeFormat().equals("p") ? 0 : 1);
    }

    private void setupFromSpinner()
    {
        switch (transaction.getSource())
        {
            case EXCHANGE_CODE:
                fromSpinner.setSelection(0);
                break;
            case WALLET_CODE:
                fromSpinner.setSelection(1);
                break;
            case MINING_CODE:
                fromSpinner.setSelection(2);
                break;
            case ELSE_WALLET_CODE:
                fromSpinner.setSelection(3);
                break;
            case AIRDROP_CODE:
                fromSpinner.setSelection(4);
                break;
            case UNKNOWN_CODE:
                fromSpinner.setSelection(5);
                break;
            case FORK_CODE:
                fromSpinner.setSelection(6);
                break;
        }
    }

    private void setupToSpinner()
    {
        switch (transaction.getDestination())
        {
            case EXCHANGE_CODE:
                toSpinner.setSelection(0);
                break;
            case WALLET_CODE:
                toSpinner.setSelection(1);
                break;
            case ELSE_WALLET_CODE:
                toSpinner.setSelection(2);
                break;
            case UNKNOWN_CODE:
                toSpinner.setSelection(3);
                break;
        }
    }

    private void setupSpinnesr()
    {
        ArrayAdapter<String> fromAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.from_transfer_options_string_array));
        fromAdapter.setDropDownViewResource(R.layout.spinner_dropdown_black);
        fromSpinner.setAdapter(fromAdapter);

        ArrayAdapter<String> toAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.to_transfer_options_string_array));
        toAdapter.setDropDownViewResource(R.layout.spinner_dropdown_black);
        toSpinner.setAdapter(toAdapter);

        ArrayAdapter<String> feeAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.fees_options));
        feeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_black);
        feeSpinner.setAdapter(feeAdapter);

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                Drawable backgroundDrawableFrom = fromSpinner.getBackground();
                backgroundDrawableFrom.mutate();
                backgroundDrawableFrom.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.separationColor), PorterDuff.Mode.SRC_ATOP));
                backgroundDrawableFrom.invalidateSelf();

                view.findViewById(R.id.errorLayouts).setVisibility(View.INVISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                Drawable backgroundDrawableTo = toSpinner.getBackground();
                backgroundDrawableTo.mutate();
                backgroundDrawableTo.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.separationColor), PorterDuff.Mode.SRC_ATOP));
                backgroundDrawableTo.invalidateSelf();

                view.findViewById(R.id.errorLayouts).setVisibility(View.INVISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private double getFees()
    {
        double fees = 0;

        if(!feesEditText.getText().toString().equals(""))
        {
            fees = Double.parseDouble(feesEditText.getText().toString());
        }

        return fees;
    }

    private boolean isTransactionPossible()
    {
        Set<Integer> conflictFrom = new HashSet<>(Arrays.asList(2, 3, 4, 5, 6));
        Set<Integer> conflictTo = new HashSet<>(Arrays.asList(2, 3));

        return !(conflictFrom.contains(fromSpinner.getSelectedItemPosition()) && conflictTo.contains(toSpinner.getSelectedItemPosition()));
    }

    private String getDestination()
    {
        String destination = "";

        switch (toSpinner.getSelectedItemPosition())
        {
            case 0:
                destination = EXCHANGE_CODE;
                break;
            case 1:
                destination = WALLET_CODE;
                break;
            case 2:
                destination = ELSE_WALLET_CODE;
                break;
            case 3:
                destination = UNKNOWN_CODE;
                break;
        }

        return destination;
    }

    private String getSource()
    {
        String source = "";

        switch (fromSpinner.getSelectedItemPosition())
        {
            case 0:
                source = EXCHANGE_CODE;
                break;
            case 1:
                source = WALLET_CODE;
                break;
            case 2:
                source = MINING_CODE;
                break;
            case 3:
                source = ELSE_WALLET_CODE;
                break;
            case 4:
                source = AIRDROP_CODE;
                break;
            case 5:
                source = UNKNOWN_CODE;
                break;
            case 6:
                source = FORK_CODE;
                break;
        }

        return source;
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
                        transferDateEditText.setText(sdf.format(calendar.getTime()));
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
                        transferDateEditText.setText(sdf.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show();
    }

    @Override
    public void onCurrencyUpdated() {
        fragmentCurrency = currency;
    }

    @Override
    public void onExchangeUpdated() {
        fragmentExchange = exchange;
    }

    @Override
    public void onPairUpdated() {

    }
}
