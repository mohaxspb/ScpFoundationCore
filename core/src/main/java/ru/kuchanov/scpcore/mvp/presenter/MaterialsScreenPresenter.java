package ru.kuchanov.scpcore.mvp.presenter;

import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.base.BaseDrawerPresenter;
import ru.kuchanov.scpcore.mvp.contract.MaterialsScreenMvp;

public class MaterialsScreenPresenter
        extends BaseDrawerPresenter<MaterialsScreenMvp.View>
        implements MaterialsScreenMvp.Presenter {

    public MaterialsScreenPresenter(MyPreferenceManager myPreferencesManager, DbProviderFactory dbProviderFactory, ApiClient apiClient) {
        super(myPreferencesManager, dbProviderFactory, apiClient);
    }
}