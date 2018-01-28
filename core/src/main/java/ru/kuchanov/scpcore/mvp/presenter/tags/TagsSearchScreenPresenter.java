package ru.kuchanov.scpcore.mvp.presenter.tags;

import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.presenter.BaseDrawerPresenter;
import ru.kuchanov.scpcore.mvp.contract.tags.TagsScreenMvp;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for TappAwards
 */
public class TagsSearchScreenPresenter
        extends BaseDrawerPresenter<TagsScreenMvp.View>
        implements TagsScreenMvp.Presenter {

    public TagsSearchScreenPresenter(MyPreferenceManager myPreferencesManager, DbProviderFactory dbProviderFactory, ApiClient apiClient) {
        super(myPreferencesManager, dbProviderFactory, apiClient);
    }
}