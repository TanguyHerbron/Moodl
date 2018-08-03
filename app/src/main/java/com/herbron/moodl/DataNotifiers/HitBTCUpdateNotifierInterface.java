package com.herbron.moodl.DataNotifiers;

public interface HitBTCUpdateNotifierInterface {

    void onHitBTCBalanceUpdateSuccess();

    void onHitBTCBalanceUpdateError(int accountId, String error);

}
