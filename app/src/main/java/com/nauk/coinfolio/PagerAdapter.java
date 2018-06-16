package com.nauk.coinfolio;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nauk.coinfolio.Activities.HomeActivityFragments.MarketCapitalization;
import com.nauk.coinfolio.Activities.HomeActivityFragments.Summary;
import com.nauk.coinfolio.Activities.HomeActivityFragments.Watchlist;

/**
 * Created by Tiji on 13/04/2018.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {

    private int numOfTabs;

    public PagerAdapter(FragmentManager fm, int numOfTabs)
    {
        super(fm);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                Watchlist watchlist = new Watchlist();
                return watchlist;
            case 1:
                Summary summary = new Summary();
                return summary;
            case 2:
                MarketCapitalization marketCapitalization = new MarketCapitalization();
                return marketCapitalization;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
