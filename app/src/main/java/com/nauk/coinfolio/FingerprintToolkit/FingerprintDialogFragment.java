package com.nauk.coinfolio.FingerprintToolkit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nauk.coinfolio.R;

/**
 * Created by Guitoune on 28/02/2018.
 */

public class FingerprintDialogFragment extends DialogFragment{

    public static FingerprintDialogFragment newInstance(int title)
    {
        FingerprintDialogFragment frag = new FingerprintDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_fingerprint_scanner, container);

        return view;
    }
}
