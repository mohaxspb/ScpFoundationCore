package ru.kuchanov.scpcore.mvp.presenter


import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper
import ru.kuchanov.scpcore.mvp.contract.MainMvp

class MainPresenter(
        myPreferencesManager: MyPreferenceManager,
        dbProviderFactory: DbProviderFactory,
        apiClient: ApiClient,
        inAppHelper: InAppHelper
) : BaseDrawerPresenter<MainMvp.View>(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper), MainMvp.Presenter