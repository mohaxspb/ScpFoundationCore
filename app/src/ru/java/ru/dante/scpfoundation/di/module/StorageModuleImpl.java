package ru.dante.scpfoundation.di.module;

import java.util.Locale;

import dagger.Module;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.db.model.LeaderboardUser;
import ru.kuchanov.scpcore.db.model.RealmString;
import ru.kuchanov.scpcore.db.model.SocialProviderModel;
import ru.kuchanov.scpcore.db.model.User;
import ru.kuchanov.scpcore.db.model.gallery.GalleryImage;
import ru.kuchanov.scpcore.db.model.gallery.GalleryImageTranslation;
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
            final RealmSchema schema = realm.getSchema();

            Timber.d("providesRealmMigration: %s/%s", oldVersion, newVersion);

            for (final RealmObjectSchema realmObjectSchema : schema.getAll()) {
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

            if (oldVersion == 3) {
                final RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                if (articleSchema != null) {
                    articleSchema
                            .removeField("tabsTitles")
                            .removeField("tabsTexts")
                            .removeField("hasTabs");
                }
                oldVersion++;
            }

            if (oldVersion == 4) {
                final RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
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

            if (oldVersion == 5) {
                final RealmObjectSchema leaderboardUserSchema = schema.create(LeaderboardUser.class.getSimpleName());
                leaderboardUserSchema
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

            if (oldVersion == 6) {
                final RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                articleSchema
                        .addRealmListField(Article.FIELD_INNER_ARTICLES_URLS, schema.get(RealmString.class.getSimpleName()));

                oldVersion++;
            }

            if (oldVersion == 7) {
                final RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                articleSchema
                        .addField(Article.FIELD_COMMENTS_URL, String.class);

                oldVersion++;
            }

            if (oldVersion == 8) {
                schema.remove("VkImage");

                schema.create(GalleryImageTranslation.class.getSimpleName())
                        .addField(
                                GalleryImageTranslation.FIELD_ID,
                                int.class,
                                FieldAttribute.PRIMARY_KEY,
                                FieldAttribute.REQUIRED
                        )
                        .addField(GalleryImageTranslation.FIELD_LANG_CODE, String.class)
                        .setRequired(GalleryImageTranslation.FIELD_LANG_CODE, true)
                        .addField(GalleryImageTranslation.FIELD_TRANSLATION, String.class)
                        .setRequired(GalleryImageTranslation.FIELD_TRANSLATION, true)
                        .addField(GalleryImageTranslation.FIELD_AUTHOR_ID, int.class)
                        .addField(GalleryImageTranslation.FIELD_APPROVED, boolean.class)
                        .addField(GalleryImageTranslation.FIELD_APPROVER_ID, Integer.class)
                        .addField(GalleryImageTranslation.FIELD_CREATED, String.class)
                        .setRequired(GalleryImageTranslation.FIELD_CREATED, true)
                        .addField(GalleryImageTranslation.FIELD_UPDATED, String.class)
                        .setRequired(GalleryImageTranslation.FIELD_UPDATED, true);

                schema.create(GalleryImage.class.getSimpleName())
                        .addField(
                                GalleryImage.FIELD_ID,
                                int.class,
                                FieldAttribute.PRIMARY_KEY,
                                FieldAttribute.REQUIRED
                        )
                        .addField(GalleryImage.FIELD_VK_ID, int.class)
                        .addField(GalleryImage.FIELD_IMAGE_URL, String.class)
                        .setRequired(GalleryImage.FIELD_IMAGE_URL, true)
                        .addField(GalleryImage.FIELD_AUTHOR_ID, int.class)
                        .addField(GalleryImage.FIELD_APPROVED, boolean.class)
                        .addField(GalleryImage.FIELD_APPROVER_ID, Integer.class)
                        .addField(GalleryImage.FIELD_CREATED, String.class)
                        .setRequired(GalleryImage.FIELD_CREATED, true)
                        .addField(GalleryImage.FIELD_UPDATED, String.class)
                        .setRequired(GalleryImage.FIELD_UPDATED, true)
                        .addRealmListField(
                                GalleryImage.FIELD_GALLERY_IMAGE_TRANSLATIONS,
                                schema.get(GalleryImageTranslation.class.getSimpleName())
                        );

                oldVersion++;
            }

            //TODO add new if blocks if schema changed
            if (oldVersion < newVersion) {
                throw new IllegalStateException(String.format(Locale.ENGLISH, "Migration missing from v%d to v%d", oldVersion, newVersion));
            }
        };
    }
}