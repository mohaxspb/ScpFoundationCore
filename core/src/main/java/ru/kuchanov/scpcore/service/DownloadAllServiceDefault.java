package ru.kuchanov.scpcore.service;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProvider;
import ru.kuchanov.scpcore.downloads.DownloadAllService;
import ru.kuchanov.scpcore.downloads.DownloadEntry;

/**
 * Created by mohax on 01.07.2017.
 * <p>
 * for ScpFoundationRu
 */
public class DownloadAllServiceDefault extends DownloadAllService {

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    protected void callInject() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    public ApiClient getApiClient() {
        return mApiClient;
    }

    @Override
    protected void download(final DownloadEntry type) {
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
    protected DbProvider getDbProvider() {
        return mDbProviderFactory.getDbProvider();
    }
}