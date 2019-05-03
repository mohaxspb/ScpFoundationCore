package ru.kuchanov.scpcore.mvp.presenter.monetization

import com.android.vending.billing.IInAppBillingService
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
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
import ru.kuchanov.scpcore.db.model.Article
import ru.kuchanov.scpcore.db.model.LeaderboardUser
import ru.kuchanov.scpcore.db.model.User
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper
import ru.kuchanov.scpcore.mvp.base.BasePresenter
import ru.kuchanov.scpcore.mvp.contract.monetization.LeaderboardContract
import ru.kuchanov.scpcore.util.DimensionUtils
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
        apiClient,
        inAppHelper
), LeaderboardContract.Presenter {

    companion object {
        const val APPODEAL_ID = "appodeal"

        val levelJson = LevelsJson.levelsJson
    }

    override var isDataLoaded = false

    override var usersCount = 0

    override var data = mutableListOf<MyListItem>()

    override var inApps = listOf<Subscription>()

    override var myUser: User? = null

    override var userPositionOnLeaderboard: String? = null

    override var inAppService: IInAppBillingService? = null

    private var updated = false

    override var updateTime = myPreferencesManager.leaderboardUpdateDate.time

    override var readArticlesCount: Int = Article.ORDER_NONE

    override fun loadInitialData() {
        Timber.d("loadInitialData")
        if (inAppService == null) {
            view.showMessage(R.string.google_services_not_connected)
            view.showProgressCenter(false)
            view.enableSwipeRefresh(false)
            view.showSwipeRefreshProgress(false)
            view.showRefreshButton(true)
            return
        }
        view.showProgressCenter(true)
        view.showRefreshButton(false)
        view.showUpdateDate(updateTime)

        val skuList = InAppHelper.getNewSubsSkus()
        if (FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)) {
            skuList.addAll(InAppHelper.getNewNoAdsSubsSkus())
        }

        Single
                .zip(
                        inAppHelper.getInAppsListToBuyObservable(),
                        Single.just(mDbProviderFactory.dbProvider.userUnmanaged)
                ) { inApps: List<Subscription>, user: User? ->
                    Pair(
                            inApps,
                            user
                    )
                }
                .doOnSuccess {
                    inApps = it.first
                    myUser = it.second
                }
                .map { mDbProviderFactory.dbProvider.leaderboardUsersUnmanaged }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess {
                    updateUserPositionOnLeaderboard()
                    updateUserReadArticlesCount()
                }
                .subscribeBy(
                        onSuccess = {
                            Timber.d("loadInitialData onSuccess: ${it.size}")
                            isDataLoaded = true

                            usersCount = it.size

                            if (it.isEmpty()) {
                                view.showProgressCenter(true)
                                view.enableSwipeRefresh(false)
                                view.showSwipeRefreshProgress(false)
                                view.showRefreshButton(false)

                                updateLeaderboardFromApi(0)
                            } else {
                                view.showProgressCenter(false)
                                view.enableSwipeRefresh(true)
                                view.showSwipeRefreshProgress(true)
                                view.showRefreshButton(false)

                                data.clear()
                                data.addAll(createViewModels(it, inApps))

                                view.showData(data)
                                view.showUser(convertUser(user))
                                view.showUpdateDate(updateTime)
                                view.resetOnScrollListener()

                                if (!updated) {
                                    view.showSwipeRefreshProgress(true)
                                    updateLeaderboardFromApi(0)
                                }
                            }
                        },
                        onError = {
                            Timber.e(it, "error getting cur subs")
                            isDataLoaded = false

                            view.showError(it)
                            view.showProgressCenter(false)
                            view.enableSwipeRefresh(false)
                            view.showSwipeRefreshProgress(false)
                            view.showRefreshButton(true)
                        })
    }

    private fun updateUserReadArticlesCount() {
        Timber.d("updateUserReadArticlesCount")
        mDbProviderFactory
                .dbProvider
                .readArticlesCount
                .subscribeBy(
                        onNext = {
                            Timber.d("updateUserReadArticlesCount onNext: $it")
                            readArticlesCount = it
                            view.showUser(convertUser(myUser))
                        },
                        onError = { Timber.e(it, "Error while updateUserReadArticlesCount") }
                )
    }

    override fun updateUserPositionOnLeaderboard() {
        if (!myPreferencesManager.accessToken.isNullOrEmpty()) {
            mApiClient.userPositionInLeaderboard
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    //todo show progress
                    .subscribeBy(
                            onSuccess = {
                                userPositionOnLeaderboard = it.toString()
                                view.showUserPosition(it.toString())
                            },
                            onError = { Timber.e(it) }
                    )
        }
    }

    override fun updateLeaderboardFromApi(offset: Int, limit: Int) {
        Timber.d("updateLeaderboardFromApi: $offset/$limit")
        if (data.isEmpty()) {
            view.showProgressCenter(true)
            view.enableSwipeRefresh(false)
        } else {
            view.showSwipeRefreshProgress(true)
        }
        mApiClient.getLeaderboardUsers(offset, limit)
                .flatMap { users ->
                    if (offset == 0) {
                        mApiClient.leaderboardUsersUpdateDates.map { updateDates ->
                            val leaderboardUsersUpdateDates = updateDates
                                    .find { it.langId.equals(mApiClient.constantValues.appLang, true) }

                            leaderboardUsersUpdateDates?.let {
                                myPreferencesManager.saveLeaderboardUpdateDate(it.updated)
                            }
                        }.map { users }
                    } else {
                        Single.just(users)
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { users ->
                    if (offset == 0) {
                        mDbProviderFactory.dbProvider.saveLeaderboardUsers(users).map { users }
                    } else {
                        Single.just(users)
                    }
                }
                .subscribeBy(
                        onSuccess = {
                            Timber.d("updateLeaderboardFromApi onSuccess: ${it.size}")

                            if (offset == 0) {
                                view.showProgressCenter(false)
                                view.showSwipeRefreshProgress(false)
                                view.enableSwipeRefresh(true)

                                updated = true

                                usersCount = it.size

                                data.clear()
                                data.addAll(createViewModels(it, inApps))

                                view.showData(data)
                            } else {
                                view.showBottomProgress(false)
                                view.enableSwipeRefresh(true)

                                data.addAll(convertUsers(it, usersCount))

                                usersCount += it.size

                                view.showData(data)
                            }
                            view.resetOnScrollListener()

                            updateTime = myPreferencesManager.leaderboardUpdateDate.time
                            view.showUpdateDate(updateTime)
                        },
                        onError = {
                            Timber.e(it)
                            view.showError(it)
                            view.enableSwipeRefresh(data.isNotEmpty())
                            view.showRefreshButton(data.isEmpty())
                            view.showProgressCenter(false)
                            view.showBottomProgress(false)
                            view.showSwipeRefreshProgress(false)
                            view.resetOnScrollListener()
                        }
                )

        updateUserPositionOnLeaderboard()
    }

    private fun convertUser(user: User?): LeaderboardUserViewModel? {
        if (user == null) {
            return null
        }
        val level = levelJson.getLevelForScore(user.score) ?: return null
        val userInFirebase = LeaderboardUser(
                0,
                user.fullName,
                user.avatar,
                user.score,
                readArticlesCount,
                levelNum = level.id,
                scoreToNextLevel = levelJson.scoreToNextLevel(user.score, level),
                curLevelScore = user.score - level.score
        )

        //set score from realm
        userInFirebase.score = user.score
        return LeaderboardUserViewModel(
                //todo create method for calculate user position in API
                -1,
                userInFirebase,
                LevelViewModel(
                        level,
                        levelJson.scoreToNextLevel(userInFirebase.score, level),
                        levelJson.getLevelMaxScore(level),
                        level.id == LevelsJson.MAX_LEVEL_ID),
                bgColor = R.color.leaderboardBottomBgColor)
    }

    override fun onUserChanged(user: User?) {
        super.onUserChanged(user)
        myUser = user
        Timber.d("onUserChanged: $myUser")
        if (myUser == null) {
            view.showUser(null)
        } else {
            view.showUser(convertUser(myUser))
        }
    }

    override fun onRewardedVideoClick() = view.onRewardedVideoClick()

    private fun createViewModels(users: List<LeaderboardUser>, inApps: List<Subscription>): List<MyListItem> {
        val viewModels = mutableListOf<MyListItem>()

        viewModels.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(16)))

        viewModels.addAll(convertUsers(users, 0, true))

        viewModels.addAll(7, createMonetizationViewModels(inApps))

        return viewModels
    }

    private fun convertUsers(
            leaderboardUsers: List<LeaderboardUser>,
            startIndex: Int = 0,
            markFirst: Boolean = false
    ): List<MyListItem> {
        Timber.d("convertUsers: ${leaderboardUsers.size}/${leaderboardUsers[0]}")
        val viewModels = mutableListOf<MyListItem>()
        viewModels.addAll(leaderboardUsers.mapIndexed { index, firebaseObjectUser ->
            val level = levelJson.levels[firebaseObjectUser.levelNum]
            LeaderboardUserViewModel(
                    index + startIndex + 1,
                    firebaseObjectUser,
                    LevelViewModel(
                            level,
                            firebaseObjectUser.scoreToNextLevel,
                            levelJson.getLevelMaxScore(level),
                            level.id == LevelsJson.MAX_LEVEL_ID),
                    bgColor = R.color.leaderboardBottomBgColor)
        })

        if (markFirst) {
            val medalColorsArr = listOf(R.color.medalGold, R.color.medalSilver, R.color.medalBronze)

            viewModels.subList(0, 3).forEachIndexed { index, listItem ->
                with(listItem as LeaderboardUserViewModel) {
                    medalTint = medalColorsArr[index]
                    bgColor = R.color.freeAdsBackgroundColor
                }
            }

            for (index in 0..2 * 2 step 2) {
                viewModels.add(
                        index,
                        LabelViewModel(
                                0,
                                textString = BaseApplication.getAppInstance().getString(
                                        R.string.leaderboard_place,
                                        index / 2 + 1),
                                bgColor = R.color.freeAdsBackgroundColor))
                viewModels.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(8)))
            }
        }

        return viewModels
    }

    private fun createMonetizationViewModels(inApps: List<Subscription>): MutableList<MyListItem> {
        val monetizationViewModels = mutableListOf<MyListItem>()
        //levelUp inapp
        monetizationViewModels.add(DividerViewModel(R.color.freeAdsBackgroundColor, DimensionUtils.dpToPx(16)))
        monetizationViewModels.add(
                LabelViewModel(
                        R.string.leaderboard_inapp_label,
                        textColor = R.color.material_green_500,
                        bgColor = R.color.freeAdsBackgroundColor))
        val levelUpInApp = inApps.firstOrNull()
        monetizationViewModels.add(
                InAppViewModel(
                        R.string.leaderboard_inapp_title,
                        R.string.leaderboard_inapp_description,
                        levelUpInApp?.price ?: "N/A",
                        levelUpInApp?.productId ?: "N/A",
                        R.drawable.ic_leaderbord_levelup_icon,
                        R.color.freeAdsBackgroundColor))
        //appodeal rewarded video
        val score: Int = FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_REWARDED_VIDEO).toInt()
        monetizationViewModels.add(
                LabelViewModel(
                        0,
                        textString = BaseApplication.getAppInstance().getString(
                                R.string.leaderboard_inapp_label_undefined,
                                score
                        ),
                        textColor = R.color.material_green_500,
                        bgColor = R.color.freeAdsBackgroundColor))
        monetizationViewModels.add(
                InAppViewModel(
                        R.string.leaderboard_inapp_title,
                        R.string.leaderboard_appodeal_description,
                        "FREE",
                        APPODEAL_ID,
                        R.drawable.ic_inspect,
                        R.color.freeAdsBackgroundColor))

        return monetizationViewModels
    }
}