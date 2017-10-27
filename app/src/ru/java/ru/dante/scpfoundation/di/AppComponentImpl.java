package ru.dante.scpfoundation.di;

import javax.inject.Singleton;

import dagger.Component;
import ru.dante.scpfoundation.di.module.HelpersModuleImpl;
import ru.dante.scpfoundation.di.module.NetModuleImpl;
import ru.dante.scpfoundation.di.module.StorageModuleImpl;
import ru.dante.scpfoundation.service.DownloadAllServiceImpl;
import ru.dante.scpfoundation.ui.fragment.ObjectsFrArticlesFragment;
import ru.kuchanov.scpcore.di.AppComponent;
import ru.kuchanov.scpcore.di.module.AppModule;
import ru.kuchanov.scpcore.di.module.NotificationModule;
import ru.kuchanov.scpcore.di.module.PresentersModule;

@Singleton
@Component(modules = {
        AppModule.class,
        StorageModuleImpl.class,
        PresentersModule.class,
        NetModuleImpl.class,
        NotificationModule.class,
        HelpersModuleImpl.class,
})
public interface AppComponentImpl extends AppComponent {

    void inject(DownloadAllServiceImpl service);

    void inject(ObjectsFrArticlesFragment fragment);
}