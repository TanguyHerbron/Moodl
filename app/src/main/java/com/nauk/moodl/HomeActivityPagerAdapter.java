package com.nauk.moodl;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nauk.moodl.Activities.HomeActivityFragments.MarketCapitalization;
import com.nauk.moodl.Activities.HomeActivityFragments.Summary;
import com.nauk.moodl.Activities.HomeActivityFragments.Watchlist;

/**
 * Created by Tiji on 13/04/2018.
 */

public class HomeActivityPagerAdapter extends FragmentStatePagerAdapter {

    private int numOfTabs;

    public HomeActivityPagerAdapter(FragmentManager fm, int numOfTabs)
    {
        super(fm);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                return new Watchlist();
            case 1:
                return new Summary();
            case 2:
                return new MarketCapitalization();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}