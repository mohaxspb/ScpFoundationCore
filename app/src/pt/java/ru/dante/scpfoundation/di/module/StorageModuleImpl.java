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

            if (oldVersion == 1) {
                final RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                articleSchema
                        .addRealmListField(Article.FIELD_INNER_ARTICLES_URLS, schema.get(RealmString.class.getSimpleName()));

                oldVersion++;
            }

            if (oldVersion == 2) {
                final RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                articleSchema
                        .addField(Article.FIELD_COMMENTS_URL, String.class);

                oldVersion++;
            }

            if (oldVersion == 3) {
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

            if (oldVersion == 4) {
                final RealmObjectSchema articleSchema = schema.get(Article.class.getSimpleName());
                if (articleSchema != null) {
                    articleSchema
                            .addField(Article.FIELD_IS_IN_OBJECTS_5, long.class);
                }
                oldVersion++;
            }

            if (oldVersion == 5) {
                final RealmObjectSchema leaderboardUserSchema = schema.get(LeaderboardUser.class.getSimpleName());
                if (leaderboardUserSchema != null) {
                    leaderboardUserSchema
                            .removeField(LeaderboardUser.FIELD_UID)
                            .addField(
                                    LeaderboardUser.FIELD_ID,
                                    Long.class,
                                    FieldAttribute.PRIMARY_KEY,
                                    FieldAttribute.INDEXED,
                                    FieldAttribute.REQUIRED
                            );
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