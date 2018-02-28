package com.nauk.coinfolio.FingerprintHelper;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;

/**
 * Created by Guitoune on 28/02/2018.
 */

public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private Context context;

    public FingerprintHandler(Context context)
    {
        this.context = context;
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
}
