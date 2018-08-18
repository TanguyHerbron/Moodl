package com.herbron.moodl.Activities.RecordTransactionFragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.herbron.moodl.CustomLayouts.CustomRecordFragment;
import com.herbron.moodl.DataManagers.CurrencyData.Currency;
import com.herbron.moodl.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TransferFragment extends CustomRecordFragment {

    private static Currency fragmentCurrency;

    private Spinner fromSpinner;
    private Spinner toSpinner;

    private TextInputEditText transferDateEditText;
    private SimpleDateFormat sdf;
    private Calendar calendar;

    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.record_transaction_fragment_transfer, container, false);

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat(" HH:mm dd/MM/yyyy", Locale.UK);

        fromSpinner = view.findViewById(R.id.from_transfer_spinner);
        toSpinner = view.findViewById(R.id.to_transfer_spinner);

        ArrayAdapter<String> fromAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.from_transfer_options_string_array));
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> toAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.to_transfer_options_string_array));
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        fromSpinner.setAdapter(fromAdapter);
        toSpinner.setAdapter(toAdapter);

        transferDateEditText = view.findViewById(R.id.transfertDate_editText);
        transferDateEditText.setText(sdf.format(calendar.getTime()));

        transferDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDatePicker();
            }
        });

        return view;
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

    }

    @Override
    public void onPairUpdated() {

    }
}
