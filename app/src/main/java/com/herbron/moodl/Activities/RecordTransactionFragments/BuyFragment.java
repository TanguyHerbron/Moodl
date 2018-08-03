package com.herbron.moodl.Activities.RecordTransactionFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.herbron.moodl.DataNotifiers.CryptocompareNotifierInterface;
import com.herbron.moodl.DataManagers.InfoAPIManagers.CryptocompareApiManager;
import com.herbron.moodl.R;

public class BuyFragment extends Fragment implements CryptocompareNotifierInterface {

    private CryptocompareApiManager cryptocompareApiManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buy, container, false);

        cryptocompareApiManager = CryptocompareApiManager.getInstance(getContext());

        cryptocompareApiManager.addListener(this);

        cryptocompareApiManager.updateExchangeList();

        return view;
    }

    @Override
    public void onDetailsUpdated() {

    }

    @Override
    public void onExchangesUpdated() {
        Log.d("moodl", "Received ! " + cryptocompareApiManager.getExchangeList().get(0).getName());
    }
}
