package com.herbron.moodl.DataNotifiers;

/**
 * Created by Administrator on 17/06/2018.
 */

public interface BalanceUpdateNotifierInterface {

    void onBalanceDataUpdated();

    void onBalanceError(String error);
}
