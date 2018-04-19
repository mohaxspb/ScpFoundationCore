package ru.dante.scpfoundation.service;

import javax.inject.Inject;

import ru.dante.scpfoundation.MyApplicationImpl;
import ru.dante.scpfoundation.R;
import ru.dante.scpfoundation.di.AppComponentImpl;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProvider;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.downloads.DownloadAllService;
import ru.kuchanov.scpcore.downloads.DownloadEntry;
import timber.log.Timber;

/**
 * Created by mohax on 01.07.2017.
 * <p>
 * for ScpFoundationRu
 */
public class DownloadAllServiceImpl extends DownloadAllService {

    @Inject
    ApiClient mApiClient;
    @Inject
    DbProviderFactory mDbProviderFactory;
    @Inject
    ConstantValues mConstantValues;

    @Override
    public void onCreate() {
        super.onCreate();
        ((AppComponentImpl) MyApplicationImpl.getAppComponent()).inject(this);
    }

    @Override
    protected void download(DownloadEntry type) {
        Timber.d("download: %s", type);
        switch (type.resId) {
            case R.string.type_all:
                downloadAll();
                break;
            default:
                downloadObjects(type);
                break;
        }
    }

    @Override
    protected int getNumOfArticlesOnRecentPage() {
        return mConstantValues.getNumOfArticlesOnRecentPage();
    }

    @Override
    public ApiClient getApiClient() {
        return mApiClient;
    }

    @Override
    protected DbProvider getDbProvider() {
        return mDbProviderFactory.getDbProvider();
    }
}