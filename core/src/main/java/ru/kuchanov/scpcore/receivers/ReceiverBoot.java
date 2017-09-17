package ru.kuchanov.scpcore.receivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.manager.MyNotificationManager;
import timber.log.Timber;

public class ReceiverBoot extends BroadcastReceiver {

    @Inject
    protected MyNotificationManager mMyNotificationManager;

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Timber.d("onReceive with action: %s", intent.getAction());

        callInjection();
        mMyNotificationManager.checkAlarm();
    }

    protected void callInjection() {
        BaseApplication.getAppComponent().inject(this);
    }
}