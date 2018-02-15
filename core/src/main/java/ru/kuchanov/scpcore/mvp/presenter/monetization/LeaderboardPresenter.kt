package ru.kuchanov.scpcore.mvp.presenter.monetization

import android.support.v4.app.Fragment
import com.android.vending.billing.IInAppBillingService
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.realm.RealmResults
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Period
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.api.model.remoteconfig.LevelsJson
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
import ru.kuchanov.scpcore.ui.activity.BaseActivity
import ru.kuchanov.scpcore.ui.fragment.BaseFragment
import ru.kuchanov.scpcore.util.DimensionUtils
import rx.Observable
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

    override var users: RealmResults<LeaderboardUser>? = null

    override var myUser: User? = null

    private var updated = false;

    override fun loadData(service: IInAppBillingService) {
        Timber.d("getMarketData")
        view.showProgressCenter(true)
        view.showRefreshButton(false)

        val skuList = InAppHelper.getNewSubsSkus()
        if (FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)) {
            skuList.addAll(InAppHelper.getNewNoAdsSubsSkus())
        }

        Observable.zip(
            inAppHelper.getInAppsListToBuyObservable(service),
            Observable.just(mDbProviderFactory.dbProvider.leaderboardUsersUnmanaged),
            Observable.just(mDbProviderFactory.dbProvider.userUnmanaged),
            { inapps: List<Subscription>, users: List<LeaderboardUser>, user: User? -> Triple(inapps, users, user) }
        )
                .map {
                    val levelJson = LevelsJson.levelsJson
                    val viewModels = mutableListOf<MyListItem>()

                    val users = it.second
                    if (!users.isEmpty()) {
                        viewModels.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(16)))
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

                            val level = levelJson.levels[user.levelNum]
                            viewModels.add(
                                LeaderboardUserViewModel(
                                    index + 1,
                                    user,
                                    LevelViewModel(
                                        level,
                                        user.scoreToNextLevel,
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
                            val level = levelJson.levels[firebaseObjectUser.levelNum]
                            LeaderboardUserViewModel(
                                index + 3 + 1,
                                firebaseObjectUser,
                                LevelViewModel(
                                    level,
                                    firebaseObjectUser.scoreToNextLevel,
                                    levelJson.getLevelMaxScore(level),
                                    level.id == LevelsJson.MAX_LEVEL_ID),
                                bgColor = R.color.leaderboardBottomBgColor)
                        })
                    }
                    myUser = it.third

                    return@map Triple(viewModels, it.second, convertUser(myUser, users, levelJson))
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        isDataLoaded = true
                        view.showProgressCenter(false)
                        view.showRefreshButton(false)
                        data.clear()

                        data.addAll(it.first)

                        view.showData(data)
                        view.showUpdateDate(mMyPreferencesManager.leaderBoardUpdatedTime)
                        view.showUser(it.third)

                        if (!updated) {
                            view.showProgressCenter(true)
                            updateLeaderboardFromApi()
                        }
                    },
                    onError = {
                        Timber.e(it, "error getting cur subs")
                        isDataLoaded = false

                        view.showError(it)
                        view.showProgressCenter(false)
                        view.showRefreshButton(true)
                    })
    }

    override fun updateLeaderboardFromApi() = mApiClient.leaderboard.toSingle()
            .map { leaderBoardResponse ->
                val realmUsers = leaderBoardResponse.users.map {
                    //                    Timber.d("user: $it")
                    LeaderboardUser(
                        it.uid,
                        it.fullName,
                        it.avatar,
                        it.score,
                        it.numOfReadArticles,
                        it.levelNum,
                        it.scoreToNextLevel,
                        it.curLevelScore
                    )
                }
                return@map Triple(leaderBoardResponse.lastUpdated, leaderBoardResponse.timeZone, realmUsers)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { triple -> mDbProviderFactory.dbProvider.saveLeaderboardUsers(triple.third).toSingle().map { triple } }
            .subscribeBy(
                onSuccess = {
                    Timber.d("serverTime: ${DateTime(it.first, DateTimeZone.forID(it.second))}")
                    val utcTime = DateTime(it.first, DateTimeZone.forID(it.second)).withZone(DateTimeZone.UTC)
                    Timber.d("utcTime: $utcTime")
                    mMyPreferencesManager.leaderBoardUpdatedTime = utcTime.millis
                    updated = true
                    loadData((view as BaseFragment<*, *>).getBaseActivity().getIInAppBillingService())
                },
                onError = {
                    Timber.e(it)
                    view.showError(it)
                    view.showProgressCenter(false)
                }
            )


    private fun convertUser(user: User?, users: List<LeaderboardUser>, levelJson: LevelsJson): LeaderboardUserViewModel? {
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
            users?.apply { view.showUser(convertUser(myUser, this, LevelsJson.levelsJson)) }
        }
    }

    override fun onRewardedVideoClick() {
        //nothing to do
    }

    override fun onSubscriptionClick(id: String, target: Fragment, inAppBillingService: IInAppBillingService) {
        val type = if (id in InAppHelper.getNewInAppsSkus()) {
            InAppHelper.InappType.IN_APP
        } else {
            InAppHelper.InappType.SUBS
        }
        try {
            InAppHelper.startSubsBuy(target, inAppBillingService, type, id)
        } catch (e: Exception) {
            Timber.e(e)
            view.showError(e)
        }
    }
}