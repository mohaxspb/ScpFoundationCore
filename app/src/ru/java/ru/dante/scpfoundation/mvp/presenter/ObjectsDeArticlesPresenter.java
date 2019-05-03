package ru.dante.scpfoundation.mvp.presenter;

import android.support.annotation.NonNull;

import ru.dante.scpfoundation.mvp.contract.ObjectsDeArticles;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.BaseObjectsArticlesPresenter;

public class ObjectsDeArticlesPresenter
        extends BaseObjectsArticlesPresenter<ObjectsDeArticles.View>
        implements ObjectsDeArticles.Presenter {

    public ObjectsDeArticlesPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            @NonNull final ConstantValues constantValues,
            final InAppHelper inAppHelper
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, constantValues, inAppHelper);
    }

    @Override
    protected String getObjectsInDbFieldName() {
        return Article.FIELD_IS_IN_OBJECTS_DE;
    }

    @Override
    protected String getObjectsLink() {
        return mConstantValues.getObjectsDe();
    }
}