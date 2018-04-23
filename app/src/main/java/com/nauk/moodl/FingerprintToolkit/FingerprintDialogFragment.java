package com.nauk.moodl.FingerprintToolkit;

import android.app.DialogFragment;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mattprecious.swirl.SwirlView;
import com.nauk.moodl.R;

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

        view.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
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

    public void wrongFingerprint(String errorString)
    {
        ((SwirlView) this.getView().findViewById(R.id.swirl)).clearColorFilter();
        ((SwirlView) this.getView().findViewById(R.id.swirlBackground)).clearColorFilter();
        ((SwirlView) this.getView().findViewById(R.id.swirl)).setState(SwirlView.State.ERROR);
        ((SwirlView) this.getView().findViewById(R.id.swirlBackground)).setState(SwirlView.State.ERROR);

        ((TextView) this.getView().findViewById(R.id.fingerprint_error)).setText(errorString);
    }

    public void resetFingerprint()
    {
        ((SwirlView) this.getView().findViewById(R.id.swirlBackground)).setState(SwirlView.State.ON);
        ((TextView) this.getView().findViewById(R.id.fingerprint_error)).setText("");
        SwirlView swirlView = this.getView().findViewById(R.id.swirl);

        swirlView.clearColorFilter();

        swirlView.setState(SwirlView.State.OFF);
    }
}
