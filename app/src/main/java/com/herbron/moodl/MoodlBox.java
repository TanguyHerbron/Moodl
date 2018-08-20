package com.herbron.moodl;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.herbron.moodl.DataManagers.InfoAPIManagers.CryptocompareApiManager;
import com.herbron.moodl.DataNotifiers.MoodlboxNotifierInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static java.lang.Math.abs;

/**
 * Created by Guitoune on 30/04/2018.
 */

public class MoodlBox {

    public static void expandH(final View v) {
        v.measure(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);

        Animation a = getHorizontalExpandAnimation(v);

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static Animation getHorizontalExpandAnimation(final View v)
    {
        final int targetHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? CardView.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        return a;
    }

    public static void expandW(final View v, Animation a)
    {
        v.measure(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        int targetWidth = v.getMeasuredWidth();

        v.getLayoutParams().width = 1;
        v.setVisibility(View.VISIBLE);

        a.setDuration((int)(targetWidth / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void expandW(final View v) {
        expandW(v, getVerticalExpandAnimation(v));
    }

    public static Animation getVerticalExpandAnimation(final View v)
    {
        final int targetWidth = v.getMeasuredWidth();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().width = interpolatedTime == 1
                        ? CardView.LayoutParams.WRAP_CONTENT
                        : (int)(targetWidth * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        return a;
    }

    public static void collapseH(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapseW(final View v) {
        final int initialWidth = v.getMeasuredWidth();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().width = initialWidth - (int)(initialWidth * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int)(initialWidth / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static  String numberConformer(double number)
    {
        String str;

        if(abs(number) > 1)
        {
            str = String.format( Locale.UK, "%.2f", number).replaceAll("\\.?0*$", "");
        }
        else
        {
            str = String.format( Locale.UK, "%.4f", number).replaceAll("\\.?0*$", "");
        }

        if(!str.equals("Infinity"))
        {
            int counter = 0;
            int i = str.indexOf(".");
            if(i <= 0)
            {
                i = str.length();
            }
            for(i -= 1; i > 0; i--)
            {
                counter++;
                if(counter == 3)
                {
                    str = str.substring(0, i) + " " + str.substring(i, str.length());
                    counter = 0;
                }
            }
        }

        return str;
    }

    public static String getDateFromTimestamp(long timeStamp){
        try{
            SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm dd/MM/yyyy", Locale.getDefault());
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }

    public static void getBitmapFromURL(String src, String symbol, Resources resources, Context context, MoodlboxNotifierInterface callBack)
    {
        String size = src.substring(src.lastIndexOf("=") + 1, src.length());
        String filepath = context.getCacheDir() + "/" + symbol + "x" + size + ".png";
        Bitmap result;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        result = BitmapFactory.decodeFile(filepath, options);

        if(result == null)
        {
            try {
                java.net.URL url = new java.net.URL(src);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                result = BitmapFactory.decodeStream(input);

                FileOutputStream out = new FileOutputStream(filepath);
                result.compress(Bitmap.CompressFormat.PNG, 100, out);

            } catch (IOException e) {
                Log.d("moodl", "Error while downloading " + symbol + " icon > " + e.getMessage());
                result = BitmapFactory.decodeResource(resources,
                        R.mipmap.ic_launcher_moodl);
                result = Bitmap.createScaledBitmap(result, Integer.valueOf(size), Integer.valueOf(size), false);
            }
        }

        callBack.onBitmapDownloaded(result);
    }

    public static int getColor(int id, Context context)
    {
        int color;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            color = context.getColor(id);
        }
        else
        {
            color = context.getResources().getColor(id);
        }

        return color;
    }

    public static Drawable getDrawable(int id, Context context)
    {
        Drawable drawable;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            drawable = context.getDrawable(id);
        }
        else
        {
            drawable = context.getResources().getDrawable(id);
        }

        return drawable;
    }

    public static String getIconUrl(String symbol, CryptocompareApiManager cryptocompareApiManager)
    {
        return getIconUrl(symbol, 50, cryptocompareApiManager);
    }

    public static float convertDpToPx(float dp, Resources resources)
    {
        return dp * resources.getDisplayMetrics().density;
    }

    public static String getIconUrl(String symbol, int size, CryptocompareApiManager cryptocompareApiManager)
    {
        String url;

        try {
            JSONObject jsonObject = new JSONObject(cryptocompareApiManager.getCoinInfosHashmap().get(symbol));
            url = "https://www.cryptocompare.com" + jsonObject.getString("ImageUrl") + "?width=" + size;
        } catch (JSONException | NullPointerException e) {
            url = null;
        }

        return url;
    }
}
