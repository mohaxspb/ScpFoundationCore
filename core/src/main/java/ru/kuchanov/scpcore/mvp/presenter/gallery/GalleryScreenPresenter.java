package ru.kuchanov.scpcore.mvp.presenter.gallery;

import java.util.List;

import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.gallery.GalleryImage;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.mvp.contract.GalleryScreenMvp;
import ru.kuchanov.scpcore.mvp.presenter.BaseDrawerPresenter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class GalleryScreenPresenter
        extends BaseDrawerPresenter<GalleryScreenMvp.View>
        implements GalleryScreenMvp.Presenter {

    private List<GalleryImage> mData;

    public GalleryScreenPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final InAppHelper inAppHelper
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper);
    }

    @Override
    public void updateData() {
        mApiClient.getGallery()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(vkImages -> mDbProviderFactory.getDbProvider().saveImages(vkImages))
                .subscribe(
                        vkImages -> Timber.d("updateData onNext: %s", vkImages),
                        error -> Timber.e(error, "error while updateData")
                );
    }

    @Override
    public void getDataFromDb() {
        Timber.d("getCachedData");
        getView().showCenterProgress(true);
        getView().showEmptyPlaceholder(false);

        mDbProviderFactory.getDbProvider().getGalleryImages().subscribe(
                data -> {
                    Timber.d("getDataFromCache onNext: %s", data.size());

                    mData = data;

                    if (data.isEmpty()) {
                        //load from API
                        Timber.d("no data in DB, so load from api");
                        updateData();
                    } else {
                        getView().showCenterProgress(false);
                        getView().showEmptyPlaceholder(false);

                        getView().showData(data);
                    }
                }
        );
    }

    @Override
    public void setData(final List<GalleryImage> data) {
        mData = data;
    }

    @Override
    public List<GalleryImage> getData() {
        return mData;
    }
}