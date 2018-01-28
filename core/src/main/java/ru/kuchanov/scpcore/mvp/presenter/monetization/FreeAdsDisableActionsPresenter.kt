package ru.kuchanov.scpcore.mvp.presenter.monetization

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKScope
import com.vk.sdk.VKSdk
import org.joda.time.Duration
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.*
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.controller.adapter.viewmodel.DividerViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.LabelViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.freeadsdisable.*
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.manager.MyNotificationManager
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.model.ApplicationsResponse
import ru.kuchanov.scpcore.monetization.model.PlayMarketApplication
import ru.kuchanov.scpcore.monetization.model.VkGroupToJoin
import ru.kuchanov.scpcore.monetization.model.VkGroupsToJoinResponse
import ru.kuchanov.scpcore.mvp.base.BasePresenter
import ru.kuchanov.scpcore.mvp.contract.monetization.FreeAdsDisableActionsContract
import ru.kuchanov.scpcore.util.DimensionUtils
import ru.kuchanov.scpcore.util.IntentUtils
import rx.lang.kotlin.subscribeBy
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
        private val mGson: Gson,
        private val mMyNotificationManager: MyNotificationManager
) : BasePresenter<FreeAdsDisableActionsContract.View>(
        mMyPreferenceManager,
        dbProviderFactory,
        apiClient
), FreeAdsDisableActionsContract.Presenter {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val authStateListener: FirebaseAuth.AuthStateListener = FirebaseAuth.AuthStateListener {
        Timber.d("stateChanged: ${it.currentUser}")
        createData()
        view.showData(data)
    }

    override val data = mutableListOf<MyListItem>()

    override fun onCreate() {
        super.onCreate()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onDestroy() = firebaseAuth.removeAuthStateListener(authStateListener)

    override fun createData() {
        val context = BaseApplication.getAppInstance()
        val config = FirebaseRemoteConfig.getInstance()

        data.clear()

        data.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(12)))

        if (config.getBoolean(FREE_AUTH_ENABLED)
                && FirebaseAuth.getInstance().currentUser == null
                && !mMyPreferenceManager.isUserAwardedFromAuth()) {
            val numOfMillis = config.getLong(AUTH_COOLDOWN_IN_MILLIS)
            val hours = Duration.millis(numOfMillis).toStandardHours().hours
            val score = config.getLong(SCORE_ACTION_AUTH).toInt()
            data.add(DisableAdsForAuthViewModel(
                    R.string.free_ads_auth_title,
                    context.getString(R.string.free_ads_simple_subtitle, hours, score)
            ))
            data.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(4)))
        }
        if (config.getBoolean(FREE_REWARDED_VIDEO_ENABLED)) {
            val numOfMillis = config.getLong(REWARDED_VIDEO_COOLDOWN_IN_MILLIS)
            val hours = Duration.millis(numOfMillis).toStandardHours().hours
            val score = config.getLong(SCORE_ACTION_REWARDED_VIDEO).toInt()
            data.add(RewardedVideoViewModel(
                    R.string.free_ads_rewarded_video_title,
                    context.getString(R.string.free_ads_simple_subtitle, hours, score)
            ))
            data.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(4)))
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
            data.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(4)))
        }
        if (config.getBoolean(FREE_APPS_INSTALL_ENABLED)) {
            val jsonString = config.getString(APPS_TO_INSTALL_JSON)

            var applications: List<PlayMarketApplication>? = null
            try {
                applications = mGson.fromJson(jsonString, ApplicationsResponse::class.java).items
            } catch (e: Exception) {
                Timber.e(e)
            }

            if (applications != null) {
                val availableAppsToInstall = mutableListOf<PlayMarketApplication>()
                for (application in applications) {
                    if (mMyPreferenceManager.isAppInstalledForPackage(application.id)) {
                        continue
                    }
                    if (IntentUtils.isPackageInstalled(context, application.id)) {
                        continue
                    }
                    availableAppsToInstall.add(application)
                }
                if (!availableAppsToInstall.isEmpty()) {
                    val numOfMillis = config.getLong(APP_INSTALL_REWARD_IN_MILLIS)
                    val hours = Duration.millis(numOfMillis).toStandardHours().hours
                    val score = config.getLong(SCORE_ACTION_OUR_APP).toInt()

                    data.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(4)))
                    data.add(LabelViewModel(R.string.free_ads_app_install_label, R.color.freeAdsBackgroundColor, R.color.freeAdsTextColor))
                    data.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(4)))

                    data.addAll(availableAppsToInstall.map {
                        AppToInstallViewModel(
                                it.id,
                                context.getString(R.string.free_ads_app_install_title, hours, score),
                                it.name,
                                it.imageUrl
                        )
                    })
                }
            }
        }
        if (config.getBoolean(FREE_VK_GROUPS_ENABLED)) {
            val jsonString = config.getString(VK_GROUPS_TO_JOIN_JSON)

            var items: List<VkGroupToJoin>? = null
            try {
                items = mGson.fromJson(jsonString, VkGroupsToJoinResponse::class.java).items
            } catch (e: Exception) {
                Timber.e(e)
            }

            if (items != null) {
                val availableItems = ArrayList<VkGroupToJoin>()
                items.filterNotTo(availableItems) { mMyPreferenceManager.isVkGroupJoined(it.id) }
                if (availableItems.isNotEmpty()) {
                    val numOfMillis = config.getLong(FREE_VK_GROUPS_JOIN_REWARD)
                    val hours = Duration.millis(numOfMillis).toStandardHours().hours
                    val score = config.getLong(SCORE_ACTION_VK_GROUP).toInt()

                    data.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(4)))
                    data.add(LabelViewModel(R.string.free_ads_vk_group_label, R.color.freeAdsBackgroundColor, R.color.freeAdsTextColor))
                    data.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(4)))

                    data.addAll(availableItems.map {
                        VkGroupToJoinViewModel(
                                it.id,
                                context.getString(R.string.free_ads_vk_group_title, hours, score),
                                it.name,
                                it.imageUrl
                        )
                    })
                }
            }
        }
        data.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(12)))
    }

    override fun onInviteFriendsClick() {
        Timber.d("onInviteFriendsClick: ${isNeedToOfferFreeTrial()}")
        if (isNeedToOfferFreeTrial()) {
            return
        }
        view.onInviteFriendsClick()
    }

    override fun onRewardedVideoClick() {
        if (isNeedToOfferFreeTrial()) {
            return
        }
        view.onRewardedVideoClick()
    }

    override fun onAuthClick() {
        if (isNeedToOfferFreeTrial()) {
            return
        }
        view.onAuthClick()
    }

    override fun onAppInstallClick(id: String) {
        if (isNeedToOfferFreeTrial()) {
            return
        }
        view.onAppInstallClick(id)
    }

    override fun onVkGroupClick(id: String) {
        if (isNeedToOfferFreeTrial()) {
            return
        }
        if (!VKSdk.isLoggedIn()) {
            view.onVkLoginAttempt()
            return
        } else if (!VKAccessToken.currentToken().hasScope(VKScope.GROUPS)) {
            view.showMessage(R.string.need_vk_group_access)
            return
        }

        val context: Context = BaseApplication.getAppInstance()

        mApiClient.joinVkGroup(id).toSingle().subscribeBy(
                onSuccess = {
                    if (it) {
                        Timber.d("Successful group join")
                        mMyPreferenceManager.setVkGroupJoined(id)
                        mMyPreferenceManager.applyAwardVkGroupJoined()

                        val numOfMillis = FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.FREE_VK_GROUPS_JOIN_REWARD)
                        val hours = Duration.millis(numOfMillis).toStandardHours().hours

                        mMyNotificationManager.showNotificationSimple(
                                context.getString(R.string.ads_reward_gained, hours),
                                context.getString(R.string.thanks_for_supporting_us),
                                NOTIFICATION_ID
                        )

//                        data.remove(data.find { it is VkGroupToJoinViewModel && it.id == id })
                        createData()
                        view.showData(data)

                        val bundle = Bundle()
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "group_" + id)
                        FirebaseAnalytics.getInstance(context).logEvent(Constants.Firebase.Analitics.EventName.VK_GROUP_JOINED, bundle)

                        updateUserScoreForVkGroup(id)
                    } else {
                        Timber.e("error group join")
                        view.showMessage("error group join")
                    }
                },
                onError = {
                    Timber.e(it, "error while join group")
                    view.showError(it)
                }
        )
    }

    /**
     * check if its time to offer free trial subscription
     */
    private fun isNeedToOfferFreeTrial(): Boolean {
        val hasSubscription = mMyPreferenceManager.isHasSubscription || mMyPreferenceManager.isHasNoAdsSubscription
        if (!hasSubscription && mMyPreferenceManager.isTimeOfferFreeTrialFromDisableAdsOption) {
            val bundle = Bundle()
            bundle.putString(Constants.Firebase.Analitics.EventParam.PLACE, Constants.Firebase.Analitics.EventValue.ADS_DISABLE)
            FirebaseAnalytics.getInstance(BaseApplication.getAppInstance()).logEvent(Constants.Firebase.Analitics.EventName.FREE_TRIAL_OFFER_SHOWN, bundle)

            mMyPreferenceManager.setFreeAdsDisableRewardGainedCount(0)
            view.showOfferFreeTrialSubscriptionPopup()
            return true
        } else {
            return false
        }
    }

    companion object {

        private val NOTIFICATION_ID = 103
    }
}