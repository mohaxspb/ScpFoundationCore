package ru.kuchanov.scpcore.monetization.util.playmarket

import android.app.Activity
import android.content.IntentSender
import com.amazon.device.iap.PurchasingService
import com.jakewharton.rxrelay.PublishRelay
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.model.Item
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.InappPurchaseUtil
import ru.kuchanov.scpcore.ui.activity.BaseActivity
import rx.Single
import rx.lang.kotlin.subscribeBy
import timber.log.Timber

class InAppHelper constructor(
        val preferenceManager: MyPreferenceManager,
        val dbProviderFactory: DbProviderFactory,
        val apiClient: ApiClient
) : InappPurchaseUtil {

    private val subscriptionsRelay = PublishRelay.create<List<Subscription>>()
    private val inappsRelay = PublishRelay.create<List<Subscription>>()

    private val purchaseListener = PurchaseListenerImpl(subscriptionsRelay, inappsRelay)

    init {
        Timber.d("InAppHelper created!")
        BaseApplication.getAppComponent().inject(this)

        subscriptionsRelay.subscribeBy(
                onNext = { Timber.d("subscriptionsRelay: $it") }
        )
        inappsRelay.subscribeBy(
                onNext = { Timber.d("inappsRelay: $it") }
        )
    }

    override fun onActivate(activity: BaseActivity<*, *>) {
        PurchasingService.registerListener(activity, purchaseListener)

        Timber.d("PurchasingService.IS_SANDBOX_MODE: ${PurchasingService.IS_SANDBOX_MODE}")
    }

    override fun onResume() {
        PurchasingService.getUserData()

        PurchasingService.getPurchaseUpdates(false)

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
        //todo
        Timber.d("getSubsListToBuyObservable: $skus")

//        PurchasingService.getProductData(getNewSubsSkus().toMutableSet())
        PurchasingService.getProductData(skus.toMutableSet())

        return subscriptionsRelay
//                .doOnNext { Timber.d("getSubsListToBuyObservable onNext: $it") }
                .take(1)
                .toSingle()
//                .doOnError { Timber.e("getSubsListToBuyObservable onError: $it") }
//                .toSingle()

//        return Single.just(listOf())
    }

    override fun getInAppsListToBuyObservable(): Single<List<Subscription>> {
        //todo
        PurchasingService.getProductData(getNewInAppsSkus().toMutableSet())

        return inappsRelay
                .take(1)
                .toSingle()

//        return Single.just(listOf())
    }

    override fun consumeInApp(sku: String, token: String): Single<Int> {
        //todo
        return Single.just(-1)
    }

    override fun validateSubsObservable(): Single<List<Item>> {
        //todo
        return Single.just(listOf())
    }

    override fun intentSenderSingle(type: String, sku: String): Single<IntentSender> {
        //todo
        return Single.error(IllegalStateException("Not supported for Amazon!"))
    }

    override fun startPurchase(intentSender: IntentSender, activity: BaseActivity<*, *>, requestCode: Int) {
        //todo
        Timber.wtf("Not supported for Amazon!")
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
