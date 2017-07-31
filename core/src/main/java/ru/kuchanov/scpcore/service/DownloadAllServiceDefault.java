package ru.kuchanov.scpcore.service;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ru.kuchanov.scp.downloads.ApiClientModel;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scp.downloads.DbProviderModel;
import ru.kuchanov.scp.downloads.DownloadAllService;
import ru.kuchanov.scp.downloads.DownloadEntry;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;

/**
 * Created by mohax on 01.07.2017.
 * <p>
 * for ScpFoundationRu
 */
public class DownloadAllServiceDefault extends DownloadAllService<Article> {

    @Inject
    MyPreferenceManager mMyPreferenceManager;
    @Inject
    ApiClient mApiClient;
    @Inject
    DbProviderFactory mDbProviderFactory;
    @Inject
    ConstantValues mConstantValues;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    public ApiClientModel<Article> getApiClient() {
        return mApiClient;
    }

    @Override
    protected void download(DownloadEntry type) {
        switch (type.resId) {
            case R2.string.type_all:
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
    protected DbProviderModel<Article> getDbProviderModel() {
        return mDbProviderFactory.getDbProvider();
    }
}