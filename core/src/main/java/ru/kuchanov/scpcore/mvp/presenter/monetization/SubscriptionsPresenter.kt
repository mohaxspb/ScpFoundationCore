@file:JvmName("SubscriptionsPresenterUtils")

package ru.kuchanov.scpcore.mvp.presenter.monetization

import com.android.vending.billing.IInAppBillingService
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.model.Item
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper
import ru.kuchanov.scpcore.mvp.base.BasePresenter
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsContract
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.subscribeBy
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.regex.Pattern


/**
 * Created by mohax on 13.01.2018.
 *
 * for ScpCore
 */
class SubscriptionsPresenter(
        myPreferencesManager: MyPreferenceManager,
        dbProviderFactory: DbProviderFactory,
        apiClient: ApiClient,
        private val inAppHelper: InAppHelper
) : BasePresenter<SubscriptionsContract.View>(
        myPreferencesManager,
        dbProviderFactory,
        apiClient,
        inAppHelper
), SubscriptionsContract.Presenter {

    override var isDataLoaded = false

    val items = mutableListOf<MyListItem>()

    override var owned: List<Item>? = null
    override var subsToBuy: List<Subscription>? = null
    override var inAppsToBuy: List<Subscription>? = null
    @InAppHelper.SubscriptionType
    override var type: Int = InAppHelper.SubscriptionType.NONE

    override fun getMarketData(service: IInAppBillingService) {
        Timber.d("getMarketData")
        view.showProgressCenter(true)
        view.showRefreshButton(false)

        val skuList = InAppHelper.getNewSubsSkus()
        if (FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)) {
            skuList.addAll(InAppHelper.getNewNoAdsSubsSkus())
        }

        Single.zip(
                inAppHelper.validateSubsObservable(service).toSingle(),
                inAppHelper.getSubsListToBuyObservable(service, skuList).toSingle(),
                inAppHelper.getInAppsListToBuyObservable(service).toSingle()
        ) { t1: List<Item>, t2: List<Subscription>, t3: List<Subscription> -> Triple(t1, t2, t3) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = {
                            isDataLoaded = true
                            view.showProgressCenter(false)
                            view.showRefreshButton(false)

                            owned = it.first
                            subsToBuy = it.second
                            inAppsToBuy = it.third
                            type = InAppHelper.getSubscriptionTypeFromItemsList(it.first)
                            //todo create data and show it in fragment
//                            items.clear()
//                            items.add(TextViewModel(R.string.subs_main_text))
//                            items.add(TextViewModel(R.string.subs_free_actions_title))
//                            items.add(InAppViewModel(
//                                    R.string.subs_free_actions_card_title,
//                                    R.string.subs_free_actions_card_description,
//                                    BaseApplication.getAppInstance().getString(R.string.free),
//                                    ID_FREE_ADS_DISABLE,
//                                    R.drawable.ic_free_ads_disable
//                            ))

//                            view.showData(items)

                            view.showData(it.first, it.second, it.third, type)
                        },
                        onError = {
                            Timber.e(it, "error getting cur subs")
                            isDataLoaded = false

                            view.showError(it)
                            view.showProgressCenter(false)
                            view.showRefreshButton(true)
                        }
                )
    }

    override fun onCurrentSubscriptionClick(id: String) = view.navigateToDisableAds()

    companion object {
        const val ID_FREE_ADS_DISABLE = "ID_FREE_ADS_DISABLE"
        const val ID_CURRENT_SUBS = "ID_CURRENT_SUBS"
        const val ID_CURRENT_SUBS_EMPTY = "ID_CURRENT_SUBS_EMPTY"

        fun getMonthFromSkuId(sku: String): Int {
            val p = Pattern.compile("\\d+")
            val m = p.matcher(sku)
            if (m.find()) {
                return m.group().toInt()
            }

            throw IllegalArgumentException("cant find month in sku")
        }
    }
}