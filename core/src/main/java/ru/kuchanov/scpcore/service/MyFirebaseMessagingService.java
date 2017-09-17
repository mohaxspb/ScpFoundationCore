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

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.base.BasePresenter;
import ru.kuchanov.scpcore.mvp.contract.DataSyncActions;
import ru.kuchanov.scpcore.ui.activity.MainActivity;
import ru.kuchanov.scpcore.ui.activity.WebViewActivity;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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

        //todo need to switch by some key-value pair to be able to handle different pushes
        if (remoteMessage.getNotification() == null) {
            //as we didn't add some pushType params while send push from server we'll think that there is
            //only one type - invite. For any other types we'll think that it's mass sent with simple text

            //or, as for now we can just check for inviteId key in data
            if (remoteMessage.getData().containsKey(Constants.Firebase.PushDataKeys.INVITE_ID)) {
                //give no ads reward
                mMyPreferenceManager.applyAwardForInvite();

                String notifMessage;
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    //also increase user score
                    incrementUserScoreForInvite();

                    long numOfMillis = FirebaseRemoteConfig.getInstance()
                            .getLong(Constants.Firebase.RemoteConfigKeys.INVITE_REWARD_IN_MILLIS);
                    int hours = Duration.millis(numOfMillis).toStandardHours().getHours();
                    int score = (int) FirebaseRemoteConfig.getInstance()
                            .getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_INVITE);
                    notifMessage = getString(R.string.invite_received_reward_message, hours, score);
                } else {
                    Timber.d("Not authorized, so only noAds period is increased");
                    notifMessage = getString(R.string.ads_disabled_for_some_hours);
                }

                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

                buildNotification(
                        remoteMessage.getMessageId().hashCode(),
                        getString(R.string.your_invite_received),
                        notifMessage,
                        pendingIntent
                );
            } else {
                String title = remoteMessage.getData().get(Constants.Firebase.PushDataKeys.TITLE);
                String message = remoteMessage.getData().get(Constants.Firebase.PushDataKeys.MESSAGE);
                String url = remoteMessage.getData().get(Constants.Firebase.PushDataKeys.URL);
                boolean openInThirdPartyBrowser =
                        Boolean.parseBoolean(remoteMessage.getData().get(Constants.Firebase.PushDataKeys.OPEN_IN_THIRD_PARTY_BROWSER));
                Intent intent;
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
                PendingIntent pendingIntent = PendingIntent.getActivity(
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
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
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

//        Map data = remoteMessage.getData();
//        String type = (String) data.get(Constants.PushFields.PUSH_FIELD_TYPE);
//
//        if (TextUtils.isEmpty(type)) {
//            Timber.e("type is empty!");
//            return;
//        }
    }

    private void incrementUserScoreForInvite() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Timber.d("user unlogined, do nothing");
            return;
        }

        @DataSyncActions.ScoreAction
        String action = DataSyncActions.ScoreAction.INVITE;
        int totalScoreToAdd = BasePresenter.getTotalScoreToAddFromAction(action, mMyPreferenceManager);

        //increment scoreInFirebase
        mApiClient
                .incrementScoreInFirebaseObservable(totalScoreToAdd)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(newTotalScore -> mDbProviderFactory.getDbProvider().updateUserScore(newTotalScore))
                .subscribe(
                        newTotalScore -> {
                            Timber.d("new total score is: %s", newTotalScore);
                        },
                        e -> {
                            Timber.e(e, "error while increment userCore from action");
                            //increment unsynced score to sync it later
                            mMyPreferenceManager.addUnsyncedScore(totalScoreToAdd);
                        }
                );
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