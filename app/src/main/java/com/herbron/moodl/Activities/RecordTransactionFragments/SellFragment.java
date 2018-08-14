package com.herbron.moodl.Activities.RecordTransactionFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.herbron.moodl.CustomLayouts.CustomRecordFragment;
import com.herbron.moodl.R;

public class SellFragment extends CustomRecordFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sell, container, false);
        return view;
    }

    @Override
    public void onCurrencyUpdated() {

    }

    @Override
    public void onExchangeUpdated() {

    }

    @Override
    public void onPairUpdated() {

    }
}
