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
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.*
import ru.kuchanov.scpcore.ui.activity.BaseActivity
import rx.Single
import timber.log.Timber

class InAppHelper constructor(
        val preferenceManager: MyPreferenceManager,
        val dbProviderFactory: DbProviderFactory,
        val apiClient: ApiClient
) : InappPurchaseUtil {

    private val subscriptionsToBuyRelay = PublishRelay.create<SubscriptionsListWrapper>()
    private val inappsToBuyRelay = PublishRelay.create<SubscriptionsListWrapper>()
    private val inappsBoughtRelay = PublishRelay.create<SubscriptionWrapper>()
    private val ownedSubsRelay = PublishRelay.create<ItemsListWrapper>()

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

//        subscriptionsToBuyRelay.subscribeBy(
//                onNext = { Timber.d("subscriptionsToBuyRelay: $it") }
//        )
//        inappsToBuyRelay.subscribeBy(
//                onNext = { Timber.d("inappsToBuyRelay: $it") }
//        )
//        inappsBoughtRelay.subscribeBy(
//                onNext = { Timber.d("inappsBoughtRelay: $it") }
//        )
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

    override fun getInAppHistoryObservable(): Single<ItemsListWrapper> {
        //for amazon we don't (seems to be) have situation with unconsumed levelUps...
        return Single.just(ItemsListWrapper())
    }

    override fun getSubsListToBuyObservable(skus: List<String>): Single<SubscriptionsListWrapper> {
        Timber.d("getSubsListToBuyObservable: $skus")

        PurchasingService.getProductData(skus.toMutableSet())

        return subscriptionsToBuyRelay
                .take(1)
                .toSingle()
    }

    override fun getInAppsListToBuy(): Single<SubscriptionsListWrapper> {
        PurchasingService.getProductData(getNewInAppsSkus().toMutableSet())

        return inappsToBuyRelay
                .take(1)
                .toSingle()
    }

    override fun consumeInApp(sku: String, token: String): Single<Int> {
        //noting to do for amazon
        return Single.just(-1)
    }

    override fun validateSubsObservable(): Single<ItemsListWrapper> {
        return getValidatedOwnedSubsObservable()
                .flatMap { validatedItems ->
                    Timber.d("market validatedItems: %s", validatedItems)

                    preferenceManager.setLastTimeSubscriptionsValidated(System.currentTimeMillis())

                    if (validatedItems.items == null) {
                        return@flatMap Single.error<ItemsListWrapper>(NullPointerException("Items is null, while validateSubsObservable#flatMap"))
                    } else {
                        @InappPurchaseUtil.SubscriptionType val type = getSubscriptionTypeFromItemsList(validatedItems.items)
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

                        return@flatMap Single.just(validatedItems)
                    }
                }
    }

    private fun getValidatedOwnedSubsObservable(): Single<ItemsListWrapper> {
        PurchasingService.getPurchaseUpdates(true)
        return ownedSubsRelay
                .take(1)
                .toSingle()
    }

    override fun intentSenderSingle(@InappPurchaseUtil.InappType type: String, sku: String): Single<IntentSenderWrapper> {
        return Single.just(IntentSenderWrapper(null, type, sku))
    }

    override fun startPurchase(
            intentSender: IntentSenderWrapper
    ): Single<SubscriptionWrapper> {
        val requestId: RequestId = PurchasingService.purchase(intentSender.sku)
        Timber.d("onBuyOrangeClick: requestId ($requestId)")
        return when (intentSender.type) {
            InappPurchaseUtil.InappType.IN_APP -> inappsBoughtRelay
                    .take(1)
                    .toSingle()
                    .doOnEach { Timber.d("inappsBoughtRelay single doOnEach: $it") }
            InappPurchaseUtil.InappType.SUBS -> ownedSubsRelay
                    .take(1)
                    .map {
                        it.items?.first()
                                ?: throw NullPointerException("Items is null, while grant subscription")
                    }
                    .map {
                        SubscriptionWrapper(
                                subscription = Subscription(
                                        it.sku,
                                        InappPurchaseUtil.InappType.SUBS,
                                        null,
                                        0, //price_amount_micros
                                        null, //price_currency_code
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        0,
                                        null,
                                        0
                                )
                        )
                    }
                    .toSingle()
            else -> throw IllegalArgumentException("Unexpected type: ${intentSender.type}")
        }
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
