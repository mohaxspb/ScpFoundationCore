package ru.kuchanov.scpcore;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import com.crashlytics.android.Crashlytics;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import ru.kuchanov.scpcore.di.AppComponent;
import ru.kuchanov.scpcore.util.SystemUtils;
import timber.log.Timber;

/**
 * Created by mohax on 01.01.2017.
 * <p>
 * for scp_ru
 */
public abstract class BaseApplication extends MultiDexApplication {

    private static AppComponent sAppComponent;

    private static BaseApplication sAppInstance;

    public static AppComponent getAppComponent() {
        return sAppComponent;
    }

    public static BaseApplication getAppInstance() {
        return sAppInstance;
    }

    public RefWatcher getRefWatcher() {
        return refWatcher;
    }

    private RefWatcher refWatcher;

    VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                //VKAccessToken is invalid
                //TODO may be we need to logout user...
                Timber.d("VK token is null, do smth with it!");
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }

        refWatcher = LeakCanary.install(this);

        FirebaseApp.initializeApp(this);

        //yandex metrica
        YandexMetrica.activate(
                getApplicationContext(),
                YandexMetricaConfig.newConfigBuilder(getString(R.string.yandex_metrica_api_key)).build()
        );
        YandexMetrica.enableActivityAutoTracking(this);

        sAppInstance = this;

        sAppComponent = buildAppComponentImpl();

        if (BuildConfig.TIMBER_ENABLE) {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected void log(final int priority, final String tag, @NonNull final String message, final Throwable t) {
                    super.log(priority, tag, formatLogs(message), t);
                }

                private String formatLogs(final String message) {
                    if (!message.startsWith("{")) {
                        return message;
                    }
                    try {
                        return new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(message));
                    } catch (final JsonSyntaxException m) {
                        return message;
                    }
                }
            });
        } else {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected void log(int priority, String tag, String message, Throwable t) {
                    if (priority == Log.ERROR) {
                        //maybe send error via some service, i.e. firebase or googleAnalitics
                        super.log(priority, tag, message, t);
                    }
                }
            });
        }

        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(this);

        SystemUtils.printCertificateFingerprints();

        Realm.init(this);

        //subscribe to main push topic
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.Firebase.PushTopics.MAIN);

        //need to initialize it manually
        //https://stackoverflow.com/a/50782095/3212712
        Fabric.with(this, new Crashlytics());
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String userIdForCrashlytics = user == null ? "unloginedUser" : user.getUid();
        Crashlytics.setUserIdentifier(userIdForCrashlytics);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    protected abstract AppComponent buildAppComponentImpl();
}