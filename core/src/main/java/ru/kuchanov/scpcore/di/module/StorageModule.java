package ru.kuchanov.scpcore.di.module;

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
 * <p>
 * for scp_ru
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
    MyPreferenceManager providesPreferenceManager(final Context context, final Gson gson) {
        return new MyPreferenceManager(context, gson);
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