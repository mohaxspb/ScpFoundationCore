package ru.kuchanov.scpcore.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.joda.time.Duration;

import java.util.Map;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.base.BasePresenter;
import ru.kuchanov.scpcore.mvp.contract.DataSyncActions;
import ru.kuchanov.scpcore.ui.activity.MainActivity;
import ru.kuchanov.scpcore.ui.activity.WebViewActivity;
import ru.kuchanov.scpcore.util.NotificationUtilsKt;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by mohax on 16.09.2017.
 * <p>
 * for ScpCore
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String CHANEL_ID = "FCM_CHANEL_ID";

    public static final String CHANEL_NAME = "FCM_CHANEL_NAME";

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    @Inject
    ApiClient mApiClient;

    @Inject
    DbProviderFactory mDbProviderFactory;

    @Inject
    protected ConstantValues mConstantValues;

    @Override
    public void onCreate() {
        super.onCreate();

        BaseApplication.getAppComponent().inject(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtilsKt.createNotificationChannel(
                    this,
                    CHANEL_ID + "_" + mConstantValues.getAppLang(),
                    CHANEL_NAME + "_" + mConstantValues.getAppLang()
            );
        }
    }

    @Override
    public void onNewToken(final String s) {
        super.onNewToken(s);
        try {
            Timber.d("onNewToken(): %s", s);
        } catch (final Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        Timber.d("onMessageReceived: %s", remoteMessage.getData());
        super.onMessageReceived(remoteMessage);

        //todo need to switch by some key-value pair to be able to handle different pushes
        if (remoteMessage.getNotification() == null) {
            //as we didn't add some pushType params while send push from server we'll think that there is
            //only one type - invite. For any other types we'll think that it's mass sent with simple text

            //or, as for now we can just check for inviteId key in data
            if (remoteMessage.getData().containsKey(Constants.Firebase.PushDataKeys.INVITE_ID)) {
                Timber.d("Handle invite accepted push");
                //give no ads reward
                mMyPreferenceManager.applyAwardForInvite();

                final String notifMessage;
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    //also increase user score
                    incrementUserScoreForInvite();

                    final long numOfMillis = FirebaseRemoteConfig.getInstance()
                            .getLong(Constants.Firebase.RemoteConfigKeys.INVITE_REWARD_IN_MILLIS);
                    final int hours = Duration.millis(numOfMillis).toStandardHours().getHours();
                    final int score = (int) FirebaseRemoteConfig.getInstance()
                            .getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_INVITE);
                    notifMessage = getString(R.string.invite_received_reward_message, hours, score);
                } else {
                    Timber.d("Not authorized, so only noAds period is increased");
                    final long numOfMillis = FirebaseRemoteConfig.getInstance()
                            .getLong(Constants.Firebase.RemoteConfigKeys.INVITE_REWARD_IN_MILLIS);
                    final int hours = Duration.millis(numOfMillis).toStandardHours().getHours();
                    notifMessage = getString(R.string.ads_disabled_for_some_hours, hours);
                }

                final Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                final PendingIntent pendingIntent = PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

                Timber.d("title/body/messageId: %s/%s/%s",
                        getString(R.string.your_invite_received),
                        notifMessage,
                        remoteMessage.getMessageId()
                );

                buildNotification(
                        remoteMessage.getMessageId().hashCode(),
                        getString(R.string.your_invite_received),
                        notifMessage,
                        pendingIntent
                );
            } else {
                Map<String, String> data = remoteMessage.getData();
                final String title = data.get(Constants.Firebase.PushDataKeys.TITLE);
                final String message = data.get(Constants.Firebase.PushDataKeys.MESSAGE);
                final String url = data.get(Constants.Firebase.PushDataKeys.URL);
                final boolean openInThirdPartyBrowser = Boolean.parseBoolean(
                        data.get(Constants.Firebase.PushDataKeys.OPEN_IN_THIRD_PARTY_BROWSER)
                );
                final Intent intent;
                if (url == null) {
                    intent = new Intent(this, MainActivity.class);
                } else if (!openInThirdPartyBrowser) {
                    intent = new Intent(this, WebViewActivity.class);
                    intent.putExtra(WebViewActivity.EXTRA_URL, url);
                } else {
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                final PendingIntent pendingIntent = PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_ONE_SHOT
                );
                buildNotification(
                        remoteMessage.getMessageId().hashCode(),
                        title,
                        message,
                        pendingIntent
                );
            }
        } else {
            final Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            final PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            Timber.d("title/body/messageId: %s/%s/%s",
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody(),
                    remoteMessage.getMessageId()
            );
            buildNotification(
                    remoteMessage.getMessageId().hashCode(),
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody(),
                    pendingIntent
            );
        }
    }

    //fixme delete it!
    @Deprecated
    private void incrementUserScoreForInvite() {
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//            Timber.d("user unlogined, do nothing");
//            return;
//        }
//
//        @DataSyncActions.ScoreAction final String action = DataSyncActions.ScoreAction.INVITE;
//        final int totalScoreToAdd = BasePresenter.getTotalScoreToAddFromAction(action, mMyPreferenceManager);

        //increment scoreInFirebase
//        mApiClient
//                .incrementScoreInFirebase(totalScoreToAdd)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .flatMap(newTotalScore -> mDbProviderFactory.getDbProvider().updateUserScore(newTotalScore))
//                .subscribe(
//                        newTotalScore -> Timber.d("new total score is: %s", newTotalScore),
//                        e -> {
//                            Timber.e(e, "error while increment userCore from action");
//                            //increment unsynced score to sync it later
//                            mMyPreferenceManager.addUnsyncedScore(totalScoreToAdd);
//                        }
//                );
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private void buildNotification(
            final int id,
            final CharSequence title,
            final String message,
            final PendingIntent pendingIntent
    ) {
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getChanelId())
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

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(id, notificationBuilder.build());
    }

    private String getChanelId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return NotificationUtilsKt.createNotificationChannel(
                    this,
                    CHANEL_ID + "_" + mConstantValues.getAppLang(),
                    CHANEL_NAME + "_" + mConstantValues.getAppLang()
            );
        } else {
            return "";
        }
    }
}
