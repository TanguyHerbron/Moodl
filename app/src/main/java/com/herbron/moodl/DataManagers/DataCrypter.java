package com.herbron.moodl.DataManagers;

import android.content.Context;
import android.util.Log;

import com.herbron.moodl.R;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DataCrypter {

    private static Key aesKey;

    public static void updateKey(String key)
    {
        for(int i = 0; key.getBytes().length < 32; i++)
        {
            key += "0";
        }

        try {
            aesKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        } catch (UnsupportedEncodingException e) {
            Log.d("moodl", "Error while creating encryption key " + e.getMessage());
        }
    }

    public static String encrypt(Context context, String data)
    {
        String encryptedData = null;

        try {

            IvParameterSpec ivParameterSpec = new IvParameterSpec(context.getString(R.string.ivKey).getBytes("UTF-8"));

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivParameterSpec);

            byte[] encryptedBytes = cipher.doFinal(data.getBytes());

            encryptedData = Base64.encodeBase64String(encryptedBytes);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | UnsupportedEncodingException
                | InvalidAlgorithmParameterException e) {

            Log.d("moodl", "Error while encrypting data " + e.getMessage());

        }

        return encryptedData;
    }
}
