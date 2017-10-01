package ru.dante.scpfoundation.di.module;

import java.util.Locale;

import dagger.Module;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.db.model.SocialProviderModel;
import ru.kuchanov.scpcore.db.model.User;
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

            if (oldVersion == 0) {
                schema.create(SocialProviderModel.class.getSimpleName())
                        .addField(SocialProviderModel.FIELD_PROVIDER, String.class)
                        .addField(SocialProviderModel.FIELD_ID, String.class);

                RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                if (articleSchema != null) {
                    articleSchema
                            .addField(Article.FIELD_SYNCED, int.class)
                            .transform(obj -> {
                                boolean isInFavorite = obj.getLong(Article.FIELD_IS_IN_FAVORITE) != Article.ORDER_NONE;
                                boolean isInRead = obj.getBoolean(Article.FIELD_IS_IN_READEN);
                                if (isInFavorite || isInRead) {
                                    obj.set(Article.FIELD_SYNCED, Article.SYNCED_NEED);
                                }
                            });
                }

                RealmObjectSchema userSchema = schema.get(User.class.getSimpleName());
                if (userSchema != null) {
                    userSchema
                            .addField(User.FIELD_SCORE, int.class)
                            .addField(User.FIELD_UID, String.class)
                            .addField(User.FIELD_EMAIL, String.class)
                            .addRealmListField(User.FIELD_SOCIAL_PROVIDERS, schema.get(SocialProviderModel.class.getSimpleName()))
                            .removeField("firstName")
                            .removeField("lastName")
                            .removeField("network");
                }
                oldVersion++;
            }

            if (oldVersion == 1) {
                RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                if (articleSchema != null) {
                    articleSchema
                            .addField(Article.FIELD_IS_IN_OBJECTS_4, long.class)
                            .transform(obj -> obj.set(Article.FIELD_IS_IN_OBJECTS_4, Article.ORDER_NONE));
                }
                oldVersion++;
            }

            if (oldVersion == 2) {
                schema.create(ArticleTag.class.getSimpleName())
                        .addField(ArticleTag.FIELD_TITLE, String.class, FieldAttribute.PRIMARY_KEY);

                RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                if (articleSchema != null) {
                    articleSchema
                            .addRealmListField(Article.FIELD_TAGS, schema.get(ArticleTag.class.getSimpleName()));
                }
                oldVersion++;
            }

            if (oldVersion == 3) {
                RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                if (articleSchema != null) {
                    articleSchema
                            .removeField("tabsTitles")
                            .removeField("tabsTexts")
                            .removeField("hasTabs");
                }
                oldVersion++;
            }

            //add new if blocks if schema changed
            if (oldVersion < newVersion) {
                throw new IllegalStateException(String.format(Locale.ENGLISH, "Migration missing from v%d to v%d", oldVersion, newVersion));
            }
        };
    }
}