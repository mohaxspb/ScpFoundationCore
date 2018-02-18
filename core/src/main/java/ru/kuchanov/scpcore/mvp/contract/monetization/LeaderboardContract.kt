package ru.kuchanov.scpcore.mvp.contract.monetization

import android.support.v4.app.Fragment
import com.android.vending.billing.IInAppBillingService
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard.LeaderboardUserViewModel
import ru.kuchanov.scpcore.db.model.LeaderboardUser
import ru.kuchanov.scpcore.db.model.User
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
        fun showUpdateDate(lastUpdated: Long)
        fun showUser(myUser: LeaderboardUserViewModel?)
        fun showSwipeRefreshProgress(show: Boolean)
        fun enableSwipeRefresh(enable: Boolean)
        fun showOfferLoginForLevelUpPopup()
    }

    interface Presenter : BaseMvp.Presenter<View> {
        val isDataLoaded: Boolean

        val data: List<MyListItem>
        var users: List<LeaderboardUser>
        var myUser: User?
        var updateTime: Long

        var inAppService: IInAppBillingService?

        fun loadData()
        fun onSubscriptionClick(id: String, target: Fragment, ignoreUserCheck: Boolean = false)
        fun updateLeaderboardFromApi()
    }
}