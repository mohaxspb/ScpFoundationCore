package ru.kuchanov.scpcore.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.activity.MainActivity;
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


        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

//        buildNotification(
//                remoteMessage.getMessageId().hashCode(),
//                remoteMessage.getNotification().getTitle(),
//                remoteMessage.getNotification().getBody(),
//                pendingIntent
//        );

        buildNotification(
                remoteMessage.getMessageId().hashCode(),
                getString(R.string.your_invite_received),
                getString(R.string.invite_receiver_reward_message),
                pendingIntent
        );

//        Map data = remoteMessage.getData();
//        String type = (String) data.get(Constants.PushFields.PUSH_FIELD_TYPE);
//
//        if (TextUtils.isEmpty(type)) {
//            Timber.e("type is empty!");
//            return;
//        }
    }

    private void buildNotification(int id, String title, String message, PendingIntent pendingIntent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "invite push")
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_logo_notification)
                .setContentTitle(title)
                .setWhen(System.currentTimeMillis())
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{1000, 1000})
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(id, notificationBuilder.build());
    }
}