package ru.kuchanov.scpcore.db;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import ru.kuchanov.scp.downloads.DbProviderFactoryModel;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
public class DbProviderFactory implements DbProviderFactoryModel {

    private MyPreferenceManager mMyPreferenceManager;
    private ConstantValues mConstantValues;

    public DbProviderFactory(RealmConfiguration realmConfiguration, MyPreferenceManager preferenceManager, ConstantValues constantValues) {
        Realm.setDefaultConfiguration(realmConfiguration);
        mMyPreferenceManager = preferenceManager;
        mConstantValues = constantValues;
    }

    public DbProvider getDbProvider() {
        return new DbProvider(mMyPreferenceManager, mConstantValues);
    }
}