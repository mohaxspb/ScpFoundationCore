package ru.kuchanov.scpcore.service;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import timber.log.Timber;

/**
 * Created by mohax on 16.09.2017.
 * <p>
 * for ScpCore
 * <p></p>
 * Use it if you want callback for FCM token changed.
 */
public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        Timber.d("onTokenRefresh(): %s", FirebaseInstanceId.getInstance().getToken());

//        App.getAppComponent().inject(this);
//        mNotificationManager.updateToken();
    }
}