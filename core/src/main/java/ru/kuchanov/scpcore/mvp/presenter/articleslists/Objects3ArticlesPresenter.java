package ru.kuchanov.scpcore.mvp.presenter.articleslists;

import android.support.annotation.NonNull;

import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.mvp.contract.articleslists.Objects3Articles;

public class Objects3ArticlesPresenter
        extends BaseObjectsArticlesPresenter<Objects3Articles.View>
        implements Objects3Articles.Presenter {

    public Objects3ArticlesPresenter(
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
        return Article.FIELD_IS_IN_OBJECTS_3;
    }

    @Override
    protected String getObjectsLink() {
        return mConstantValues.getObjects3();
    }
}