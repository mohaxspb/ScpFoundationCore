package ru.kuchanov.scpcore.mvp.presenter.monetization

import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.util.InAppHelper
import ru.kuchanov.scpcore.mvp.base.BasePresenter
import ru.kuchanov.scpcore.mvp.contract.monetization.FreeAdsDisableActionsContract

/**
 * Created by mohax on 13.01.2018.
 *
 * for ScpCore
 */
class FreeAdsDisableActionsPresenter(
        myPreferencesManager: MyPreferenceManager,
        dbProviderFactory: DbProviderFactory,
        apiClient: ApiClient,
        private val inAppHelper: InAppHelper
) : BasePresenter<FreeAdsDisableActionsContract.View>(
        myPreferencesManager,
        dbProviderFactory,
        apiClient
), FreeAdsDisableActionsContract.Presenter {

    override val data = mutableListOf<MyListItem>()

    override fun createData() {
        //todo
    }
}