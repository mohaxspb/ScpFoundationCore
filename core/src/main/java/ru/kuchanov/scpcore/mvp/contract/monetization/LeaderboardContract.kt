package ru.kuchanov.scpcore.mvp.contract.monetization

import com.android.vending.billing.IInAppBillingService
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.mvp.base.BaseMvp
import ru.kuchanov.scpcore.mvp.contract.FragmentToolbarStateSetter

/**
 * Created by y.kuchanov on 21.12.16.
 *
 *
 * for scp_ru
 */
interface LeaderboardContract : BaseMvp {

    interface View : BaseMvp.View, FragmentToolbarStateSetter {
        fun showProgressCenter(show: Boolean)
        fun showData(data: List<MyListItem>)
        fun onRewardedVideoClick()
        fun showRefreshButton(show: Boolean)
    }

    interface Presenter : BaseMvp.Presenter<View> {
        val isDataLoaded: Boolean

        val data: List<MyListItem>

        fun onRewardedVideoClick()
        fun loadData(service: IInAppBillingService)
    }
}