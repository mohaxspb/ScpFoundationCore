package ru.dante.scpfoundation.mvp.presenter;

import android.support.annotation.NonNull;

import ru.dante.scpfoundation.mvp.contract.ObjectsFrArticles;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.BaseObjectsArticlesPresenter;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for TappAwards
 */
public class ObjectsFrArticlesPresenter
        extends BaseObjectsArticlesPresenter<ObjectsFrArticles.View>
        implements ObjectsFrArticles.Presenter {

    public ObjectsFrArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            @NonNull ConstantValues constantValues
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Override
    protected String getObjectsInDbFieldName() {
        return Article.FIELD_IS_IN_OBJECTS_FR;
    }

    @Override
    protected String getObjectsLink() {
        return mConstantValues.getObjectsFr();
    }
}