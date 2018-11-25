package ru.kuchanov.scpcore.mvp.contract.monetization

import com.android.vending.billing.IInAppBillingService
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard.LeaderboardUserViewModel
import ru.kuchanov.scpcore.db.model.User
import ru.kuchanov.scpcore.monetization.model.Subscription
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
        fun resetOnScrollListener()
        fun showBottomProgress(show: Boolean)
        fun showUserPosition(positionInLeaderboard: String?)
    }

    interface Presenter : BaseMvp.Presenter<View> {
        val isDataLoaded: Boolean
        var usersCount: Int
        var data: MutableList<MyListItem>
        var myUser: User?
        var userPositionOnLeaderboard: String?
        var inApps: List<Subscription>
        var inAppService: IInAppBillingService?
        val updateTime: Long

        fun loadInitialData()
        fun updateLeaderboardFromApi(offset: Int, limit: Int = LEADERBOARD_REQUEST_LIMIT)
        fun onRewardedVideoClick()
        fun updateUserPositionOnLeaderboard()
    }
}

const val LEADERBOARD_REQUEST_LIMIT = 100