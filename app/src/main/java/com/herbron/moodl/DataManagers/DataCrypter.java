package com.herbron.moodl.DataManagers;

import android.content.Context;
import android.util.Log;

import com.herbron.moodl.R;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
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
        try {
            byte[] keyByte = key.getBytes("UTF-8");
            byte[] finalKey = new byte[32];
            System.arraycopy(keyByte, 0, finalKey, 0, keyByte.length);

            aesKey = new SecretKeySpec(finalKey, "AES");
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

            byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));

            encryptedData = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);

        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | UnsupportedEncodingException
                | InvalidAlgorithmParameterException e) {

            Log.d("moodl", "Error while encrypting data " + e.getMessage());

        }

        return encryptedData;
    }

    public static String decrypt(Context context, String data)
    {
        String decryptedData = null;

        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(context.getString(R.string.ivKey).getBytes("UTF-8"));

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, aesKey, ivParameterSpec);
            
            byte[] decryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));

            //byte[] dataBytes = Base64.decode(data, Base64.DEFAULT);
            decryptedData = new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch(NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | UnsupportedEncodingException
                | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        return decryptedData;
    }
}
