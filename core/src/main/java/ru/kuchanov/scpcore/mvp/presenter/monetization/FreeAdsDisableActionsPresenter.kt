package ru.kuchanov.scpcore.mvp.presenter.monetization

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import org.joda.time.Duration
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.*
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.freeadsdisable.DisableAdsForAuthViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.freeadsdisable.InviteFriendsViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.freeadsdisable.RewardedVideoViewModel
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.util.InAppHelper
import ru.kuchanov.scpcore.mvp.base.BasePresenter
import ru.kuchanov.scpcore.mvp.contract.monetization.FreeAdsDisableActionsContract
import timber.log.Timber

/**
 * Created by mohax on 13.01.2018.
 *
 * for ScpCore
 */
class FreeAdsDisableActionsPresenter(
        val mMyPreferenceManager: MyPreferenceManager,
        dbProviderFactory: DbProviderFactory,
        apiClient: ApiClient,
        private val inAppHelper: InAppHelper,
        private val mGson: Gson
) : BasePresenter<FreeAdsDisableActionsContract.View>(
        mMyPreferenceManager,
        dbProviderFactory,
        apiClient
), FreeAdsDisableActionsContract.Presenter {

    override val data = mutableListOf<MyListItem>()

    override fun createData() {
        Timber.d("createData")

        val context = BaseApplication.getAppInstance()

        val config = FirebaseRemoteConfig.getInstance()
        if (config.getBoolean(FREE_AUTH_ENABLED)
                && FirebaseAuth.getInstance().currentUser == null
                && !mMyPreferenceManager.isUserAwardedFromAuth()) {
            val numOfMillis = config.getLong(AUTH_COOLDOWN_IN_MILLIS)
            val hours = numOfMillis / 1000 / 60 / 60
            val score = config.getLong(SCORE_ACTION_AUTH).toInt()
            data.add(DisableAdsForAuthViewModel(
                    R.string.free_ads_auth_title,
                    context.getString(R.string.free_ads_simple_subtitle, hours, score)
            ))
        }
        if (config.getBoolean(FREE_REWARDED_VIDEO_ENABLED)) {
            val numOfMillis = config.getLong(REWARDED_VIDEO_COOLDOWN_IN_MILLIS)
            val hours = Duration.millis(numOfMillis).toStandardHours().hours
            val score = config.getLong(SCORE_ACTION_REWARDED_VIDEO).toInt()
            data.add(RewardedVideoViewModel(
                    R.string.free_ads_rewarded_video_title,
                    context.getString(R.string.free_ads_simple_subtitle, hours, score)
            ))
        }
        if (config.getBoolean(FREE_INVITES_ENABLED)) {
            //set num of rewarded no ads period and score
            val numOfMillis = config.getLong(INVITE_REWARD_IN_MILLIS)
            val hours = Duration.millis(numOfMillis).toStandardHours().hours
            val score = config.getLong(SCORE_ACTION_INVITE).toInt()
            data.add(InviteFriendsViewModel(
                    R.string.free_ads_invite_title,
                    context.getString(R.string.free_ads_simple_subtitle, hours, score)
            ))
        }
//        if (config.getBoolean(FREE_APPS_INSTALL_ENABLED)) {
//            val jsonString = config.getString(APPS_TO_INSTALL_JSON)
//
//            var applications: List<PlayMarketApplication>? = null
//            try {
//                applications = mGson.fromJson(jsonString, ApplicationsResponse::class.java).items
//            } catch (e: Exception) {
//                Timber.e(e)
//            }
//
//            if (applications != null) {
//                val availableAppsToInstall = mutableListOf<PlayMarketApplication>()
//                for (application in applications) {
//                    if (mMyPreferenceManager.isAppInstalledForPackage(application.id)) {
//                        continue
//                    }
//                    if (IntentUtils.isPackageInstalled(context, application.id)) {
//                        continue
//                    }
//                    availableAppsToInstall.add(application)
//                }
//                if (!availableAppsToInstall.isEmpty()) {
//                    //add row with description
//                    val numOfMillis = config.getLong(APP_INSTALL_REWARD_IN_MILLIS)
//                    val hours = numOfMillis / 1000 / 60 / 60
//                    val score = config.getLong(SCORE_ACTION_OUR_APP).toInt()
//                    data.add(AppInstallHeader(context.getString(R.string.app_install_ads_disable_title, hours, score)))
//                    data.addAll(availableAppsToInstall)
//                }
//            }
//        }
//        if (config.getBoolean(FREE_VK_GROUPS_ENABLED)) {
//            val jsonString = config.getString(VK_GROUPS_TO_JOIN_JSON)
//
//            var items: List<VkGroupToJoin>? = null
//            try {
//                items = mGson.fromJson(jsonString, VkGroupsToJoinResponse::class.java).items
//            } catch (e: Exception) {
//                Timber.e(e)
//            }
//
//            if (items != null) {
//                val availableItems = ArrayList<VkGroupToJoin>()
//                for (item in items) {
//                    if (mMyPreferenceManager.isVkGroupJoined(item.id)) {
//                        continue
//                    }
//                    availableItems.add(item)
//                }
//                if (!availableItems.isEmpty()) {
//                    //add row with description
//                    val numOfMillis = config.getLong(FREE_VK_GROUPS_JOIN_REWARD)
//                    val hours = numOfMillis / 1000 / 60 / 60
//                    val score = config.getLong(SCORE_ACTION_VK_GROUP).toInt()
//                    data.add(AppInstallHeader(context.getString(R.string.vk_group_join_ads_disable_title, hours, score)))
//                    data.addAll(availableItems)
//                }
//            }
//        }
    }

    override fun onInviteFriendsClick() {
        //todo
    }

    override fun onRewardedVideoClick() {
        //todo
    }

    override fun onAuthClick() {
        //todo
    }
}