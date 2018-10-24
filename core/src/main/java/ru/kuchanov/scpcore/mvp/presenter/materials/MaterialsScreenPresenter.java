package ru.kuchanov.scpcore.mvp.presenter.materials;

import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.mvp.presenter.BaseDrawerPresenter;
import ru.kuchanov.scpcore.mvp.contract.materials.MaterialsScreenMvp;

public class MaterialsScreenPresenter
        extends BaseDrawerPresenter<MaterialsScreenMvp.View>
        implements MaterialsScreenMvp.Presenter {

    public MaterialsScreenPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final InAppHelper inAppHelper
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper);
    }
}