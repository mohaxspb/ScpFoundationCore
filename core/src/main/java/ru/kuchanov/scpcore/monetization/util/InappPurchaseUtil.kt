package ru.kuchanov.scpcore.monetization.util

import android.content.IntentSender
import android.support.annotation.IntDef
import android.support.annotation.StringDef
import ru.kuchanov.scpcore.monetization.model.Item
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.ui.activity.BaseActivity
import rx.Single

interface InappPurchaseUtil {

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(InappType.IN_APP, InappType.SUBS)
    annotation class InappType {
        companion object {
            const val IN_APP = "inapp"
            const val SUBS = "subs"
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(SubscriptionType.NO_ADS, SubscriptionType.FULL_VERSION, SubscriptionType.NONE)
    annotation class SubscriptionType {
        companion object {
            const val NONE = -1
            const val NO_ADS = 0
            const val FULL_VERSION = 1
        }
    }

    fun getInAppHistoryObservable(): Single<List<Item>>

    fun getSubsListToBuyObservable(skus: List<String>): Single<List<Subscription>>

    fun getInAppsListToBuyObservable(): Single<List<Subscription>>

    fun consumeInApp(sku: String, token: String): Single<Int>

    fun validateSubsObservable(): Single<List<Item>>

    fun intentSenderSingle(@InappType type: String, sku: String): Single<IntentSender>

    fun startPurchase(
            intentSender: IntentSender,
            activity: BaseActivity<*, *>,
            requestCode: Int
    )
}
