package ru.kuchanov.scpcore.mvp.presenter.monetization

import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsScreenContract
import ru.kuchanov.scpcore.mvp.presenter.BaseActivityPresenter

class SubscriptionsScreenPresenter(
        myPreferencesManager: MyPreferenceManager,
        dbProviderFactory: DbProviderFactory,
        apiClient: ApiClient,
        inAppHelper: InAppHelper
) : BaseActivityPresenter<SubscriptionsScreenContract.View>(
        myPreferencesManager,
        dbProviderFactory,
        apiClient,
        inAppHelper
), SubscriptionsScreenContract.Presenter {

    override fun showSubscriptionsScreen() = view.showScreen(SubscriptionsScreenContract.Screen.SUBS)

    override fun showDisableAdsForFreeScreen() = view.showScreen(SubscriptionsScreenContract.Screen.FREE_ACTIONS)

    override fun showLeaderboardScreen() = view.showScreen(SubscriptionsScreenContract.Screen.LEADERBOARD)
}