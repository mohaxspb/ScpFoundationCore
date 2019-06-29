package ru.kuchanov.scpcore.mvp.presenter.materials;

import android.support.annotation.NonNull;

import java.util.List;

import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.mvp.contract.materials.MaterialsExperimentsMvp;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.BaseObjectsArticlesPresenter;
import rx.Single;

/**
 * Created by y.kuchanov on 21.12.16.
 */
public class MaterialsExperimentsPresenter
        extends BaseObjectsArticlesPresenter<MaterialsExperimentsMvp.View>
        implements MaterialsExperimentsMvp.Presenter {

    public MaterialsExperimentsPresenter(
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
        return Article.FIELD_IS_IN_EXPERIMETS;
    }

    @Override
    protected String getObjectsLink() {
        return mConstantValues.getExperiments();
    }

    @Override
    protected Single<List<Article>> getApiObservable(final int offset) {
        return mApiClient.getMaterialsArticles(getObjectsLink());
    }
}