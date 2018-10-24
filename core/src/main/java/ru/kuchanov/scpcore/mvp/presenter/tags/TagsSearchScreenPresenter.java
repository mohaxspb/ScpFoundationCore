package ru.kuchanov.scpcore.mvp.presenter.tags;

import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.mvp.contract.tags.TagsScreenMvp;
import ru.kuchanov.scpcore.mvp.presenter.BaseDrawerPresenter;

/**
 * Created by y.kuchanov on 21.12.16.
 */
public class TagsSearchScreenPresenter
        extends BaseDrawerPresenter<TagsScreenMvp.View>
        implements TagsScreenMvp.Presenter {

    public TagsSearchScreenPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final InAppHelper inAppHelper
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper);
    }
}