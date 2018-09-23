package ru.dante.scpfoundation.di.module;

import java.util.Locale;

import dagger.Module;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import ru.kuchanov.scpcore.db.model.Article;
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