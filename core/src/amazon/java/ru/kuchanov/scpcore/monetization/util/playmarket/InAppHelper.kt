package ru.kuchanov.scpcore.monetization.util.playmarket

import android.app.Activity
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.RequestId
import com.jakewharton.rxrelay.PublishRelay
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.model.Item
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.InappPurchaseUtil
import ru.kuchanov.scpcore.monetization.util.IntentSenderWrapper
import ru.kuchanov.scpcore.ui.activity.BaseActivity
import rx.Single
import rx.lang.kotlin.subscribeBy
import timber.log.Timber

class InAppHelper constructor(
        val preferenceManager: MyPreferenceManager,
        val dbProviderFactory: DbProviderFactory,
        val apiClient: ApiClient
) : InappPurchaseUtil {

    private val subscriptionsToBuyRelay = PublishRelay.create<List<Subscription>>()
    private val inappsToBuyRelay = PublishRelay.create<List<Subscription>>()
    private val inappsBoughtRelay = PublishRelay.create<Subscription>()
    private val ownedSubsRelay = PublishRelay.create<List<Item>>()

    private val purchaseListener = PurchaseListenerImpl(
            subscriptionsToBuyRelay,
            inappsToBuyRelay,
            inappsBoughtRelay,
            ownedSubsRelay,
            preferenceManager.preferences
    )

    init {
        Timber.d("InAppHelper created!")
        BaseApplication.getAppComponent().inject(this)

        subscriptionsToBuyRelay.subscribeBy(
                onNext = { Timber.d("subscriptionsToBuyRelay: $it") }
        )
        inappsToBuyRelay.subscribeBy(
                onNext = { Timber.d("inappsToBuyRelay: $it") }
        )
        inappsBoughtRelay.subscribeBy(
                onNext = { Timber.d("inappsBoughtRelay: $it") }
        )
    }

    override fun onActivate(activity: BaseActivity<*, *>) {
        PurchasingService.registerListener(activity, purchaseListener)

        Timber.d("PurchasingService.IS_SANDBOX_MODE: ${PurchasingService.IS_SANDBOX_MODE}")
    }

    override fun onResume() {
        PurchasingService.getUserData()

//        PurchasingService.getPurchaseUpdates(false)

//        PurchasingService.getProductData(getNewSubsSkus().toMutableSet())
//        PurchasingService.getProductData(getNewInAppsSkus().toMutableSet())
    }

    override fun onActivityDestroy(activity: Activity) {
        Timber.d("onActivityDestroy")
    }

    override fun getInAppHistoryObservable(): Single<List<Item>> {
        //todo

        return Single.just(listOf())
    }

    override fun getSubsListToBuyObservable(skus: List<String>): Single<List<Subscription>> {
        Timber.d("getSubsListToBuyObservable: $skus")

        PurchasingService.getProductData(skus.toMutableSet())

        return subscriptionsToBuyRelay
//                .doOnNext { Timber.d("getSubsListToBuyObservable onNext: $it") }
                .take(1)
                .toSingle()
//                .doOnError { Timber.e("getSubsListToBuyObservable onError: $it") }
    }

    override fun getInAppsListToBuy(): Single<List<Subscription>> {
        PurchasingService.getProductData(getNewInAppsSkus().toMutableSet())

        return inappsToBuyRelay
                .take(1)
                .toSingle()
    }

    override fun consumeInApp(sku: String, token: String): Single<Int> {
        //noting to do for amazon
        return Single.just(-1)
    }

    override fun validateSubsObservable(): Single<List<Item>> {
        return getValidatedOwnedSubsObservable()
                .flatMap { validatedItems ->
                    Timber.d("market validatedItems: %s", validatedItems)

                    preferenceManager.setLastTimeSubscriptionsValidated(System.currentTimeMillis())

                    @InappPurchaseUtil.SubscriptionType val type = getSubscriptionTypeFromItemsList(validatedItems)
                    Timber.d("subscription type: %s", type)
                    when (type) {
                        InappPurchaseUtil.SubscriptionType.NONE -> {
                            preferenceManager.isHasNoAdsSubscription = false
                            preferenceManager.isHasSubscription = false
                        }
                        InappPurchaseUtil.SubscriptionType.NO_ADS -> {
                            preferenceManager.isHasNoAdsSubscription = true
                            preferenceManager.isHasSubscription = false
                        }
                        InappPurchaseUtil.SubscriptionType.FULL_VERSION -> {
                            preferenceManager.isHasSubscription = true
                            preferenceManager.isHasNoAdsSubscription = true
                        }
                        else -> throw IllegalArgumentException("unexpected type: $type")
                    }

                    Single.just<List<Item>>(validatedItems)
                }
    }

    private fun getValidatedOwnedSubsObservable(): Single<List<Item>> {
        PurchasingService.getPurchaseUpdates(true)
        return ownedSubsRelay
                .take(1)
                .toSingle()
    }

    override fun intentSenderSingle(type: String, sku: String): Single<IntentSenderWrapper> {
        return Single.just(IntentSenderWrapper(null, sku))
    }

    override fun startPurchase(
            intentSender: IntentSenderWrapper,
            activity: BaseActivity<*, *>,
            requestCode: Int
    ): Single<Subscription> {
        val requestId: RequestId = PurchasingService.purchase(intentSender.sku)
        Timber.d("onBuyOrangeClick: requestId ($requestId)")
        return inappsBoughtRelay
                .take(1)
                .toSingle()
                .doOnEach { Timber.d("inappsBoughtRelay single doOnEach: $it") }
    }

    override fun getNewSubsSkus(): List<String> =
            BaseApplication.getAppInstance().getString(R.string.ver4_skus).split(",")

    override fun getFreeTrailSubsSkus(): List<String> =
            BaseApplication.getAppInstance().getString(R.string.ver4_subs_free_trial).split(",")

    override fun getNewNoAdsSubsSkus(): List<String> =
            BaseApplication.getAppInstance().getString(R.string.ver4_subs_no_ads).split(",")

    override fun getNewInAppsSkus(): List<String> =
            BaseApplication.getAppInstance().getString(R.string.ver4_inapp_skus).split(",")
}