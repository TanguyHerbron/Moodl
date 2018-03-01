package com.nauk.coinfolio.FingerprintToolkit;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;

import com.mattprecious.swirl.SwirlView;
import com.nauk.coinfolio.Activities.SettingsActivity;
import com.nauk.coinfolio.R;

/**
 * Created by Guitoune on 28/02/2018.
 */

public class FingerprintDialogFragment extends DialogFragment{

    public static FingerprintDialogFragment newInstance()
    {
        FingerprintDialogFragment frag = new FingerprintDialogFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_fingerprint_scanner, container);

        ((Button) view.findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        ((SwirlView) view.findViewById(R.id.swirlBackground)).setState(SwirlView.State.ON, false);

        return view;
    }

    public void correctFingerprint()
    {
        SwirlView swirlView = this.getView().findViewById(R.id.swirl);

        swirlView.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        swirlView.setState(SwirlView.State.ON);
    }

    public void wrongFingerprint()
    {
        ((SwirlView) this.getView().findViewById(R.id.swirl)).clearColorFilter();
        ((SwirlView) this.getView().findViewById(R.id.swirlBackground)).clearColorFilter();
        ((SwirlView) this.getView().findViewById(R.id.swirl)).setState(SwirlView.State.ERROR);
        ((SwirlView) this.getView().findViewById(R.id.swirlBackground)).setState(SwirlView.State.ERROR);
    }

    public void resetFingerprint()
    {
        ((SwirlView) this.getView().findViewById(R.id.swirlBackground)).setState(SwirlView.State.ON);
        SwirlView swirlView = this.getView().findViewById(R.id.swirl);

        swirlView.clearColorFilter();

        swirlView.setState(SwirlView.State.OFF);
    }
}
