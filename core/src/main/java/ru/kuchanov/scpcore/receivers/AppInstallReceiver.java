package ru.kuchanov.scpcore.receivers;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;

import org.joda.time.Duration;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.BuildConfig;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.model.ApplicationsResponse;
import ru.kuchanov.scpcore.monetization.model.PlayMarketApplication;
import ru.kuchanov.scpcore.mvp.base.BasePresenter;
import ru.kuchanov.scpcore.mvp.contract.DataSyncActions;
import ru.kuchanov.scpcore.ui.activity.MainActivity;
import ru.kuchanov.scpcore.util.Entry;
import ru.kuchanov.scpcore.util.RemoteConfigJsonModel;
import ru.kuchanov.scpcore.util.StorageUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class AppInstallReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 100;

    @Inject
    Gson mGson;

    @Inject
    MyPreferenceManager mMyPreferencesManager;

    @Inject
    ApiClient mApiClient;

    @Inject
    DbProviderFactory mDbProviderFactory;

    @Inject
    ConstantValues mConstantValues;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        callInjection();
        final String packageName = intent.getData().getEncodedSchemeSpecificPart();
        Timber.d("intent data: %s", packageName);

        initRemoteConfig();

        final List<PlayMarketApplication> applications;
        try {
            applications = mGson.fromJson(FirebaseRemoteConfig.getInstance().
                    getString(Constants.Firebase.RemoteConfigKeys.APPS_TO_INSTALL_JSON), ApplicationsResponse.class)
                    .items;
        } catch (final Exception e) {
            Timber.e(e);
            return;
        }

        if (!mMyPreferencesManager.isAppInstalledForPackage(packageName) && applications.contains(new PlayMarketApplication(packageName))) {
            mMyPreferencesManager.setAppInstalledForPackage(packageName);
            mMyPreferencesManager.applyAwardForAppInstall();

            final long numOfMillis = FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.APP_INSTALL_REWARD_IN_MILLIS);
            final int hours = Duration.millis(numOfMillis).toStandardHours().getHours();

            //update score
            updateScoreFromAppInstall(packageName);

            showNotificationSimple(context, context.getString(R.string.ads_reward_gained, hours), context.getString(R.string.thanks_for_supporting_us));

            final Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, packageName);
            FirebaseAnalytics.getInstance(context).logEvent(Constants.Firebase.Analitics.EventName.APP_INSTALLED, bundle);
        }
    }

    protected void callInjection() {
        BaseApplication.getAppComponent().inject(this);
    }

    private void updateScoreFromAppInstall(final String packageName) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Timber.d("user unlogined, do nothing");
            return;
        }

        @DataSyncActions.ScoreAction final String action = DataSyncActions.ScoreAction.OUR_APP;
        final int totalScoreToAdd = BasePresenter.getTotalScoreToAddFromAction(action, mMyPreferencesManager);

        //increment scoreInFirebase
        mApiClient
                .isUserInstallApp(packageName)
                .flatMap(isUserInstallApp -> isUserInstallApp ?
                                             Observable.empty() :
                                             mApiClient.incrementScoreInFirebaseObservable(totalScoreToAdd)
                                                     .flatMap(newTotalScore -> mApiClient.addInstalledApp(packageName).flatMap(aVoid -> Observable.just(newTotalScore)))
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(newTotalScore -> mDbProviderFactory.getDbProvider().updateUserScore(newTotalScore))
                .subscribe(
                        newTotalScore -> {
                            Timber.d("new total score is: %s", newTotalScore);
                            final Context context = BaseApplication.getAppInstance();
                            Toast.makeText(
                                    context,
                                    context.getString(R.string.score_increased, context.getResources().getQuantityString(R.plurals.plurals_score, totalScoreToAdd, totalScoreToAdd))
                                    , Toast.LENGTH_LONG
                            ).show();
                        },
                        e -> {
                            Timber.e(e, "error while increment userCore from action");
//                            getView().showError(e);
                            //increment unsynced score to sync it later
                            mMyPreferencesManager.addUnsyncedApp(packageName);
                        }
                );
    }

    private static void showNotificationSimple(final Context context, final String title, final String content) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "appInstall");
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT), 0);
        builder.setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setContentText(content)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher);

        final NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    private void initRemoteConfig() {
        //remote config
        try {
            final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

            // Create Remote Config Setting to enable developer mode.
            // Fetching configs from the server is normally limited to 5 requests per hour.
            // Enabling developer mode allows many more requests to be made per hour, so developers
            // can test different config values during development.
            final FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.FLAVOR.equals("dev"))
                    .build();
            mFirebaseRemoteConfig.setConfigSettings(configSettings);

            // Set default Remote Config values. In general you should have in app defaults for all
            // values that you may configure using Remote Config later on. The idea is that you
            // use the in app defaults and when you need to adjust those defaults, you set an updated
            // value in the App Manager console. Then the next time you application fetches from the
            // server, the updated value will be used. You can set defaults via an xml file like done
            // here or you can set defaults inline by using one of the other setDefaults methods.S
            // [START set_default_values]
//        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

            try {
                final Map<String, Object> defaults = new HashMap<>();
                final RemoteConfigJsonModel remoteConfigJsonModel = new Gson().fromJson(
                        StorageUtils.readFromAssets(BaseApplication.getAppInstance(), mConstantValues.getAppLang() + ".json"),
                        RemoteConfigJsonModel.class
                );
                for (final Entry entry : remoteConfigJsonModel.getDefaultsMap().getEntry()) {
                    defaults.put(entry.getKey(), entry.getValue());
                }
                mFirebaseRemoteConfig.setDefaults(defaults);
            } catch (final IOException e) {
                Timber.e(e);
            }
        } catch (final Exception e) {
            Timber.e(e);
        }
    }
}