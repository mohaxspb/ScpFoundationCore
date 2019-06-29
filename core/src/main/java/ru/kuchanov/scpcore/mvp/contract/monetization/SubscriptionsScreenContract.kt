package ru.kuchanov.scpcore.mvp.contract.monetization

import ru.kuchanov.scpcore.mvp.base.BaseActivityMvp
import ru.kuchanov.scpcore.mvp.contract.ActivityToolbarStateSetter

/**
 * Created by y.kuchanov on 21.12.16.
 */
interface SubscriptionsScreenContract : BaseActivityMvp {

    interface View : BaseActivityMvp.View, ActivityToolbarStateSetter {
        fun showScreen(screen: Screen)
    }

    interface Presenter : BaseActivityMvp.Presenter<View> {

        fun showSubscriptionsScreen()
        fun showDisableAdsForFreeScreen()
        fun showLeaderboardScreen()
    }

    enum class Screen {
        SUBS, FREE_ACTIONS, LEADERBOARD
    }
}