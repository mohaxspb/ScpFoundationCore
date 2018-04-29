package ru.dante.scpfoundation.di;

import javax.inject.Singleton;

import dagger.Component;
import ru.dante.scpfoundation.MyApplicationImpl;
import ru.dante.scpfoundation.api.ApiClientImpl;
import ru.dante.scpfoundation.di.module.HelpersModuleImpl;
import ru.dante.scpfoundation.di.module.NetModuleImpl;
import ru.dante.scpfoundation.di.module.PresentersModuleImpl;
import ru.dante.scpfoundation.di.module.StorageModuleImpl;
import ru.dante.scpfoundation.service.DownloadAllServiceImpl;
import ru.dante.scpfoundation.ui.fragment.ObjectsDeArticlesFragment;
import ru.dante.scpfoundation.ui.fragment.ObjectsEsArticlesFragment;
import ru.dante.scpfoundation.ui.fragment.ObjectsFrArticlesFragment;
import ru.dante.scpfoundation.ui.fragment.ObjectsJpArticlesFragment;
import ru.dante.scpfoundation.ui.fragment.ObjectsPlArticlesFragment;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.di.AppComponent;
import ru.kuchanov.scpcore.di.module.AppModule;
import ru.kuchanov.scpcore.di.module.NotificationModule;

@Singleton
@Component(modules = {
        AppModule.class,
        StorageModuleImpl.class,
        PresentersModuleImpl.class,
        NetModuleImpl.class,
        NotificationModule.class,
        HelpersModuleImpl.class,
})
public interface AppComponentImpl extends AppComponent {

    void inject(DownloadAllServiceImpl service);

    void inject(ObjectsFrArticlesFragment fragment);

    void inject(ObjectsJpArticlesFragment fragment);

    void inject(ObjectsEsArticlesFragment fragment);

    void inject(ObjectsPlArticlesFragment fragment);

    void inject(ObjectsDeArticlesFragment fragment);

    void inject(ApiClientImpl apiClient);
}