package ru.kuchanov.scpcore.monetization.util

import android.app.Activity
import android.content.IntentSender
import android.support.annotation.IntDef
import android.support.annotation.StringDef
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.monetization.model.Item
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.ui.activity.BaseActivity
import rx.Single
import java.util.*

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

    fun onActivate(activity: BaseActivity<*, *>)

    fun onActivityDestroy(activity: Activity)

    fun onResume()

    fun getInAppHistoryObservable(): Single<List<Item>>

    fun getSubsListToBuyObservable(skus: List<String>): Single<List<Subscription>>

    fun getInAppsListToBuyObservable(): Single<List<Subscription>>

    fun consumeInApp(sku: String, token: String): Single<Int>

    fun validateSubsObservable(): Single<List<Item>>

    fun intentSenderSingle(@InappType type: String, sku: String): Single<IntentSenderWrapper>

    fun startPurchase(
            intentSender: IntentSenderWrapper,
            activity: BaseActivity<*, *>,
            requestCode: Int
    ): Single<Subscription>

    fun getFreeTrailSubsSkus(): List<String>

    fun getNewSubsSkus(): List<String>

    fun getNewNoAdsSubsSkus(): List<String>

    fun getNewInAppsSkus(): List<String>

    @SubscriptionType
    fun getSubscriptionTypeFromItemsList(ownedItems: List<Item>): Int {
        val context = BaseApplication.getAppInstance()
        //add old old donate subs, new ones and one with free trial period
//        val fullVersionSkus = mutableListOf<String>(*context.getString(R.string.old_skus).split(",").toTypedArray())
        val fullVersionSkus: MutableList<String> = context.getString(R.string.old_skus).split(",").toMutableList()
        fullVersionSkus += context.getString(R.string.ver_2_skus).split(",")
        fullVersionSkus += context.getString(R.string.ver3_skus).split(",")
        fullVersionSkus += context.getString(R.string.subs_free_trial).split(",")
        fullVersionSkus += context.getString(R.string.ver3_subs_free_trial).split(",")
        fullVersionSkus += context.getString(R.string.ver4_skus).split(",")
        fullVersionSkus += context.getString(R.string.ver4_subs_free_trial).split(",")

        val noAdsSkus = mutableListOf<String>()
        noAdsSkus += context.getString(R.string.subs_no_ads_old)
        noAdsSkus += context.getString(R.string.subs_no_ads_ver_2)
        noAdsSkus += context.getString(R.string.ver3_subs_no_ads)
        noAdsSkus += context.getString(R.string.ver4_subs_no_ads)

        val ownedSkus = getSkuListFromItemsList(ownedItems)
        noAdsSkus.retainAll(ownedSkus)
        fullVersionSkus.retainAll(ownedSkus)

        return if (fullVersionSkus.isEmpty())
            if (noAdsSkus.isEmpty())
                SubscriptionType.NONE
            else
                SubscriptionType.NO_ADS
        else
            SubscriptionType.FULL_VERSION
    }

    private fun getSkuListFromItemsList(ownedItems: List<Item>): List<String> {
        val skus = ArrayList<String>()
        for (item in ownedItems) {
            skus.add(item.sku)
        }
        return skus
    }

    companion object {
        const val RESULT_OK = 0 // - success

        const val RESULT_USER_CANCELED = 1 // - user pressed back or canceled a dialog

        const val RESULT_ITEM_ALREADY_OWNED = 7 // - Failure to purchase since item is already owned
    }
}

class IntentSenderWrapper(
        val intentSender: IntentSender?,
        val sku: String
)
