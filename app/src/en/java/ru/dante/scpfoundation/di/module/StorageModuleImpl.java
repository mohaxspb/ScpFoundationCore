package ru.dante.scpfoundation.di.module;

import java.util.Locale;

import dagger.Module;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.di.module.StorageModule;
import timber.log.Timber;

/**
 * Created by mohax on 10.07.2017.
 * <p>
 * for ScpFoundationRu
 */
@Module(includes = StorageModule.class)
public class StorageModuleImpl extends StorageModule {

    @Override
    protected RealmMigration getRealmMigration() {
        return (realm, oldVersion, newVersion) -> {
            RealmSchema schema = realm.getSchema();

            Timber.d("providesRealmMigration: %s/%s", oldVersion, newVersion);

            for (RealmObjectSchema realmObjectSchema : schema.getAll()) {
                Timber.d("realmObjectSchema: %s", realmObjectSchema.getClassName());
                Timber.d("realmObjectSchema: %s", realmObjectSchema.getFieldNames());
            }

            if (oldVersion == 1) {
                schema.get(Article.class.getSimpleName())
                        .addField(Article.FIELD_IS_IN_ARCHIVE, long.class)
                        .transform(obj -> obj.set(Article.FIELD_IS_IN_ARCHIVE, Article.ORDER_NONE))
                        .addField(Article.FIELD_IS_IN_EXPERIMETS, long.class)
                        .transform(obj -> obj.set(Article.FIELD_IS_IN_EXPERIMETS, Article.ORDER_NONE))
                        .addField(Article.FIELD_IS_IN_INCIDENTS, long.class)
                        .transform(obj -> obj.set(Article.FIELD_IS_IN_INCIDENTS, Article.ORDER_NONE))
                        .addField(Article.FIELD_IS_IN_INTERVIEWS, long.class)
                        .transform(obj -> obj.set(Article.FIELD_IS_IN_INTERVIEWS, Article.ORDER_NONE))
                        .addField(Article.FIELD_IS_IN_JOKES, long.class)
                        .transform(obj -> obj.set(Article.FIELD_IS_IN_JOKES, Article.ORDER_NONE))
                        .addField(Article.FIELD_IS_IN_OTHER, long.class)
                        .transform(obj -> obj.set(Article.FIELD_IS_IN_OTHER, Article.ORDER_NONE));
                oldVersion++;
            }

            //TODO add new if blocks if schema changed
            if (oldVersion < newVersion) {
                throw new IllegalStateException(String.format(Locale.ENGLISH, "Migration missing from v%d to v%d", oldVersion, newVersion));
            }
        };
    }
}