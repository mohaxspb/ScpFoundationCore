package ru.dante.scpfoundation;

import android.content.pm.PackageManager;

import ru.dante.scpfoundation.di.DaggerAppComponentImpl;
import ru.dante.scpfoundation.di.module.HelpersModuleImpl;
import ru.dante.scpfoundation.di.module.NetModuleImpl;
import ru.dante.scpfoundation.di.module.StorageModuleImpl;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.di.AppComponent;
import ru.kuchanov.scpcore.di.module.AppModule;
import ru.kuchanov.scpcore.util.SystemUtils;
import timber.log.Timber;

/**
 * Created by mohax on 01.01.2017.
 * <p>
 * for scp_ru
 */
public class MyApplicationImpl extends BaseApplication {

    //TODO create getter for appCode and name
    @Override
    public void onCreate() {
        super.onCreate();

        try {
            String packageName = getPackageName();
            Timber.d("packageName: %s", packageName);
            long versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            Timber.d("versionCode: %s", versionCode);
            Timber.d("versionCode: %s", BuildConfig.);
            Timber.d("SystemUtils.getPackageInfo().versionCode: %s", SystemUtils.getPackageInfo().versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected AppComponent buildAppComponentImpl() {
        return DaggerAppComponentImpl.builder()
                .storageModule(new StorageModuleImpl())
                .appModule(new AppModule(this))
                .netModule(new NetModuleImpl())
                .helpersModule(new HelpersModuleImpl())
                .build();
    }
}