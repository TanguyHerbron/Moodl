package com.herbron.moodl;

/**
 * Created by Administrator on 17/06/2018.
 */

public interface DataNotifierInterface {

    void onTickerListUpdated();

    void onDetailsUpdated();

    void onBalanceDataUpdated();

    void onBalanceError(String error);
}
