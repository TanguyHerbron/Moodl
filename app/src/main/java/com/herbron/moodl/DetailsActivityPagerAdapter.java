package com.herbron.moodl;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.herbron.moodl.Activities.DetailsActivityFragments.Charts;
import com.herbron.moodl.Activities.DetailsActivityFragments.Informations;
import com.herbron.moodl.Activities.DetailsActivityFragments.Transactions;

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
                return new Charts();
            case 1:
                return new Informations();
            case 2:
                return new Transactions();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
