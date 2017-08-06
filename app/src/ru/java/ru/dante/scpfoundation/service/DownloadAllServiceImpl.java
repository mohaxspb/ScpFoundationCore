package ru.dante.scpfoundation.service;

import javax.inject.Inject;

import ru.dante.scpfoundation.MyApplicationImpl;
import ru.dante.scpfoundation.R;
import ru.dante.scpfoundation.di.AppComponentImpl;
import ru.kuchanov.scp.downloads.ApiClientModel;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scp.downloads.DbProviderModel;
import ru.kuchanov.scp.downloads.DownloadAllService;
import ru.kuchanov.scp.downloads.DownloadEntry;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import timber.log.Timber;

/**
 * Created by mohax on 01.07.2017.
 * <p>
 * for ScpFoundationRu
 */
public class DownloadAllServiceImpl extends DownloadAllService<Article> {

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
    public ApiClientModel<Article> getApiClient() {
        return mApiClient;
    }

    @Override
    protected DbProviderModel<Article> getDbProviderModel() {
        return mDbProviderFactory.getDbProvider();
    }
}