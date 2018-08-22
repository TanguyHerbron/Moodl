package com.herbron.moodl.Utils;

import android.content.Context;

import com.herbron.moodl.Activities.RecordTransactionFragments.TransferFragment;
import com.herbron.moodl.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TransferUtils {

    public static boolean isBalanceRelated(String str)
    {
        Set<String> set = new HashSet<>(Arrays.asList(TransferFragment.EXCHANGE_CODE, TransferFragment.WALLET_CODE));
        return set.contains(str);
    }

    public static String getLabelFor(Context context, String str)
    {
        switch (str)
        {
            case "stra:e":
                return context.getResources().getStringArray(R.array.from_transfer_options_string_array)[0].toLowerCase();
            case "stra:mw":
                return context.getResources().getStringArray(R.array.from_transfer_options_string_array)[1].toLowerCase();
            case "stra:m":
                return context.getResources().getStringArray(R.array.from_transfer_options_string_array)[2].toLowerCase();
            case "stra:smew":
                return context.getResources().getStringArray(R.array.from_transfer_options_string_array)[3].toLowerCase();
            case "stra:a":
                return context.getResources().getStringArray(R.array.from_transfer_options_string_array)[4].toLowerCase();
            case "stra:unk":
                return context.getResources().getStringArray(R.array.from_transfer_options_string_array)[5].toLowerCase();
            case "stra:fo":
                return context.getResources().getStringArray(R.array.from_transfer_options_string_array)[6].toLowerCase();
        }

        return null;
    }
}
