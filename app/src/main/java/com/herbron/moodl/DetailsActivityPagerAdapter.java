package com.herbron.moodl;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.herbron.moodl.Activities.DetailsActivityFragments.ChartsFragment;
import com.herbron.moodl.Activities.DetailsActivityFragments.InformationFragment;
import com.herbron.moodl.Activities.DetailsActivityFragments.TransactionsFragment;

/**
 * Created by Tiji on 13/05/2018.
 */

public class DetailsActivityPagerAdapter extends FragmentStatePagerAdapter {

    private int numOfTabs;

    public DetailsActivityPagerAdapter(FragmentManager fm, int numOfTabs)
    {
        super(fm);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                return new ChartsFragment();
            case 1:
                return new InformationFragment();
            case 2:
                return new TransactionsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
