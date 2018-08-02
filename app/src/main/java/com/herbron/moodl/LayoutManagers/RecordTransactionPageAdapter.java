package com.herbron.moodl.LayoutManagers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.herbron.moodl.Activities.RecordTransactionFragments.BuyFragment;
import com.herbron.moodl.Activities.RecordTransactionFragments.SellFragment;
import com.herbron.moodl.Activities.RecordTransactionFragments.TransferFragment;

public class RecordTransactionPageAdapter extends FragmentStatePagerAdapter {

    private int tabsNumber;

    public RecordTransactionPageAdapter(FragmentManager fm, int tabsNumber) {
        super(fm);

        this.tabsNumber = tabsNumber;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position)
        {
            case 0:
                BuyFragment buyFragment = new BuyFragment();
                return buyFragment;
            case 1:
                SellFragment sellFragment = new SellFragment();
                return sellFragment;
            case 2:
                TransferFragment transferFragment = new TransferFragment();
                return transferFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabsNumber;
    }
}
