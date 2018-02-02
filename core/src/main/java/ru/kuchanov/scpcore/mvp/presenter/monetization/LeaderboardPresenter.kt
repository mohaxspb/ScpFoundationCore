package ru.kuchanov.scpcore.mvp.presenter.monetization

import com.android.vending.billing.IInAppBillingService
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.api.model.response.LeaderBoardResponse
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.InAppHelper
import ru.kuchanov.scpcore.mvp.base.BasePresenter
import ru.kuchanov.scpcore.mvp.contract.monetization.LeaderboardContract
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
        apiClient
), LeaderboardContract.Presenter {

    override var isDataLoaded = false

    override val data = mutableListOf<MyListItem>()

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
                { t1: List<Subscription>, t2: LeaderBoardResponse -> Pair(t1, t2) }
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = {
                            isDataLoaded = true;
                            view.showProgressCenter(false)
                            view.showRefreshButton(false)
                            data.clear()
                            //todo create data and show it in fragment
//                            items.add(TextViewModel(R.string.subs_main_text))
//                            items.add(TextViewModel(R.string.subs_free_actions_title))
//                            items.add(InAppViewModel(
//                                    R.string.subs_free_actions_card_title,
//                                    R.string.subs_free_actions_card_description,
//                                    BaseApplication.getAppInstance().getString(R.string.free),
//                                    ID_FREE_ADS_DISABLE,
//                                    R.drawable.ic_free_ads_disable
//                            ))

                            view.showData(data)
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

    override fun onRewardedVideoClick() {
        //todo
    }

//    override fun onSubscriptionClick(id: String, target: Fragment, inAppBillingService: IInAppBillingService) {
//        if (id == ID_FREE_ADS_DISABLE) {
//            view.navigateToDisableAds()
//            return
//        }
//        val type: String
//        if (id in InAppHelper.getNewInAppsSkus()) {
//            type = InAppHelper.InappType.IN_APP
//        } else {
//            type = InAppHelper.InappType.SUBS
//        }
//        try {
//            InAppHelper.startSubsBuy(target, inAppBillingService, type, id)
//        } catch (e: Exception) {
//            Timber.e(e)
//            view.showError(e)
//        }
//    }
}