package ru.dante.scpfoundation.di.module;

import java.util.Locale;

import dagger.Module;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import ru.kuchanov.scpcore.db.model.ReadHistoryTransaction;
import ru.kuchanov.scpcore.db.model.LeaderboardUser;
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
            final RealmSchema schema = realm.getSchema();

            Timber.d("providesRealmMigration: %s/%s", oldVersion, newVersion);

            if (oldVersion == 1) {
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

            if (oldVersion == 2) {
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

            if (oldVersion == 3) {
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