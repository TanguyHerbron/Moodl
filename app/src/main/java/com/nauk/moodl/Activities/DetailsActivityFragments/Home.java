package com.nauk.moodl.Activities.DetailsActivityFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nauk.moodl.DataManagers.CurrencyData.Currency;
import com.nauk.moodl.R;

/**
 * Created by Tiji on 13/05/2018.
 */

public class Home extends Fragment {

    private View view;
    private Currency currency;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_home_detailsactivity, container, false);

        currency = getActivity().getIntent().getParcelableExtra("currency");

        return view;
    }
}
