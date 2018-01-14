package ru.kuchanov.scpcore.mvp.presenter.monetization

import android.support.v4.app.Fragment
import com.android.vending.billing.IInAppBillingService
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.model.Item
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.InAppHelper
import ru.kuchanov.scpcore.mvp.base.BasePresenter
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsContract
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
class SubscriptionsPresenter(
        myPreferencesManager: MyPreferenceManager,
        dbProviderFactory: DbProviderFactory,
        apiClient: ApiClient,
        private val inAppHelper: InAppHelper
) : BasePresenter<SubscriptionsContract.View>(
        myPreferencesManager,
        dbProviderFactory,
        apiClient
), SubscriptionsContract.Presenter {

    override var isDataLoaded = false

    override fun getMarketData(service: IInAppBillingService) {
        view.showProgressCenter(true)
        view.showRefreshButton(false)

        val skuList = inAppHelper.newSubsSkus
        if (FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)) {
            skuList.addAll(inAppHelper.newNoAdsSubsSkus)
        }

        inAppHelper.getValidatedOwnedSubsObservable(service)
                .flatMap { ownedItems ->
                    inAppHelper.getSubsListToBuyObservable(service, skuList)
                            .flatMap { toBuy -> Observable.just(Pair<List<Item>, List<Subscription>>(ownedItems, toBuy)) }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            isDataLoaded = true;
                            view.showProgressCenter(false)
                            view.showRefreshButton(false)

                            //show current subscription
                            @InAppHelper.SubscriptionType
                            val type = inAppHelper.getSubscriptionTypeFromItemsList(it.first);
                            val curSubsText = when (type) {
                                InAppHelper.SubscriptionType.NONE -> R.string.no_subscriptions
                                InAppHelper.SubscriptionType.NO_ADS -> R.string.subscription_no_ads_title
                                InAppHelper.SubscriptionType.FULL_VERSION -> R.string.subscription_full_version_title
                                else -> throw IllegalArgumentException("unexected subs type: " + type);
                            }

                            Timber.d("curSubsText: ${BaseApplication.getAppInstance().getString(curSubsText)}")

                            //todo create data and show it in fragment
//                            recyclerView.setLayoutManager(new LinearLayoutManager (getActivity()));
//                            recyclerView.setHasFixedSize(true);
//                            SubscriptionsAdapter adapter = new SubscriptionsAdapter();
//                            adapter.setData(ownedItemsAndSubscriptions.second);
//                            adapter.setArticleClickListener(SubscriptionsFragment.this);
//                            recyclerView.setAdapter(adapter);
                        },
                        onError = {
                            Timber.e(it, "error getting cur subs");
                            isDataLoaded = false;

                            view.showError(it)
                            view.showProgressCenter(false)
                            view.showRefreshButton(true)
                        },
                        onCompleted = {}
                );
    }

    override fun onSubscriptionClick(id: String, target: Fragment, inAppBillingService: IInAppBillingService) {
        try {
            InAppHelper.startSubsBuy(target, inAppBillingService, InAppHelper.InappType.SUBS, id)
        } catch (e: Exception) {
            Timber.e(e)
            view.showError(e)
        }
    }
}