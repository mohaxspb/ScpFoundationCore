package ru.kuchanov.scpcore.ui.fragment.monetization

import android.app.Activity
import android.content.Intent
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v7.widget.LinearLayoutManager
import android.view.View.GONE
import android.view.View.VISIBLE
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import kotlinx.android.synthetic.main.fragment_subscriptions.*
import org.json.JSONException
import org.json.JSONObject
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.delegate.*
import ru.kuchanov.scpcore.controller.adapter.viewmodel.*
import ru.kuchanov.scpcore.manager.InAppBillingServiceConnectionObservable
import ru.kuchanov.scpcore.monetization.model.Item
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.InAppHelper
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsContract
import ru.kuchanov.scpcore.mvp.presenter.monetization.SubscriptionsPresenter.Companion.ID_CURRENT_SUBS
import ru.kuchanov.scpcore.mvp.presenter.monetization.SubscriptionsPresenter.Companion.ID_CURRENT_SUBS_EMPTY
import ru.kuchanov.scpcore.mvp.presenter.monetization.SubscriptionsPresenter.Companion.ID_FREE_ADS_DISABLE
import ru.kuchanov.scpcore.mvp.presenter.monetization.getMonthFromSkuId
import ru.kuchanov.scpcore.ui.base.BaseFragment
import ru.kuchanov.scpcore.util.SystemUtils
import timber.log.Timber

class SubscriptionsFragment :
        BaseFragment<SubscriptionsContract.View, SubscriptionsContract.Presenter>(),
        SubscriptionsContract.View {

    //    private val items: MutableList<MyListItem> = mutableListOf()
    private lateinit var adapter: ListDelegationAdapter<List<MyListItem>>

    override fun callInjections() = BaseApplication.getAppComponent().inject(this)

    override fun getLayoutResId(): Int = R.layout.fragment_subscriptions

    override fun initViews() {
        InAppBillingServiceConnectionObservable.getInstance().serviceStatusObservable.subscribe { connected ->
            if (connected!! && !getPresenter().isDataLoaded) {
                getPresenter().getMarketData(baseActivity.getIInAppBillingService())
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(TextDelegate())
        delegateManager.addDelegate(LabelDelegate())
        delegateManager.addDelegate(LabelWithPercentDelegate())
        delegateManager.addDelegate(InAppDelegate { getPresenter().onSubscriptionClick(it, this, baseActivity.getIInAppBillingService()) })
        delegateManager.addDelegate(CurSubsDelegate { getPresenter().onCurrentSubscriptionClick(it) })
        delegateManager.addDelegate(CurSubsEmptyDelegate(
                { getPresenter().onCurrentSubscriptionEmptyClick(it) },
                { getPresenter().getMarketData(baseActivity.getIInAppBillingService()) }
        ))
        adapter = ListDelegationAdapter(delegateManager)
        recyclerView.adapter = adapter

        refresh.setOnClickListener { getPresenter().getMarketData(baseActivity.getIInAppBillingService()) }
    }

    override fun showProgressCenter(show: Boolean) = progressCenter.setVisibility(if (show) VISIBLE else GONE)

    override fun showRefreshButton(show: Boolean) = refresh.setVisibility(if (show) VISIBLE else GONE)

    //    override fun showData(items: List<MyListItem>) {
    override fun showData(owned: List<Item>, toBuy: List<Subscription>, inApps: List<Subscription>, curSubsType: Int) {
        val items: MutableList<MyListItem> = mutableListOf()
        items.clear()
        items.add(TextViewModel(R.string.subs_main_text))
        items.add(LabelViewModel(R.string.subs_free_actions_title))
        items.add(InAppViewModel(
                R.string.subs_free_actions_card_title,
                R.string.subs_free_actions_card_description,
                BaseApplication.getAppInstance().getString(R.string.free),
                ID_FREE_ADS_DISABLE,
                R.drawable.ic_no_money
        ))
        //levelUp
        items.add(LabelViewModel(R.string.subs_level_5_label))
        val levelUp = inApps.first()
        items.add(InAppViewModel(
                R.string.subs_level_5_title,
                0,
                levelUp.price,
                levelUp.productId,
                R.drawable.ic_05
        ))
        //cur sub
        items.add(LabelViewModel(R.string.subs_cur_label))
        if (curSubsType == InAppHelper.SubscriptionType.NONE) {
            items.add(CurSubsEmptyViewModel(ID_CURRENT_SUBS_EMPTY))
        } else if (curSubsType == InAppHelper.SubscriptionType.NO_ADS) {
            items.add(CurSubsViewModel(
                    R.string.subs_no_ads_title,
                    R.string.subs_no_ads_description,
                    ID_CURRENT_SUBS,
                    R.drawable.ic_adblock
            ))
        } else {
            val item: Item?
            if (owned.any { it.sku.contains("12") }) {
                item = owned.find { it.sku.contains("12") }
            } else if (owned.any { it.sku.contains("6") }) {
                item = owned.find { it.sku.contains("6") }
            } else if (owned.any { it.sku.contains("3") }) {
                item = owned.find { it.sku.contains("3") }
            } else if (owned.any { it.sku.contains("1") }) {
                item = owned.find { it.sku.contains("1") }
            } else {
                throw IllegalArgumentException("unexpected subs period")
            }
            if (item == null) {
                throw IllegalArgumentException("item is null! wtf?!")
            }

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

            items.add(CurSubsViewModel(
                    title,
                    description,
                    ID_CURRENT_SUBS,
                    icon
            ))
        }
        //bottom panel
        //no ads
        val noAdsSubsEnabled = FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)

        val bgColor = R.color.bgSubsBottom
        val textColor = R.color.subsTextColorBottom

        val subsFullOneMonth = toBuy
                .filter { it.productId !in InAppHelper.getNewNoAdsSubsSkus() }
                .filter { getMonthFromSkuId(it.productId) == 1 }
                .first()

        toBuy.forEach {
            if (noAdsSubsEnabled && (it.productId in InAppHelper.getNewNoAdsSubsSkus())) {
                items.add(LabelViewModel(
                        R.string.subs_no_ads_label,
                        bgColor,
                        textColor
                ))
                items.add(InAppViewModel(
                        R.string.subs_no_ads_title,
                        R.string.subs_no_ads_description,
                        it.price,
                        it.productId,
                        R.drawable.ic_adblock,
                        R.color.bgSubsBottom
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
                items.add(LabelWithPercentViewModel(
                        label,
                        if (month != 1) (oneMonthPriceForMonths / 1000000L).toString() + SystemUtils.getCurrencySymbol(it.price_currency_code) else "",
                        if (month != 1) percent.toString() else ""
                ))
                items.add(InAppViewModel(
                        title,
                        description,
                        it.price,
                        it.productId,
                        icon,
                        R.color.bgSubsBottom
                ))
            }
        }

        adapter.items = items
        adapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("called in fragment")
        if (requestCode == REQUEST_CODE_SUBSCRIPTION) {
            if (data == null) {
                if (isAdded) {
                    baseActivity.showMessageLong("Error while parse result, please try again")
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
                    baseActivity.updateOwnedMarketItems()
                } catch (e: JSONException) {
                    Timber.e(e, "Failed to parse purchase data.")
                    baseActivity.showError(e)
                }

            } else {
                if (isAdded) {
                    baseActivity.showMessageLong("Error: response code is not \"0\". Please try again")
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {

        @JvmField
        val REQUEST_CODE_SUBSCRIPTION = 1001

        @JvmStatic
        fun newInstance(): SubscriptionsFragment {
            return SubscriptionsFragment()
        }
    }
}