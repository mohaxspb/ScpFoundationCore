package ru.kuchanov.scpcore.mvp.contract.monetization

import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.mvp.base.BaseMvp
import ru.kuchanov.scpcore.mvp.contract.FragmentToolbarStateSetter

/**
 * Created by y.kuchanov on 21.12.16.
 *
 *
 * for scp_ru
 */
interface FreeAdsDisableActionsContract : BaseMvp {

    interface View : BaseMvp.View, FragmentToolbarStateSetter {
        fun showData(data: List<MyListItem>)
        fun onInviteFriendsClick()
        fun onRewardedVideoClick()
        fun onAuthClick()
        fun onAppInstallClick(id: String)
        fun onVkLoginAttempt()
        fun showVkShareDialog()
    }

    interface Presenter : BaseMvp.Presenter<View> {
        val data: List<MyListItem>

        fun createData()
        fun onInviteFriendsClick()
        fun onRewardedVideoClick()
        fun onAuthClick()
        fun onAppInstallClick(id: String)
        fun onVkGroupClick(id: String)
        fun onVkShareAppClick()
        fun updateUserScoreForVkAppSahre()
        fun onDestroy()
    }
}