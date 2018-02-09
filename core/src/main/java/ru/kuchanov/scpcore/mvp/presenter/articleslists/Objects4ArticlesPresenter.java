package ru.kuchanov.scpcore.mvp.presenter.articleslists;

import android.support.annotation.NonNull;

import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.contract.articleslists.Objects4Articles;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.BaseObjectsArticlesPresenter;

public class Objects4ArticlesPresenter
        extends BaseObjectsArticlesPresenter<Objects4Articles.View>
        implements Objects4Articles.Presenter {

    public Objects4ArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            @NonNull ConstantValues constantValues
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Override
    protected String getObjectsInDbFieldName() {
        return Article.FIELD_IS_IN_OBJECTS_4;
    }

    @Override
    protected String getObjectsLink() {
        return mConstantValues.getObjects4();
    }
}