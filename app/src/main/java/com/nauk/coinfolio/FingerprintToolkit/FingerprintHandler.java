package com.nauk.coinfolio.FingerprintToolkit;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.app.DialogFragment;
import android.widget.Toast;

/**
 * Created by Guitoune on 28/02/2018.
 */

public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private Context context;
    private FingerprintDialogFragment dialogFragment;

    public FingerprintHandler(Context context, FingerprintDialogFragment dialogFragment)
    {
        this.context = context;
        this.dialogFragment = dialogFragment;
    }

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject)
    {
        cancellationSignal = new CancellationSignal();
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString)
    {
        //Toast.makeText(context, "Authentification error\n" + errString, Toast.LENGTH_LONG).show();
        dialogFragment.wrongFingerprint();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogFragment.resetFingerprint();
            }
        }, 500);
    }

    @Override
    public void onAuthenticationFailed()
    {
        //Toast.makeText(context, "Authentification failed", Toast.LENGTH_LONG).show();
        dialogFragment.wrongFingerprint();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogFragment.resetFingerprint();
            }
        }, 500);
    }

    @Override
    public void onAuthenticationHelp(int helpMsgIf, CharSequence helpString)
    {
        Toast.makeText(context, "Authentification help\n" + helpString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result)
    {
        //Toast.makeText(context, "Success !", Toast.LENGTH_LONG).show();
        //dialogFragment.dismiss();

        dialogFragment.correctFingerprint();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogFragment.dismiss();
            }
        }, 250);
    }
}
