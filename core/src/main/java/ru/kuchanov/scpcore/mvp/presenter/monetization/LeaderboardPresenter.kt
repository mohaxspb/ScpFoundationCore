package ru.kuchanov.scpcore.mvp.presenter.monetization

import android.support.v4.app.Fragment
import com.android.vending.billing.IInAppBillingService
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.api.model.firebase.FirebaseObjectUser
import ru.kuchanov.scpcore.api.model.remoteconfig.LevelsJson
import ru.kuchanov.scpcore.api.model.response.LeaderBoardResponse
import ru.kuchanov.scpcore.controller.adapter.viewmodel.DividerViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.LabelViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard.LeaderboardUserViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard.LevelViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.subscriptions.InAppViewModel
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.db.model.LeaderboardUser
import ru.kuchanov.scpcore.db.model.User
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.InAppHelper
import ru.kuchanov.scpcore.mvp.base.BasePresenter
import ru.kuchanov.scpcore.mvp.contract.monetization.LeaderboardContract
import ru.kuchanov.scpcore.util.DimensionUtils
import rx.Observable
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.subscribeBy
import rx.schedulers.Schedulers
import timber.log.Timber

/**
 * Created by mohax on 13.01.2018.
 *
 * for ScpCore
 */
class LeaderboardPresenter(
    myPreferencesManager: MyPreferenceManager,
    dbProviderFactory: DbProviderFactory,
    apiClient: ApiClient,
    private val inAppHelper: InAppHelper
) : BasePresenter<LeaderboardContract.View>(
    myPreferencesManager,
    dbProviderFactory,
    apiClient), LeaderboardContract.Presenter {

    override var isDataLoaded = false

    override val data = mutableListOf<MyListItem>()

    override var leaderBoardResponse: LeaderBoardResponse? = null

    override var myUser: User? = null

    override fun loadData(service: IInAppBillingService) {
        Timber.d("getMarketData")
        view.showProgressCenter(true)
        view.showRefreshButton(false)

        val skuList = InAppHelper.getNewSubsSkus()
        if (FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)) {
            skuList.addAll(InAppHelper.getNewNoAdsSubsSkus())
        }

        Single.zip(
            inAppHelper.getInAppsListToBuyObservable(service).toSingle(),
            mApiClient.leaderboard.toSingle(),
            Observable.just(mDbProviderFactory.dbProvider.userUnmanaged).toSingle(),
            { inapps: List<Subscription>, leaderboard: LeaderBoardResponse, user: User? -> Triple(inapps, leaderboard, user) }
        )
                .map {
                    val levelJson = LevelsJson.levelsJson
                    val viewModels = mutableListOf<MyListItem>()
                    viewModels.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(16)))
                    val users = it.second.users
                    users.sortByDescending { it.score }
                    val medalColorsArr = listOf(R.color.medalGold, R.color.medalSilver, R.color.medalBronze)
                    users.subList(0, 3).forEachIndexed { index, user ->
                        viewModels.add(
                            LabelViewModel(
                                0,
                                textString = BaseApplication.getAppInstance().getString(
                                    R.string.leaderboard_place,
                                    index + 1),
                                bgColor = R.color.freeAdsBackgroundColor))
                        viewModels.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(8)))

                        val level = levelJson.getLevelForScore(user.score)
                        viewModels.add(
                            LeaderboardUserViewModel(
                                index + 1,
                                user,
                                LevelViewModel(
                                    level!!,
                                    levelJson.scoreToNextLevel(user.score, level),
                                    levelJson.getLevelMaxScore(level),
                                    level.id == LevelsJson.MAX_LEVEL_ID),
                                medalTint = medalColorsArr[index]
                            ))
                    }

                    viewModels.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(16)))
                    viewModels.add(
                        LabelViewModel(
                            R.string.leaderboard_inapp_label,
                            textColor = R.color.material_green_500,
                            bgColor = R.color.freeAdsBackgroundColor))
                    val levelUpInApp = it.first.first()
                    viewModels.add(
                        InAppViewModel(
                            R.string.leaderboard_inapp_title,
                            R.string.leaderboard_inapp_description,
                            levelUpInApp.price,
                            levelUpInApp.productId,
                            R.drawable.ic_leaderbord_levelup_icon,
                            R.color.freeAdsBackgroundColor))

                    viewModels.addAll(users.subList(3, users.size).mapIndexed { index, firebaseObjectUser ->
                        val level = levelJson.getLevelForScore(firebaseObjectUser.score)
                        LeaderboardUserViewModel(
                            index + 3 + 1,
                            firebaseObjectUser,
                            LevelViewModel(
                                level!!,
                                levelJson.scoreToNextLevel(firebaseObjectUser.score, level),
                                levelJson.getLevelMaxScore(level),
                                level.id == LevelsJson.MAX_LEVEL_ID),
                            bgColor = R.color.leaderboardBottomBgColor)
                    })
                    myUser = it.third

                    return@map Triple(viewModels, it.second, convertUser(myUser, users, levelJson))
                }
                .retry(3)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = {
                        isDataLoaded = true
                        view.showProgressCenter(false)
                        view.showRefreshButton(false)
                        data.clear()

                        data.addAll(it.first)
                        leaderBoardResponse = it.second

                        view.showData(data)
                        view.showUpdateDate(it.second.lastUpdated, it.second.timeZone)
                        view.showUser(it.third)
                    },
                    onError = {
                        Timber.e(it, "error getting cur subs")
                        isDataLoaded = false

                        view.showError(it)
                        view.showProgressCenter(false)
                        view.showRefreshButton(true)
                    })
    }

    fun updateLeaderboardFromApi()=mApiClient.leaderboard
            .map { leaderBoardResponse -> leaderBoardResponse.users.map { LeaderboardUser(
                it.uid
            //todo
            ) } }
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { mDbProviderFactory.dbProvider.saveLeaderboardUsers(it.users) }

    private fun convertUser(user: User?, users: List<FirebaseObjectUser>, levelJson: LevelsJson): LeaderboardUserViewModel? {
        if (user == null) {
            return null
        }
        val userInFirebase = users.find { firebaseObjectUser -> firebaseObjectUser.uid == user.uid }
        val level = levelJson.getLevelForScore(userInFirebase!!.score)
        return LeaderboardUserViewModel(
            users.indexOf(userInFirebase),
            userInFirebase,
            LevelViewModel(
                level!!,
                levelJson.scoreToNextLevel(userInFirebase.score, level),
                levelJson.getLevelMaxScore(level),
                level.id == LevelsJson.MAX_LEVEL_ID),
            bgColor = R.color.leaderboardBottomBgColor)
    }

    override fun onUserChanged(user: User?) {
        super.onUserChanged(user)
        myUser = user
        if (myUser == null) {
            view.showUser(null)
        } else {
            leaderBoardResponse?.apply { view.showUser(convertUser(myUser, this.users, LevelsJson.levelsJson)) }
        }
    }

    override fun onRewardedVideoClick() {
        //nothing to do
    }

    override fun onSubscriptionClick(id: String, target: Fragment, inAppBillingService: IInAppBillingService) {
        val type: String
        if (id in InAppHelper.getNewInAppsSkus()) {
            type = InAppHelper.InappType.IN_APP
        } else {
            type = InAppHelper.InappType.SUBS
        }
        try {
            InAppHelper.startSubsBuy(target, inAppBillingService, type, id)
        } catch (e: Exception) {
            Timber.e(e)
            view.showError(e)
        }
    }
}