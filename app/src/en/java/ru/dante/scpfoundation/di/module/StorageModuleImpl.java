package ru.dante.scpfoundation.di.module;

import java.util.Locale;

import dagger.Module;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.LeaderboardUser;
import ru.kuchanov.scpcore.db.model.RealmString;
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

            if (oldVersion == 2) {
                RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                if (articleSchema != null) {
                    articleSchema
                            .removeField("tabsTitles")
                            .removeField("tabsTexts")
                            .removeField("hasTabs");
                }
                oldVersion++;
            }

            if (oldVersion == 3) {
                RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                if (articleSchema != null) {
                    articleSchema
                            .addField(Article.FIELD_IS_IN_OBJECTS_FR, long.class)
                            .addField(Article.FIELD_IS_IN_OBJECTS_JP, long.class)
                            .addField(Article.FIELD_IS_IN_OBJECTS_ES, long.class)
                            .addField(Article.FIELD_IS_IN_OBJECTS_PL, long.class)
                            .addField(Article.FIELD_IS_IN_OBJECTS_DE, long.class);
                }
                oldVersion++;
            }

            if (oldVersion == 4) {
                final RealmObjectSchema articleSchema = schema.create(LeaderboardUser.class.getSimpleName());
                articleSchema
                        .addField(
                                LeaderboardUser.FIELD_UID,
                                String.class,
                                FieldAttribute.PRIMARY_KEY,
                                FieldAttribute.INDEXED,
                                FieldAttribute.REQUIRED
                        )
                        .addField(LeaderboardUser.FIELD_FULL_NAME, String.class)
                        .setRequired(LeaderboardUser.FIELD_FULL_NAME, true)
                        .setNullable(LeaderboardUser.FIELD_FULL_NAME, true)
                        .addField(LeaderboardUser.FIELD_AVATAR, String.class)
                        .setRequired(LeaderboardUser.FIELD_AVATAR, true)
                        .setNullable(LeaderboardUser.FIELD_AVATAR, true)
                        .addField(LeaderboardUser.FIELD_SCORE, Integer.class)
                        .setRequired(LeaderboardUser.FIELD_SCORE, true)
                        .addField(LeaderboardUser.FIELD_NUM_OF_READ_ARTICLES, Integer.class)
                        .setRequired(LeaderboardUser.FIELD_NUM_OF_READ_ARTICLES, true)
                        .addField(LeaderboardUser.FIELD_LEVEL_NUM, Integer.class)
                        .setRequired(LeaderboardUser.FIELD_LEVEL_NUM, true)
                        .addField(LeaderboardUser.FIELD_SCORE_TO_NEXT_LEVEL, Integer.class)
                        .setRequired(LeaderboardUser.FIELD_SCORE_TO_NEXT_LEVEL, true)
                        .addField(LeaderboardUser.FIELD_CUR_LEVEL_SCORE, Integer.class)
                        .setRequired(LeaderboardUser.FIELD_CUR_LEVEL_SCORE, true);

                oldVersion++;
            }

            if (oldVersion == 5) {
                final RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                articleSchema
                        .addRealmListField(Article.FIELD_INNER_ARTICLES_URLS, schema.get(RealmString.class.getSimpleName()));

                oldVersion++;
            }

            if (oldVersion == 6) {
                final RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                articleSchema
                        .addField(Article.FIELD_COMMENTS_URL, String.class);

                oldVersion++;
            }

            //add new if blocks if schema changed
            if (oldVersion < newVersion) {
                throw new IllegalStateException(String.format(Locale.ENGLISH, "Migration missing from v%d to v%d", oldVersion, newVersion));
            }
        };
    }
}