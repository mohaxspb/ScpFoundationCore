package ru.kuchanov.scpcore.monetization.util.playmarket

import android.content.IntentSender
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
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
import timber.log.Timber
import javax.inject.Inject

class InAppHelper constructor(
        val preferenceManager: MyPreferenceManager,
        val dbProviderFactory: DbProviderFactory,
        val apiClient: ApiClient
) : InappPurchaseUtil {

    //    @Inject
    private val purchaseListener = PurchaseListenerImpl()

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

        PurchasingService.getPurchaseUpdates(false)

        //todo check skus
//        PurchasingService.getProductData(getNewSubsSkus().toMutableSet())
        PurchasingService.getProductData(mutableSetOf("full_1month_050519_0"))
    }

    override fun getInAppHistoryObservable(): Single<List<Item>> {
        //todo

        return Single.just(listOf())
    }

    override fun getSubsListToBuyObservable(skus: List<String>): Single<List<Subscription>> {
        //todo
        return Single.just(listOf())
    }

    override fun getInAppsListToBuyObservable(): Single<List<Subscription>> {
        //todo
        return Single.just(listOf())
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
