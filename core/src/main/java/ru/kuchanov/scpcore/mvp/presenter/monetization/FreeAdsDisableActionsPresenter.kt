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
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.freeadsdisable.DisableAdsForAuthViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.freeadsdisable.RewardedVideoViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.freeadsdisable.VkGroupToJoinViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.freeadsdisable.VkShareAppViewModel
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.manager.MyNotificationManager
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.model.VkGroupToJoin
import ru.kuchanov.scpcore.monetization.model.VkGroupsToJoinResponse
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper
import ru.kuchanov.scpcore.mvp.base.BasePresenter
import ru.kuchanov.scpcore.mvp.contract.monetization.FreeAdsDisableActionsContract
import ru.kuchanov.scpcore.util.DimensionUtils
import ru.kuchanov.scpcore.util.SystemUtils
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
        private val mMyNotificationManager: MyNotificationManager,
        inAppHelper: InAppHelper
) : BasePresenter<FreeAdsDisableActionsContract.View>(
        mMyPreferenceManager,
        dbProviderFactory,
        apiClient,
        inAppHelper
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
                && !mMyPreferenceManager.isUserAwardedFromAuth) {
            val numOfMillis = config.getLong(AUTH_COOLDOWN_IN_MILLIS)
            val hours = Duration.millis(numOfMillis).toStandardHours().hours
            val score = config.getLong(SCORE_ACTION_AUTH).toInt()
            data.add(
                    DisableAdsForAuthViewModel(
                            R.string.free_ads_auth_title,
                            context.getString(R.string.free_ads_simple_subtitle, hours, score)
                    ))
            data.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(4)))
        }
        if (config.getBoolean(FREE_REWARDED_VIDEO_ENABLED)) {
            val numOfMillis = config.getLong(REWARDED_VIDEO_COOLDOWN_IN_MILLIS)
            val hours = Duration.millis(numOfMillis).toStandardHours().hours
            val score = config.getLong(SCORE_ACTION_REWARDED_VIDEO).toInt()
            data.add(
                    RewardedVideoViewModel(
                            R.string.free_ads_rewarded_video_title,
                            context.getString(R.string.free_ads_simple_subtitle, hours, score)
                    ))
            data.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(4)))
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
                    Timber.d("numOfMillis: $numOfMillis")
                    val hours = Duration.millis(numOfMillis).toStandardHours().hours
                    Timber.d("hours: $hours")
                    val score = config.getLong(SCORE_ACTION_VK_GROUP).toInt()

                    data.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(4)))
                    data.add(
                            LabelViewModel(
                                    R.string.free_ads_vk_group_label,
                                    bgColor = R.color.freeAdsBackgroundColor,
                                    textColor = R.color.freeAdsTextColor
                            )
                    )
                    data.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(4)))

                    data.addAll(
                            availableItems.map {
//                                Timber.d("VkGroupToJoinViewModel: $it")
                                VkGroupToJoinViewModel(
                                        it.id,
                                        it.name,
                                        context.getString(R.string.free_ads_vk_group_title, hours, score),
                                        it.imageUrl
                                )
                            }
                    )
                }
            }
        }

        if (config.getBoolean(FREE_VK_SHARE_APP_ENABLED) && !mMyPreferenceManager.isVkAppShared) {
            val numOfMillis = config.getLong(FREE_VK_SHARE_APP_REWARD)
            val hours = Duration.millis(numOfMillis).toStandardHours().hours
            val score = config.getLong(SCORE_ACTION_VK_SHARE_APP).toInt()

            data.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(4)))

            data.add(
                    LabelViewModel(
                            R.string.free_ads_vk_share_label,
                            bgColor = R.color.freeAdsBackgroundColor,
                            textColor = R.color.freeAdsTextColor))
            data.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(4)))

            data.add(
                    VkShareAppViewModel(
                            SystemUtils.getPackageInfo().packageName,
                            context.getString(R.string.app_name),
                            context.getString(R.string.free_ads_vk_group_title, hours, score),
                            "https://lh3.googleusercontent.com//nxy_ouZM-1PTsve_PXDI9-CoErm1Q2XRwKML7_967K-eR5TmVlI5RHDUJsc4WhjsLaI=w300-rw"
                    ))
        }
        data.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(12)))
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

                        val numOfMillis = FirebaseRemoteConfig.getInstance().getLong(FREE_VK_GROUPS_JOIN_REWARD)
                        val hours = Duration.millis(numOfMillis).toStandardHours().hours

                        mMyNotificationManager.showNotificationSimple(
                                context.getString(R.string.ads_reward_gained, hours),
                                context.getString(R.string.thanks_for_supporting_us),
                                NOTIFICATION_ID
                        )

                        createData()
                        view.showData(data)

                        val bundle = Bundle()
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "group_$id")
                        FirebaseAnalytics.getInstance(context).logEvent(
                                Constants.Firebase.Analytics.EventName.VK_GROUP_JOINED,
                                bundle
                        )

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

    override fun onVkShareAppClick() {
        if (isNeedToOfferFreeTrial()) {
            return
        }
        if (!VKSdk.isLoggedIn()) {
            view.onVkLoginAttempt()
            return
        } else if (!VKAccessToken.currentToken().hasScope(VKScope.WALL)) {
            view.showMessage(R.string.need_vk_group_access)
            return
        }

        view.showVkShareDialog()
    }

    override fun applyAwardFromVkShare() {
        mMyPreferenceManager.applyAwardVkShareApp()

        val numOfMillis = FirebaseRemoteConfig.getInstance().getLong(FREE_VK_GROUPS_JOIN_REWARD)
        val hours = Duration.millis(numOfMillis).toStandardHours().hours

        mMyNotificationManager.showNotificationSimple(
                BaseApplication.getAppInstance().getString(R.string.ads_reward_gained, hours),
                BaseApplication.getAppInstance().getString(R.string.thanks_for_supporting_us),
                NOTIFICATION_ID
        )
    }

    /**
     * check if its time to offer free trial subscription
     */
    private fun isNeedToOfferFreeTrial(): Boolean {
        val hasSubscription = mMyPreferenceManager.isHasSubscription || mMyPreferenceManager.isHasNoAdsSubscription
        return if (!hasSubscription && mMyPreferenceManager.isTimeOfferFreeTrialFromDisableAdsOption) {
            val bundle = Bundle()
            bundle.putString(Constants.Firebase.Analytics.EventParam.PLACE, Constants.Firebase.Analytics.EventValue.ADS_DISABLE)
            FirebaseAnalytics.getInstance(BaseApplication.getAppInstance()).logEvent(
                    Constants.Firebase.Analytics.EventName.FREE_TRIAL_OFFER_SHOWN,
                    bundle)

            mMyPreferenceManager.setFreeAdsDisableRewardGainedCount(0)
            view.showOfferFreeTrialSubscriptionPopup()
            true
        } else {
            false
        }
    }

    companion object {

        private const val NOTIFICATION_ID = 103
    }
}
