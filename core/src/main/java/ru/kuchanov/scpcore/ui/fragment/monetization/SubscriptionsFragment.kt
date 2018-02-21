package ru.kuchanov.scpcore.ui.fragment.monetization

import android.app.Activity
import android.content.Intent
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v7.widget.LinearLayoutManager
import android.view.View.GONE
import android.view.View.VISIBLE
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import kotlinx.android.synthetic.main.fragment_subscriptions.*
import org.json.JSONException
import org.json.JSONObject
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.*
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.subscriptions.CurSubsDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.subscriptions.CurSubsEmptyDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.subscriptions.InAppDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.subscriptions.LabelWithPercentDelegate
import ru.kuchanov.scpcore.controller.adapter.viewmodel.*
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.subscriptions.CurSubsEmptyViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.subscriptions.CurSubsViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.subscriptions.InAppViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.subscriptions.LabelWithPercentViewModel
import ru.kuchanov.scpcore.manager.InAppBillingServiceConnectionObservable
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.model.Item
import ru.kuchanov.scpcore.monetization.model.PurchaseData
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.InAppHelper
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsContract
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsScreenContract
import ru.kuchanov.scpcore.mvp.presenter.monetization.SubscriptionsPresenter.Companion.ID_CURRENT_SUBS
import ru.kuchanov.scpcore.mvp.presenter.monetization.SubscriptionsPresenter.Companion.ID_CURRENT_SUBS_EMPTY
import ru.kuchanov.scpcore.mvp.presenter.monetization.SubscriptionsPresenter.Companion.ID_FREE_ADS_DISABLE
import ru.kuchanov.scpcore.mvp.presenter.monetization.getMonthFromSkuId
import ru.kuchanov.scpcore.ui.activity.BaseActivity
import ru.kuchanov.scpcore.ui.activity.SubscriptionsActivity
import ru.kuchanov.scpcore.ui.activity.BaseDrawerActivity.REQUEST_CODE_INAPP
import ru.kuchanov.scpcore.ui.fragment.BaseFragment
import ru.kuchanov.scpcore.util.SystemUtils
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.subscribeBy
import rx.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class SubscriptionsFragment :
        BaseFragment<SubscriptionsContract.View, SubscriptionsContract.Presenter>(),
        SubscriptionsContract.View {

    @Inject
    lateinit var mGson: Gson
    @Inject
    lateinit var mInAppHelper: InAppHelper
    @Inject
    lateinit var myPreferenceManager: MyPreferenceManager

    //    private val items: MutableList<MyListItem> = mutableListOf()
    private lateinit var adapter: ListDelegationAdapter<List<MyListItem>>

    override fun callInjections() = BaseApplication.getAppComponent().inject(this)

    override fun getLayoutResId() = R.layout.fragment_subscriptions

    override fun initViews() {
        InAppBillingServiceConnectionObservable.getInstance().serviceStatusObservable.subscribe { connected ->
            if (!isAdded) {
                return@subscribe
            }
            if (connected!! && !getPresenter().isDataLoaded && isAdded && activity is BaseActivity<*, *>) {
                (activity as? BaseActivity<*, *>)?.apply { presenter.getMarketData(this.getIInAppBillingService()) }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(DividerDelegate())
        delegateManager.addDelegate(TextDelegate())
        delegateManager.addDelegate(LabelDelegate())
        delegateManager.addDelegate(LabelWithPercentDelegate())
        delegateManager.addDelegate(InAppDelegate {
            baseActivity?.apply {
                this@SubscriptionsFragment.getPresenter().onSubscriptionClick(
                    it,
                    this@SubscriptionsFragment,
                    getIInAppBillingService())
            }
        })
        delegateManager.addDelegate(CurSubsDelegate { getPresenter().onCurrentSubscriptionClick(it) })
        delegateManager.addDelegate(CurSubsEmptyDelegate(
            {
                baseActivity?.apply {
                    this@SubscriptionsFragment.getPresenter().onSubscriptionClick(
                        InAppHelper.getNewInAppsSkus().first(),
                        this@SubscriptionsFragment,
                        getIInAppBillingService())
                }
            },
            {
                baseActivity?.updateOwnedMarketItems()
                baseActivity?.getIInAppBillingService()?.apply { this@SubscriptionsFragment.getPresenter().getMarketData(this) }
            }
        ))
        adapter = ListDelegationAdapter(delegateManager)
        recyclerView.adapter = adapter

        refresh.setOnClickListener { baseActivity?.getIInAppBillingService()?.apply { getPresenter().getMarketData(this) } }

        if (presenter.owned == null) {
            baseActivity?.getIInAppBillingService()?.apply { getPresenter().getMarketData(this) }
        } else {
            showProgressCenter(false)
            presenter.apply { showData(owned!!, subsToBuy!!, inAppsToBuy!!, type) }
        }
    }

    override fun showProgressCenter(show: Boolean) {
        progressContainer.visibility = if (show) VISIBLE else GONE
    }

    override fun showRefreshButton(show: Boolean) {
        refresh.visibility = if (show) VISIBLE else GONE
    }

    //    override fun showData(items: List<MyListItem>) {
    override fun showData(owned: List<Item>, toBuy: List<Subscription>, inApps: List<Subscription>, curSubsType: Int) {
        Timber.d("showData")
        val items: MutableList<MyListItem> = mutableListOf()
        items.clear()
        items.add(TextViewModel(R.string.subs_main_text))
        items.add(LabelViewModel(R.string.subs_free_actions_title))
        items.add(
            InAppViewModel(
                R.string.subs_free_actions_card_title,
                R.string.subs_free_actions_card_description,
                BaseApplication.getAppInstance().getString(R.string.free),
                ID_FREE_ADS_DISABLE,
                R.drawable.ic_no_money
            ))
        //levelUp
        items.add(LabelViewModel(R.string.subs_level_5_label))
        val levelUp = inApps.first()
        items.add(
            InAppViewModel(
                R.string.subs_level_5_title,
                R.string.subs_level_5_description,
                levelUp.price,
                levelUp.productId,
                R.drawable.ic_05
            ))
        //cur sub
        items.add(LabelViewModel(R.string.subs_cur_label))
        when (curSubsType) {
            InAppHelper.SubscriptionType.NONE -> items.add(CurSubsEmptyViewModel(ID_CURRENT_SUBS_EMPTY))
            InAppHelper.SubscriptionType.NO_ADS -> items.add(
                CurSubsViewModel(
                    R.string.subs_no_ads_title,
                    R.string.subs_no_ads_description,
                    ID_CURRENT_SUBS,
                    R.drawable.ic_adblock
                ))
            else -> {
                val item: Item = when {
                    owned.any { it.sku.contains("12") } -> owned.find { it.sku.contains("12") }
                    owned.any { it.sku.contains("6") } -> owned.find { it.sku.contains("6") }
                    owned.any { it.sku.contains("3") } -> owned.find { it.sku.contains("3") }
                    owned.any { it.sku.contains("1") } -> owned.find { it.sku.contains("1") }
                    else -> throw IllegalArgumentException("unexpected subs period")
                } ?: throw IllegalArgumentException("item is null! wtf?!")

                @StringRes
                val title: Int
                @StringRes
                val description: Int = R.string.subs_full_description
                @DrawableRes
                val icon: Int
                when (getMonthFromSkuId(item.sku)) {
                    1 -> {
                        title = R.string.subs_1_month_title
                        icon = R.drawable.ic_scp_icon_laborant
                    }
                    3 -> {
                        title = R.string.subs_3_month_title
                        icon = R.drawable.ic_scp_icon_mns
                    }
                    6 -> {
                        title = R.string.subs_6_month_title
                        icon = R.drawable.ic_scp_icon_ns
                    }
                    12 -> {
                        title = R.string.subs_12_month_title
                        icon = R.drawable.ic_scp_icon_sns
                    }
                    else -> throw IllegalArgumentException("unexpected subs period")
                }

                items.add(
                    CurSubsViewModel(
                        title,
                        description,
                        ID_CURRENT_SUBS,
                        icon
                    ))
            }
        }
        //bottom panel
        val bgColor = R.color.bgSubsBottom
        items.add(DividerViewModel(bgColor = bgColor, height = resources.getDimensionPixelSize(R.dimen.defaultMarginMedium)))

        //no ads
        val noAdsSubsEnabled = FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)

        val textColor = R.color.subsTextColorBottom

        val subsFullOneMonth = toBuy
                .filter { it.productId !in InAppHelper.getNewNoAdsSubsSkus() }
                .first { getMonthFromSkuId(it.productId) == 1 }

        toBuy.forEach {
            if (noAdsSubsEnabled && (it.productId in InAppHelper.getNewNoAdsSubsSkus())) {
                items.add(
                    LabelViewModel(
                        R.string.subs_no_ads_label,
                        textColor = textColor,
                        bgColor = bgColor
                    ))
                items.add(
                    InAppViewModel(
                        R.string.subs_no_ads_title,
                        R.string.subs_no_ads_description,
                        it.price,
                        it.productId,
                        R.drawable.ic_adblock,
                        bgColor
                    ))
            } else {
                @StringRes
                val label: Int
                @StringRes
                val title: Int
                @StringRes
                val description: Int = R.string.subs_full_description
                @DrawableRes
                val icon: Int

                val month = getMonthFromSkuId(it.productId)
                when (month) {
                    1 -> {
                        label = R.string.subs_1_month_label
                        title = R.string.subs_1_month_title
                        icon = R.drawable.ic_scp_icon_laborant
                    }
                    3 -> {
                        label = R.string.subs_3_month_label
                        title = R.string.subs_3_month_title
                        icon = R.drawable.ic_scp_icon_mns
                    }
                    6 -> {
                        label = R.string.subs_6_month_label
                        title = R.string.subs_6_month_title
                        icon = R.drawable.ic_scp_icon_ns
                    }
                    12 -> {
                        label = R.string.subs_12_month_label
                        title = R.string.subs_12_month_title
                        icon = R.drawable.ic_scp_icon_sns
                    }
                    else -> throw IllegalArgumentException("unexpected subs period")
                }

                val oneMonthPriceForMonths = subsFullOneMonth.price_amount_micros * month
                val percent = 100L - it.price_amount_micros * 100L / oneMonthPriceForMonths
                items.add(
                    LabelWithPercentViewModel(
                        label,
                        if (month != 1) (oneMonthPriceForMonths / 1000000L).toString() + SystemUtils.getCurrencySymbol2(it.price_currency_code) else "",
                        if (month != 1) percent.toString() else ""
                    ))
                items.add(
                    InAppViewModel(
                        title,
                        description,
                        it.price,
                        it.productId,
                        icon,
                        bgColor
                    ))
            }
        }

        adapter.items = items
        adapter.notifyDataSetChanged()
    }

    override fun navigateToDisableAds() = (baseActivity as SubscriptionsActivity)
            .showScreen(SubscriptionsScreenContract.Screen.FREE_ACTIONS)

    override fun navigateToLeaderboard() = (baseActivity as SubscriptionsActivity)
            .showScreen(SubscriptionsScreenContract.Screen.LEADERBOARD)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("called in fragment")
        if (requestCode == REQUEST_CODE_SUBSCRIPTION) {
            if (data == null) {
                if (isAdded) {
                    showMessageLong("Error while parse result, please try again")
                }
                return
            }
            val responseCode = data.getIntExtra("RESPONSE_CODE", 0)
            val purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA")
            //            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == Activity.RESULT_OK && responseCode == InAppHelper.RESULT_OK) {
                try {
                    val jo = JSONObject(purchaseData)
                    val sku = jo.getString("productId")
                    Timber.d("You have bought the %s", sku)

                    //validate subs list
                    baseActivity?.updateOwnedMarketItems()
                } catch (e: JSONException) {
                    Timber.e(e, "Failed to parse purchase data.")
                    showError(e)
                }

            } else {
                if (isAdded) {
                    showMessageLong("Error: response code is not \"0\". Please try again")
                }
            }
        } else if (requestCode == REQUEST_CODE_INAPP) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    Timber.d("error_inapp data is NULL")
                    showMessage(R.string.error_inapp)
                    return
                }
                //            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
                val purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA")
                //            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
                Timber.d("purchaseData %s", purchaseData)
                val item = mGson.fromJson(purchaseData, PurchaseData::class.java)
                Timber.d("You have bought the %s", item.productId)

                if (item.productId == getString(R.string.inapp_skus).split(",").first()) {
                    //levelUp 5
                    //add 10 000 score
                    mInAppHelper.consumeInApp(item.productId, item.purchaseToken, baseActivity?.getIInAppBillingService())
                            .toSingle()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                onSuccess = {
                                    Timber.d("consume inapp successful, so update user score")
                                    mPresenter.updateUserScoreForInapp(item.productId)

                                    if (!myPreferenceManager.isHasAnySubscription) {
                                        baseActivity?.showOfferSubscriptionPopup()
                                    }
                                },
                                onError = {
                                    Timber.e(it, "error while consume inapp... X3 what to do)))")
                                    showError(it)
                                }
                            )
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun getToolbarTitle(): Int = R.string.subs_activity_title

    override fun getToolbarTextColor(): Int = android.R.color.white

    companion object {

        @JvmField
        val REQUEST_CODE_SUBSCRIPTION = 1001

        @JvmStatic
        fun newInstance(): SubscriptionsFragment = SubscriptionsFragment()
    }
}