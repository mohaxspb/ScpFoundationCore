package ru.kuchanov.scpcore.di.module;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;

/**
 * Created by y.kuchanov on 22.12.16.
 */
@Module
public class StorageModule {

    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @Singleton
    FirebaseRemoteConfig providesRemoteConfig() {
        return FirebaseRemoteConfig.getInstance();
    }

    @Provides
    @Singleton
    MyPreferenceManager providesPreferenceManager(
            final Context context,
            final SharedPreferences preferences,
            final Gson gson,
            final FirebaseRemoteConfig remoteConfig
    ) {
        return new MyPreferenceManager(context, preferences, gson, remoteConfig);
    }

    @Provides
    @Singleton
    RealmMigration providesRealmMigration() {
        return getRealmMigration();
    }

    protected RealmMigration getRealmMigration() {
        throw new IllegalStateException("override method in app gradle module!");
    }

    @Provides
    @Singleton
    RealmConfiguration providesRealmConfiguration(
            final RealmMigration realmMigration,
            final Context context
    ) {
        return new RealmConfiguration.Builder()
                .schemaVersion(context.getResources().getInteger(R.integer.realm_version))
                .migration(realmMigration)
                .build();
    }

    @Provides
    @Singleton
    DbProviderFactory providesDbProviderFactory(
            final RealmConfiguration configuration,
            final MyPreferenceManager preferenceManager,
            final ConstantValues constantValues
    ) {
        return new DbProviderFactory(configuration, preferenceManager, constantValues);
    }
}