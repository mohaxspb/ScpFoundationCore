package ru.kuchanov.scpcore.mvp.contract.monetization

import android.support.v4.app.Fragment
import com.android.vending.billing.IInAppBillingService
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.monetization.model.Item
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.InAppHelper
import ru.kuchanov.scpcore.mvp.base.BaseMvp

/**
 * Created by y.kuchanov on 21.12.16.
 *
 *
 * for scp_ru
 */
interface SubscriptionsContract : BaseMvp {
    interface View : BaseMvp.View {
        fun showProgressCenter(show: Boolean)
        fun showRefreshButton(show: Boolean)
        //        fun showData(items: List<MyListItem>)
        fun showData(owned: List<Item>, toBuy: List<Subscription>, inApps: List<Subscription>, curSubsType: Int)

        fun navigateToDisableAds()
    }

    interface Presenter : BaseMvp.Presenter<View> {
        var owned: List<Item>?
        var subsToBuy: List<Subscription>?
        var inAppsToBuy: List<Subscription>?
        @InAppHelper.SubscriptionType
        var type: Int

        fun onSubscriptionClick(id: String, target: Fragment, inAppBillingService: IInAppBillingService)
        fun getMarketData(service: IInAppBillingService)
        var isDataLoaded: Boolean
        fun onCurrentSubscriptionClick(id: String)
//        fun onCurrentSubscriptionEmptyClick(id: String)
    }
}