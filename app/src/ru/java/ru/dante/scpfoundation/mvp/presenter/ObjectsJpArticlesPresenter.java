package ru.dante.scpfoundation.mvp.presenter;

import android.support.annotation.NonNull;

import ru.dante.scpfoundation.mvp.contract.ObjectsJpArticles;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.BaseObjectsArticlesPresenter;

public class ObjectsJpArticlesPresenter
        extends BaseObjectsArticlesPresenter<ObjectsJpArticles.View>
        implements ObjectsJpArticles.Presenter {

    public ObjectsJpArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            @NonNull ConstantValues constantValues
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Override
    protected String getObjectsInDbFieldName() {
        return Article.FIELD_IS_IN_OBJECTS_JP;
    }

    @Override
    protected String getObjectsLink() {
        return mConstantValues.getObjectsJp();
    }
}