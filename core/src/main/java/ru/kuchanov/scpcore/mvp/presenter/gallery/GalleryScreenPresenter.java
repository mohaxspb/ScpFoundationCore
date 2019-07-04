package ru.kuchanov.scpcore.mvp.presenter.gallery;

import java.util.List;

import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.gallery.GalleryImage;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.mvp.contract.GalleryScreenMvp;
import ru.kuchanov.scpcore.mvp.presenter.BaseDrawerPresenter;

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
    public void setData(final List<GalleryImage> data) {
        mData = data;
    }

    @Override
    public List<GalleryImage> getData() {
        return mData;
    }
}
