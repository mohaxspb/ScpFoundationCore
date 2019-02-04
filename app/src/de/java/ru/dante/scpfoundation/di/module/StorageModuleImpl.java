package ru.dante.scpfoundation.di.module;

import java.util.Locale;

import dagger.Module;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ReadHistoryTransaction;
import ru.kuchanov.scpcore.db.model.LeaderboardUser;
import ru.kuchanov.scpcore.db.model.RealmString;
import ru.kuchanov.scpcore.db.model.gallery.GalleryImage;
import ru.kuchanov.scpcore.db.model.gallery.GalleryImageTranslation;
import ru.kuchanov.scpcore.di.module.StorageModule;
import ru.kuchanov.scpcore.db.model.MyNativeBanner;
import io.realm.RealmResults;
import io.realm.DynamicRealmObject;
import ru.kuchanov.scpcore.db.model.BannerType;
import ru.kuchanov.scpcore.db.model.RealmString;
import timber.log.Timber;

/**
 * Created by mohax on 10.07.2017.
 * <p>
 * for ScpFoundationRu
 */
@Module(includes = StorageModule.class)
public class StorageModuleImpl extends StorageModule {

    private int currentId;

    @Override
    protected RealmMigration getRealmMigration() {
        return (realm, oldVersion, newVersion) -> {
            RealmSchema schema = realm.getSchema();

            Timber.d("providesRealmMigration: %s/%s", oldVersion, newVersion);

            if (oldVersion == 1) {
                RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                if (articleSchema != null) {
                    articleSchema
                            .removeField("tabsTitles")
                            .removeField("tabsTexts")
                            .removeField("hasTabs");
                }
                oldVersion++;
            }

            if (oldVersion == 2) {
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

            if (oldVersion == 3) {
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

            if (oldVersion == 4) {
                final RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                articleSchema
                        .addRealmListField(Article.FIELD_INNER_ARTICLES_URLS, schema.get(RealmString.class.getSimpleName()));

                oldVersion++;
            }

            if (oldVersion == 5) {
                final RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                articleSchema
                        .addField(Article.FIELD_COMMENTS_URL, String.class);

                oldVersion++;
            }

            if (oldVersion == 6) {
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

            if (oldVersion == 7) {
                RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                if (articleSchema != null) {
                    articleSchema
                            .addField(Article.FIELD_IS_IN_OBJECTS_5, long.class);
                }
                oldVersion++;
            }

            if (oldVersion == 8) {
                final RealmObjectSchema leaderboardUserSchema = schema.get(LeaderboardUser.class.getSimpleName());
                if (leaderboardUserSchema != null) {
                    leaderboardUserSchema
                            .removeField(LeaderboardUser.FIELD_UID)
                            .addField(
                                    LeaderboardUser.FIELD_ID,
                                    Long.class,
                                    FieldAttribute.INDEXED,
                                    FieldAttribute.REQUIRED
                            )
                            .transform(obj -> obj.set(LeaderboardUser.FIELD_ID, currentId++))
                            .addPrimaryKey(LeaderboardUser.FIELD_ID);
                }
                oldVersion++;
            }

            if (oldVersion == 9) {
                schema.create(MyNativeBanner.class.getSimpleName())
                        .addField(
                                MyNativeBanner.FIELD_ID,
                                Long.class,
                                FieldAttribute.PRIMARY_KEY,
                                FieldAttribute.REQUIRED
                        )
                        .addField(MyNativeBanner.FIELD_LOGO_URL, String.class)
                        .addField(MyNativeBanner.FIELD_IMAGE_URL, String.class)
                        .addField(MyNativeBanner.FIELD_TITLE, String.class)
                        .addField(MyNativeBanner.FIELD_SUB_TITLE, String.class)
                        .addField(MyNativeBanner.FIELD_CTA_BUTTON_TEXT, String.class)
                        .addField(MyNativeBanner.FIELD_REDIRECT_URL, String.class)
                        .addField(MyNativeBanner.FIELD_ENABLED, Boolean.class)
                        .addField(MyNativeBanner.FIELD_AUTHOR_ID, Long.class)
                        .addField(MyNativeBanner.FIELD_CREATED, String.class)
                        .addField(MyNativeBanner.FIELD_UPDATED, String.class)
                        .addField(MyNativeBanner.FIELD_BANNER_TYPE, String.class);


                final DynamicRealmObject banner = realm.createObject(MyNativeBanner.class.getSimpleName(), 999999);
                banner.setString(MyNativeBanner.FIELD_LOGO_URL, "ads/files/5/logo");
                banner.setString(MyNativeBanner.FIELD_IMAGE_URL, "ads/files/5/image");
                banner.setString(MyNativeBanner.FIELD_TITLE, "Книги SCP Foundation уже в продаже!");
                banner.setString(MyNativeBanner.FIELD_SUB_TITLE, "Спрашивайте в книжных магазинах своего города или закажите доставку в любой уголок страны");
                banner.setString(MyNativeBanner.FIELD_CTA_BUTTON_TEXT, "Подробнее");
                banner.setString(MyNativeBanner.FIELD_REDIRECT_URL, "http://artscp.com/promo?utm_source=ru.kuchanov.scpfoundation&utm_medium=referral&utm_campaign=app-ads&utm_term=1");
                banner.setBoolean(MyNativeBanner.FIELD_ENABLED, true);
                banner.setLong(MyNativeBanner.FIELD_AUTHOR_ID, 32062);
                banner.setString(MyNativeBanner.FIELD_CREATED, "2019-01-06T17:42:59.341Z");
                banner.setString(MyNativeBanner.FIELD_UPDATED, "2019-01-06T17:42:59.341Z");
                banner.setString(MyNativeBanner.FIELD_BANNER_TYPE, BannerType.ART.name());

                final RealmResults<DynamicRealmObject> banners = realm.where(MyNativeBanner.class.getSimpleName()).findAll();
                Timber.d("banners: %s", banners);

                oldVersion++;
            }

            if (oldVersion == 10) {
                schema.create(ReadHistoryTransaction.class.getSimpleName())
                        .addField(
                                MyNativeBanner.FIELD_ID,
                                Long.class,
                                FieldAttribute.PRIMARY_KEY,
                                FieldAttribute.REQUIRED
                        )
                        .addField(ReadHistoryTransaction.FIELD_TITLE, String.class)
                        .addField(ReadHistoryTransaction.FIELD_URL, String.class)
                        .addField(ReadHistoryTransaction.FIELD_CREATED, Long.class);

                oldVersion++;
            }

            //add new if blocks if schema changed
            if (oldVersion < newVersion) {
                throw new IllegalStateException(String.format(Locale.ENGLISH, "Migration missing from v%d to v%d", oldVersion, newVersion));
            }
        };
    }
}