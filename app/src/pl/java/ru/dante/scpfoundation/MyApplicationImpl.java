package ru.dante.scpfoundation;

import ru.dante.scpfoundation.di.DaggerAppComponentImpl;
import ru.dante.scpfoundation.di.module.HelpersModuleImpl;
import ru.dante.scpfoundation.di.module.NetModuleImpl;
import ru.dante.scpfoundation.di.module.StorageModuleImpl;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.di.AppComponent;
import ru.kuchanov.scpcore.di.module.AppModule;

/**
 * Created by mohax on 01.01.2017.
 * <p>
 * for scp_ru
 */
public class MyApplicationImpl extends BaseApplication {

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