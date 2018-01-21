package ru.kuchanov.scpcore.mvp.contract.monetization

import ru.kuchanov.scpcore.mvp.base.BaseActivityMvp

/**
 * Created by y.kuchanov on 21.12.16.
 *
 * for scp_ru
 */
interface SubscriptionsScreenContract : BaseActivityMvp {

    interface View : BaseActivityMvp.View {
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