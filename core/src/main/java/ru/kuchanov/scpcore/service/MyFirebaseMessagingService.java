package ru.kuchanov.scpcore.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import timber.log.Timber;

/**
 * Created by mohax on 16.09.2017.
 * <p>
 * for ScpCore
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Inject
    MyPreferenceManager mMyPreferenceManager;
    @Inject
    ApiClient mApiClient;
    @Inject
    DbProviderFactory mDbProviderFactory;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        BaseApplication.getAppComponent().inject(this);

        Timber.d("onMessageReceived: %s", remoteMessage.getData() != null ? remoteMessage.getData() : null);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Timber.d("Not authorized");
            //todo give only no ads reward
        } else {
            //todo also increase user score
        }

//        Map data = remoteMessage.getData();
//        String type = (String) data.get(Constants.PushFields.PUSH_FIELD_TYPE);
//
//        if (TextUtils.isEmpty(type)) {
//            Timber.e("type is empty!");
//            return;
//        }
    }
}