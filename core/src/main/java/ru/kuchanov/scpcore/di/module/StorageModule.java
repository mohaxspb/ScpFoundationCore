package ru.kuchanov.scpcore.di.module;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.Locale;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.FieldAttribute;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.db.model.SocialProviderModel;
import ru.kuchanov.scpcore.db.model.User;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import timber.log.Timber;

/**
 * Created by y.kuchanov on 22.12.16.
 * <p>
 * for scp_ru
 */
@Module
public class StorageModule {

    @Provides
    @NonNull
    @Singleton
    SharedPreferences providesSharedPreferences(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @NonNull
    @Singleton
    MyPreferenceManager providesPreferencesManager(@NonNull Context context, @NonNull Gson gson) {
        return new MyPreferenceManager(context, gson);
    }

    @Provides
    @NonNull
    @Singleton
    RealmMigration providesRealmMigration() {
        return getRealmMigration();
    }

    protected RealmMigration getRealmMigration() {
        return (realm, oldVersion, newVersion) -> {
            RealmSchema schema = realm.getSchema();

            Timber.d("providesRealmMigration: %s/%s", oldVersion, newVersion);

            for (RealmObjectSchema realmObjectSchema : schema.getAll()) {
                Timber.d("realmObjectSchema: %s", realmObjectSchema.getClassName());
                Timber.d("realmObjectSchema: %s", realmObjectSchema.getFieldNames());
            }

            if (oldVersion == 0) {
                schema.create(SocialProviderModel.class.getSimpleName())
                        .addField(SocialProviderModel.FIELD_PROVIDER, String.class)
                        .addField(SocialProviderModel.FIELD_ID, String.class);

                schema.get(Article.class.getSimpleName())
                        .addField(Article.FIELD_SYNCED, int.class)
                        .transform(obj -> {
                            boolean isInFavorite = obj.getLong(Article.FIELD_IS_IN_FAVORITE) != Article.ORDER_NONE;
                            boolean isInRead = obj.getBoolean(Article.FIELD_IS_IN_READEN);
                            if (isInFavorite || isInRead) {
                                obj.set(Article.FIELD_SYNCED, Article.SYNCED_NEED);
                            }
                        });

                schema.get(User.class.getSimpleName())
                        .addField(User.FIELD_SCORE, int.class)
                        .addField(User.FIELD_UID, String.class)
                        .addField(User.FIELD_EMAIL, String.class)
                        .addRealmListField(User.FIELD_SOCIAL_PROVIDERS, schema.get(SocialProviderModel.class.getSimpleName()))
                        .removeField("firstName")
                        .removeField("lastName")
                        .removeField("network");

                oldVersion++;
            }

            if (oldVersion == 1) {
                schema.get(Article.class.getSimpleName())
                        .addField(Article.FIELD_IS_IN_OBJECTS_4, long.class)
                        .transform(obj -> obj.set(Article.FIELD_IS_IN_OBJECTS_4, Article.ORDER_NONE));
                oldVersion++;
            }

            if (oldVersion == 2) {
                schema.create(ArticleTag.class.getSimpleName())
                        .addField(ArticleTag.FIELD_TITLE, String.class, FieldAttribute.PRIMARY_KEY);

                schema.get(Article.class.getSimpleName())
                        .addRealmListField(Article.FIELD_TAGS, schema.get(ArticleTag.class.getSimpleName()));

                oldVersion++;
            }

            //TODO add new if blocks if schema changed
            if (oldVersion < newVersion) {
                throw new IllegalStateException(String.format(Locale.ENGLISH, "Migration missing from v%d to v%d", oldVersion, newVersion));
            }
        };
    }

    @Provides
    @NonNull
    @Singleton
    RealmConfiguration providesRealmConfiguration(@NonNull RealmMigration realmMigration, @NonNull Context context) {
        return new RealmConfiguration.Builder()
                .schemaVersion(context.getResources().getInteger(R.integer.realm_version))
                .migration(realmMigration)
                .build();
    }

    @Provides
    @NonNull
    @Singleton
    DbProviderFactory providesDbProviderFactory(
            @NonNull RealmConfiguration configuration,
            @NonNull MyPreferenceManager preferenceManager,
            @NonNull ConstantValues constantValues
    ) {
        return new DbProviderFactory(configuration, preferenceManager, constantValues);
    }
}