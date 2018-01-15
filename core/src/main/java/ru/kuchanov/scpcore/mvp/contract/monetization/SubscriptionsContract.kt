package ru.kuchanov.scpcore.mvp.contract.monetization

import android.support.v4.app.Fragment
import com.android.vending.billing.IInAppBillingService
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
        fun showData()
    }

    interface Presenter : BaseMvp.Presenter<View> {
        fun onSubscriptionClick(id: String, target: Fragment, inAppBillingService: IInAppBillingService)
        fun getMarketData(service: IInAppBillingService)
        var isDataLoaded:Boolean
    }
}