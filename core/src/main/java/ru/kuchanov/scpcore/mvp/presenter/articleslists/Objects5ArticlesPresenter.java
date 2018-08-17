package ru.kuchanov.scpcore.mvp.presenter.articleslists;

import android.support.annotation.NonNull;

import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.contract.articleslists.Objects5Articles;

public class Objects5ArticlesPresenter
        extends BaseObjectsArticlesPresenter<Objects5Articles.View>
        implements Objects5Articles.Presenter {

    public Objects5ArticlesPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            @NonNull final ConstantValues constantValues
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Override
    protected String getObjectsInDbFieldName() {
        return Article.FIELD_IS_IN_OBJECTS_5;
    }

    @Override
    protected String getObjectsLink() {
        return mConstantValues.getObjects5();
    }
}