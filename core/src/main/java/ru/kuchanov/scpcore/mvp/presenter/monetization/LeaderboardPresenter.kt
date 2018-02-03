package ru.kuchanov.scpcore.mvp.presenter.monetization

import android.support.v4.app.Fragment
import com.android.vending.billing.IInAppBillingService
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.api.model.remoteconfig.LevelsJson
import ru.kuchanov.scpcore.api.model.response.LeaderBoardResponse
import ru.kuchanov.scpcore.controller.adapter.viewmodel.LabelViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard.LeaderboardUserViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard.LevelViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.subscriptions.InAppViewModel
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.db.model.User
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.InAppHelper
import ru.kuchanov.scpcore.mvp.base.BasePresenter
import ru.kuchanov.scpcore.mvp.contract.monetization.LeaderboardContract
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
        private val inAppHelper: InAppHelper,
        private val gson: Gson
) : BasePresenter<LeaderboardContract.View>(
        myPreferencesManager,
        dbProviderFactory,
        apiClient
), LeaderboardContract.Presenter {

    override var isDataLoaded = false

    override val data = mutableListOf<MyListItem>()

//    override lateinit var leaderBoardResponse: LeaderBoardResponse;

    override lateinit var myUser: User

//    override var inAppsToBuy: List<Subscription>? = null

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
//                mDbProviderFactory.dbProvider.userSyncUnmanaged.toSingle(),
                Observable.just(mDbProviderFactory.dbProvider.userUnmanaged).toSingle(),
                { inapps: List<Subscription>, leaderboard: LeaderBoardResponse, user: User? -> Triple(inapps, leaderboard, user) }
        )
                .map {
                    val levelJson = LevelsJson.getLevelsJson()
                    val viewModels = mutableListOf<MyListItem>()
                    val users = it.second.users
                    users.sortByDescending { it.score }
                    users.subList(0, 3).forEachIndexed { index, user ->
                        viewModels.add(LabelViewModel(0, textString = BaseApplication.getAppInstance().getString(R.string.leaderboard_place, index + 1)))
                        val level = levelJson.getLevelForScore(user.score)
                        viewModels.add(LeaderboardUserViewModel(
                                index + 1,
                                user,
                                LevelViewModel(
                                        level!!,
                                        levelJson.scoreToNextLevel(user.score, level),
                                        levelJson.getLevelMaxScore(level),
                                        level.id == LevelsJson.MAX_LEVEL_ID)
                        ))
                    }

                    //todo colors
                    viewModels.add(LabelViewModel(R.string.leaderboard_inapp_label))
                    val levelUpInApp = it.first.first()
                    viewModels.add(InAppViewModel(
                            R.string.leaderboard_inapp_title,
                            R.string.leaderboard_inapp_description,
                            levelUpInApp.price,
                            levelUpInApp.productId,
                            R.drawable.ic_adblock,
                            R.color.bgSubsBottom
                    ))

                    viewModels.addAll(users.subList(3, users.size).mapIndexed { index, firebaseObjectUser ->
                        val level = levelJson.getLevelForScore(firebaseObjectUser.score)
                        LeaderboardUserViewModel(
                                index + 3 + 1,
                                firebaseObjectUser,
                                LevelViewModel(
                                        level!!,
                                        levelJson.scoreToNextLevel(firebaseObjectUser.score, level),
                                        levelJson.getLevelMaxScore(level),
                                        level.id == LevelsJson.MAX_LEVEL_ID)
                        )
                    })

                    return@map Triple(viewModels, it.second, it.third)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = {
                            isDataLoaded = true;
                            view.showProgressCenter(false)
                            view.showRefreshButton(false)
                            data.clear()

                            data.addAll(it.first)
//                            leaderBoardResponse = it.second
                            myUser = it.third!!

                            view.showData(data)
                            view.showUpdateDate(it.second.lastUpdated, it.second.timeZone)
                            view.showUser(myUser)
                        },
                        onError = {
                            Timber.e(it, "error getting cur subs");
                            isDataLoaded = false;

                            view.showError(it)
                            view.showProgressCenter(false)
                            view.showRefreshButton(true)
                        }
                );
    }

    override fun onUserChanged(user: User?) {
        super.onUserChanged(user)
        myUser = user!!
        view.showUser(myUser)
    }

    override fun onRewardedVideoClick() {
        //todo
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