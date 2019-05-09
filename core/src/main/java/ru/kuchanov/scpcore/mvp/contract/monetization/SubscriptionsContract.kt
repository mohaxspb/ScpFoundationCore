package ru.kuchanov.scpcore.mvp.contract.monetization

import ru.kuchanov.scpcore.monetization.model.Item
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.InappPurchaseUtil
import ru.kuchanov.scpcore.mvp.base.BaseMvp
import ru.kuchanov.scpcore.mvp.contract.FragmentToolbarStateSetter

/**
 * Created by y.kuchanov on 21.12.16.
 *
 *
 * for scp_ru
 */
interface SubscriptionsContract : BaseMvp {
    interface View : BaseMvp.View, FragmentToolbarStateSetter {
        fun showProgressCenter(show: Boolean)
        fun showRefreshButton(show: Boolean)
        fun showData(owned: List<Item>, toBuy: List<Subscription>, inApps: List<Subscription>, curSubsType: Int)

        fun navigateToDisableAds()
        fun navigateToLeaderboard()
    }

    interface Presenter : BaseMvp.Presenter<View> {
        var owned: List<Item>?
        var subsToBuy: List<Subscription>?
        var inAppsToBuy: List<Subscription>?
        @InappPurchaseUtil.SubscriptionType
        var type: Int

        fun getMarketData()
        var isDataLoaded: Boolean
        fun onCurrentSubscriptionClick(id: String)
    }
}