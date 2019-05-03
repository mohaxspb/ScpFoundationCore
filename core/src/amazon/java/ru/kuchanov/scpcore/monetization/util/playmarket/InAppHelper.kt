package ru.kuchanov.scpcore.monetization.util.playmarket

import android.content.IntentSender
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

class InAppHelper(
        val preferenceManager: MyPreferenceManager,
        val dbProviderFactory: DbProviderFactory,
        val apiClient: ApiClient
) : InappPurchaseUtil {

    override fun getInAppHistoryObservable(): Single<List<Item>> {
        //todo
    }

    override fun getSubsListToBuyObservable(skus: List<String>): Single<List<Subscription>> {
        //todo
    }

    override fun getInAppsListToBuyObservable(): Single<List<Subscription>> {
        //todo
    }

    override fun consumeInApp(sku: String, token: String): Single<Int> {
        //todo
    }

    override fun validateSubsObservable(): Single<List<Item>> {
        //todo
    }

    override fun intentSenderSingle(type: String, sku: String): Single<IntentSender> {
        //todo
    }

    override fun startPurchase(intentSender: IntentSender, activity: BaseActivity<*, *>, requestCode: Int) {
        //todo
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
