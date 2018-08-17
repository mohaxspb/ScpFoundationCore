package ru.dante.scpfoundation.mvp.presenter;

import android.support.annotation.NonNull;

import ru.dante.scpfoundation.mvp.contract.ObjectsEsArticles;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.BaseObjectsArticlesPresenter;

public class ObjectsEsArticlesPresenter
        extends BaseObjectsArticlesPresenter<ObjectsEsArticles.View>
        implements ObjectsEsArticles.Presenter {

    public ObjectsEsArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            @NonNull ConstantValues constantValues
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Override
    protected String getObjectsInDbFieldName() {
        return Article.FIELD_IS_IN_OBJECTS_ES;
    }

    @Override
    protected String getObjectsLink() {
        return mConstantValues.getObjectsEs();
    }
}