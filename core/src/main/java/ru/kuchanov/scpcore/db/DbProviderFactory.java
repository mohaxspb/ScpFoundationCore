package ru.kuchanov.scpcore.db;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
public class DbProviderFactory {

    private final MyPreferenceManager mMyPreferenceManager;
    private final ConstantValues mConstantValues;

    public DbProviderFactory(
            final RealmConfiguration realmConfiguration,
            final MyPreferenceManager preferenceManager,
            final ConstantValues constantValues
    ) {
        super();
        Realm.setDefaultConfiguration(realmConfiguration);
        mMyPreferenceManager = preferenceManager;
        mConstantValues = constantValues;
    }

    public DbProvider getDbProvider() {
        return new DbProvider(mMyPreferenceManager, mConstantValues);
    }
}