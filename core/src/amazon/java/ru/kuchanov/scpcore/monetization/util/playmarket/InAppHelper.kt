package ru.kuchanov.scpcore.monetization.util.playmarket

import android.app.Activity
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.RequestId
import com.jakewharton.rxrelay.PublishRelay
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.model.Item
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
    private val mOwnedSubsRelay = PublishRelay.create<ItemsListWrapper>()

    private val purchaseListener = PurchaseListenerImpl(
            subscriptionsToBuyRelay,
            inappsToBuyRelay,
            inappsBoughtRelay,
            getOwnedSubsRelay(),
            preferenceManager.preferences
    )

    init {
        Timber.d("InAppHelper created!")
        BaseApplication.getAppComponent().inject(this)
    }

    override fun onActivate(activity: BaseActivity<*, *>) {
        PurchasingService.registerListener(activity, purchaseListener)

        Timber.d("PurchasingService.IS_SANDBOX_MODE: ${PurchasingService.IS_SANDBOX_MODE}")
    }

    override fun onResume() {
        PurchasingService.getUserData()
    }

    override fun onActivityDestroy(activity: Activity) {
        Timber.d("onActivityDestroy")
    }

    override fun getInAppHistory(): Single<Subscription> {
        PurchasingService.getPurchaseUpdates(true)

        return inappsBoughtRelay
                .map { it.subscription ?: throw it.error!! }
                .take(1)
                .toSingle()
    }

    override fun consumeInApp(sku: String, token: String): Single<Int> {
        val totalScoreToAdd = Constants.LEVEL_UP_SCORE_TO_ADD

        return apiClient
                .incrementScoreInFirebase(totalScoreToAdd)
                .flatMap { newTotalScore ->
                    apiClient
                            .addRewardedInapp(sku)
                            .flatMap { dbProviderFactory.dbProvider.updateUserScore(newTotalScore) }
                }
                .doOnError { preferenceManager.addUnsyncedScore(totalScoreToAdd) }
    }

    override fun getSubsListToBuyObservable(skus: List<String>): Single<List<Subscription>> {
        PurchasingService.getProductData(skus.toMutableSet())

        return subscriptionsToBuyRelay
                .map { it.subscriptions ?: throw it.error!! }
                .take(1)
                .toSingle()
    }

    override fun getInAppsListToBuy(): Single<List<Subscription>> {
        PurchasingService.getProductData(getNewInAppsSkus().toMutableSet())

        return inappsToBuyRelay
                .map { it.subscriptions ?: throw it.error!! }
                .take(1)
                .toSingle()
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

                    return@flatMap Single.just(validatedItems)
                }
    }

    private fun getValidatedOwnedSubsObservable(): Single<List<Item>> {
        PurchasingService.getPurchaseUpdates(true)

        return getOwnedSubsRelay()
                .map { it.items ?: throw it.error!! }
                .take(1)
                .toSingle()
    }

    override fun intentSenderSingle(
            @InappPurchaseUtil.InappType type: String,
            sku: String
    ): Single<IntentSenderWrapper> {
        return Single.just(IntentSenderWrapper(null, type, sku))
    }

    override fun startPurchase(
            intentSender: IntentSenderWrapper
    ): Single<Subscription> {
        val requestId: RequestId = PurchasingService.purchase(intentSender.sku)
        Timber.d("startPurchase: requestId ($requestId)")
        return when (intentSender.type) {
            InappPurchaseUtil.InappType.IN_APP -> {
                inappsBoughtRelay
                        .map { it.subscription ?: throw it.error!! }
                        .take(1)
                        .toSingle()
            }
            InappPurchaseUtil.InappType.SUBS -> {
                getOwnedSubsRelay()
                        .map { subsWrapper ->
                            subsWrapper.items?.first()?.let {
                                Subscription(
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
                                        0,
                                        null
                                )
                            } ?: throw subsWrapper.error!!
                        }
                        .take(1)
                        .toSingle()
            }
            else -> throw IllegalArgumentException("Unexpected type: ${intentSender.type}")
        }
    }

    override fun getOwnedSubsRelay(): PublishRelay<ItemsListWrapper> = mOwnedSubsRelay

    override fun getBoughtInappRelay(): PublishRelay<SubscriptionWrapper> = inappsBoughtRelay

    override fun getNewSubsSkus(): List<String> =
            BaseApplication.getAppInstance().getString(R.string.ver4_skus).split(",")

    override fun getFreeTrailSubsSkus(): List<String> =
            BaseApplication.getAppInstance().getString(R.string.ver4_subs_free_trial).split(",")

    override fun getNewNoAdsSubsSkus(): List<String> =
            BaseApplication.getAppInstance().getString(R.string.ver4_subs_no_ads).split(",")

    override fun getNewInAppsSkus(): List<String> =
            BaseApplication.getAppInstance().getString(R.string.ver4_inapp_skus).split(",")
}
