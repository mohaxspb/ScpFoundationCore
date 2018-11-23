package ru.kuchanov.scpcore.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProvider;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.downloads.ScpParseException;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.activity.MainActivity;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static ru.kuchanov.scpcore.downloads.DownloadAllService.getAndSaveInnerArticles;

public class ReceiverTimer extends BroadcastReceiver {

    public static final int NOTIF_ID = 963;

    public static final long[] VIBRATION_PATTERN = {500, 500, 500, 500, 500};

    private static final int LED_DURATION = 3000;

    @Inject
    MyPreferenceManager mMyPreferencesManager;

    @Inject
    DbProviderFactory mDbProviderFactory;

    @Inject
    ApiClient mApiClient;

    @Inject
    ConstantValues mConstantValues;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Timber.d("onReceive with action: %s", intent.getAction());
        if (context.getString(R.string.receiver_action_timer).equals(intent.getAction())) {
            callInjection();
            download(context);
        }
    }

    protected void callInjection() {
        BaseApplication.getAppComponent().inject(this);
    }

    protected void download(final Context context) {
        mApiClient.getRecentArticlesForPage(1)
                .map(downloadedArticles -> {
                    DbProvider dbProvider = mDbProviderFactory.getDbProvider();
                    List<Article> newArticles = new ArrayList<>();
                    for (Article apiArticle : downloadedArticles) {
                        Article inDbArticle = dbProvider.getArticleSync(apiArticle.url);
                        if (inDbArticle == null) {
                            //so its new one, increase counter
                            newArticles.add(apiArticle);
                        }
                    }
                    dbProvider.close();
                    return newArticles;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(newArticles -> mDbProviderFactory.getDbProvider()
                        .saveRecentArticlesList(newArticles, Constants.Api.ZERO_OFFSET)
                        .flatMap(newArticlesAddedToDb -> {
                            if (mMyPreferencesManager.isSaveNewArticlesEnabled()) {
                                return Observable.just(newArticles)
                                        .map(articles -> {
                                            int limit = (int) FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_FREE_ARTICLES_LIMIT);

                                            DbProvider dbProvider = mDbProviderFactory.getDbProvider();
                                            limit += mDbProviderFactory.getDbProvider().getScore() / FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_SCORE_PER_ARTICLE);
                                            dbProvider.close();

                                            if (limit > articles.size()) {
                                                limit = articles.size();
                                            }

                                            return articles.subList(0, limit);
                                        })
                                        .observeOn(Schedulers.io())
                                        .flatMap(articles -> {
                                            final DbProvider dbProvider = mDbProviderFactory.getDbProvider();
                                            int innerArticlesDepth = mMyPreferencesManager.getInnerArticlesDepth();
                                            for (final Article articleToDownload : articles) {
                                                try {
                                                    Timber.d("Start download article: %s", articleToDownload.title);
                                                    Article articleDownloaded = mApiClient.getArticleFromApi(articleToDownload.getUrl());
                                                    if (articleDownloaded != null) {
                                                        dbProvider.saveArticleSync(articleDownloaded, false);
                                                        mApiClient.downloadImagesOnDisk(articleDownloaded);

                                                        if (mMyPreferencesManager.isHasSubscription() && innerArticlesDepth != 0) {
                                                            getAndSaveInnerArticles(
                                                                    dbProvider,
                                                                    mApiClient,
                                                                    articleDownloaded,
                                                                    0,
                                                                    innerArticlesDepth
                                                            );
                                                        }

                                                        Timber.d("downloaded: %s", articleDownloaded.getUrl());
                                                    } else {
                                                        Timber.e("fail to load article: %s", articleToDownload.getUrl());
                                                    }
                                                } catch (Exception | ScpParseException e) {
                                                    Timber.e(e);
                                                }
                                            }
                                            dbProvider.close();
                                            return Observable.just(articles);
                                        });
                            } else {
                                return Observable.just(newArticles);
                            }
                        }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> sendNotification(context, result),
                        error -> Timber.e(error, "error while getRecentArts")
                );
    }

    public void sendNotification(final Context ctx, final List<Article> dataFromWeb) {
        if (dataFromWeb.isEmpty()) {
            Timber.d("no new articles");
            return;
        }
        // Use NotificationCompat.Builder to set up our notification.
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, "new articles");

        //icon appears in device notification bar and right hand corner of notification
        builder.setSmallIcon(R.drawable.ic_logo_notification);

        //Set the text that is displayed in the status bar when the notification first arrives.
        builder.setTicker(dataFromWeb.get(0).title);

        // This intent is fired when notification is clicked
        final Intent intent = new Intent(ctx, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_LINK, mConstantValues.getNewArticles());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        final PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Set the intent that will fire when the user taps the notification.
        builder.setContentIntent(pendingIntent);

        // Large icon appears on the left of the notification
        builder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.ic_launcher));

        // Content title, which appears in large type at the top of the notification
        //		builder.setContentTitle("Новые статьи");

        // Content text, which appears in smaller text below the title
        //				builder.setContentText("Новые статьи");

        // The subtext, which appears under the text on newer devices.
        // This will show-up in the devices with Android 4.2 and above only
        builder.setSubText(dataFromWeb.get(0).title);//"Всего новых статей:");

        builder.setAutoCancel(true);

        final NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        if (dataFromWeb.size() == mConstantValues.getNumOfArticlesOnRecentPage()) {
            final String[] events = new String[dataFromWeb.size()];
            inboxStyle.setBigContentTitle(ctx.getString(R.string.notif_new_arts_title, dataFromWeb.size(), "+"));
            // Moves events into the expanded layout
            for (int i = 0; i < events.length; i++) {
                events[i] = dataFromWeb.get(i).title;
                inboxStyle.addLine(events[i]);
            }
            builder.setNumber(dataFromWeb.size());
        } else {
            //to test
            //newQuont = "10";
            final String[] events = new String[dataFromWeb.size()];
            // Sets a title for the Inbox in expanded layout
            inboxStyle.setBigContentTitle(ctx.getString(R.string.notif_new_arts_title, dataFromWeb.size(), ""));
            // Moves events into the expanded layout
            for (int i = 0; i < events.length; i++) {
                events[i] = dataFromWeb.get(i).title;
                inboxStyle.addLine(events[i]);
            }
            builder.setNumber(dataFromWeb.size());
        }

        // Moves the expanded layout object into the notification object.
        builder.setStyle(inboxStyle);

        // Content title, which appears in large type at the top of the notification
        builder.setContentTitle(ctx.getString(R.string.notif_new_arts_title, dataFromWeb.size(), ""));
        if (mMyPreferencesManager.isNotificationVibrationEnabled()) {
            builder.setVibrate(VIBRATION_PATTERN);
        }
        //LED
        if (mMyPreferencesManager.isNotificationLedEnabled()) {
            builder.setLights(Color.WHITE, LED_DURATION, LED_DURATION);
        }
        //Sound//LED
        if (mMyPreferencesManager.isNotificationSoundEnabled()) {
            final Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(alarmSound);
        }

        final NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        // Will display the notification in the notification bar
        assert notificationManager != null;
        notificationManager.notify(NOTIF_ID, builder.build());
    }
}