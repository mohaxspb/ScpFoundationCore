package ru.kuchanov.scpcore.ui.fragment.monetization

import android.content.SharedPreferences
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v7.widget.LinearLayoutManager
import android.view.View.GONE
import android.view.View.VISIBLE
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import kotlinx.android.synthetic.main.fragment_subscriptions.*
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.DividerDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.LabelDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.TextDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.subscriptions.CurSubsDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.subscriptions.CurSubsEmptyDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.subscriptions.InAppDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.subscriptions.LabelWithPercentDelegate
import ru.kuchanov.scpcore.controller.adapter.viewmodel.DividerViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.LabelViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.adapter.viewmodel.TextViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.subscriptions.CurSubsEmptyViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.subscriptions.CurSubsViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.subscriptions.InAppViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.subscriptions.LabelWithPercentViewModel
import ru.kuchanov.scpcore.manager.InAppBillingServiceConnectionObservable
import ru.kuchanov.scpcore.manager.MyPreferenceManager.Keys
import ru.kuchanov.scpcore.monetization.model.Item
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.InappPurchaseUtil
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsContract
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsScreenContract
import ru.kuchanov.scpcore.mvp.presenter.monetization.SubscriptionsPresenter
import ru.kuchanov.scpcore.mvp.presenter.monetization.SubscriptionsPresenter.Companion.ID_CURRENT_SUBS
import ru.kuchanov.scpcore.mvp.presenter.monetization.SubscriptionsPresenter.Companion.ID_CURRENT_SUBS_EMPTY
import ru.kuchanov.scpcore.mvp.presenter.monetization.SubscriptionsPresenter.Companion.ID_FREE_ADS_DISABLE
import ru.kuchanov.scpcore.ui.activity.BaseActivity
import ru.kuchanov.scpcore.ui.activity.SubscriptionsActivity
import ru.kuchanov.scpcore.ui.fragment.BaseFragment
import ru.kuchanov.scpcore.util.SystemUtils
import timber.log.Timber
import javax.inject.Inject

class SubscriptionsFragment :
        BaseFragment<SubscriptionsContract.View, SubscriptionsContract.Presenter>(),
        SubscriptionsContract.View, SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var inAppHelper: InAppHelper

    private lateinit var adapter: ListDelegationAdapter<List<MyListItem>>

    override fun callInjections() = BaseApplication.getAppComponent().inject(this)

    override fun getLayoutResId() = R.layout.fragment_subscriptions

    override fun initViews() {
        InAppBillingServiceConnectionObservable.getInstance()
                .serviceStatusObservable.subscribe { connected ->
            if (!isAdded) {
                return@subscribe
            }
            if (connected!! && !getPresenter().isDataLoaded && isAdded && activity is BaseActivity<*, *>) {
                presenter.getMarketData()
            }
        }

        recyclerView.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(DividerDelegate())
        delegateManager.addDelegate(TextDelegate())
        delegateManager.addDelegate(LabelDelegate())
        delegateManager.addDelegate(LabelWithPercentDelegate())
        delegateManager.addDelegate(
                InAppDelegate { id ->
                    if (id == ID_FREE_ADS_DISABLE) {
                        navigateToDisableAds()
                    } else {
                        this@SubscriptionsFragment.baseActivity?.getPresenter()?.onPurchaseClick(
                                id,
                                false
                        )
                    }
                }
        )
        delegateManager.addDelegate(
                CurSubsDelegate(
                        {
                            getPresenter().onCurrentSubscriptionClick(it)
                        },
                        {
                            this@SubscriptionsFragment.getPresenter().getMarketData()
                        }
                )
        )
        delegateManager.addDelegate(
                CurSubsEmptyDelegate(
                        {
                            this@SubscriptionsFragment.baseActivity?.getPresenter()?.onPurchaseClick(
                                    inAppHelper.getNewInAppsSkus().first(),
                                    false
                            )
                        },
                        {
                            this@SubscriptionsFragment.getPresenter().getMarketData()
                        }
                )
        )
        adapter = ListDelegationAdapter(delegateManager)
        recyclerView.adapter = adapter

        refresh.setOnClickListener { getPresenter().getMarketData() }

        if (presenter.owned == null) {
            getPresenter().getMarketData()
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

    override fun showData(
            owned: List<Item>,
            toBuy: List<Subscription>,
            inApps: List<Subscription>,
            curSubsType: Int
    ) {
//        Timber.d("showData curSubsType: $curSubsType, owned: $owned, toBuy: $toBuy")
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
                )
        )
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
                )
        )
        //cur sub
        items.add(LabelViewModel(R.string.subs_cur_label))
        when (curSubsType) {
            InappPurchaseUtil.SubscriptionType.NONE -> {
                items.add(CurSubsEmptyViewModel(ID_CURRENT_SUBS_EMPTY))
            }
            InappPurchaseUtil.SubscriptionType.NO_ADS -> {
                items.add(
                        CurSubsViewModel(
                                R.string.subs_no_ads_title,
                                R.string.subs_no_ads_description,
                                ID_CURRENT_SUBS,
                                R.drawable.ic_adblock
                        )
                )
            }
            else -> {
                val item: Item = when {
                    owned.any { it.sku.contains("12") } -> owned.find { it.sku.contains("12") }
                    owned.any { it.sku.contains("6") } -> owned.find { it.sku.contains("6") }
                    owned.any { it.sku.contains("3") } -> owned.find { it.sku.contains("3") }
                    owned.any { it.sku.contains("1") } -> owned.find { it.sku.contains("1") }
                    else -> throw IllegalArgumentException("unexpected subs period")
                } ?: throw IllegalArgumentException("item is null! wtf?!")

                Timber.d("ownedItem: $item")

                //title icon for all types, as amazon do not return concrete type, just parent
                @StringRes
                val title: Int = R.string.subscription_full_version_title
                @StringRes
                val description: Int = R.string.subs_full_description
                //one icon for all types, as amazon do not return concrete type, just parent
                @DrawableRes
                val icon: Int = R.drawable.ic_check_circle_black_24dp
                when (SubscriptionsPresenter.getMonthFromSkuId(item.sku)) {
                    1 -> {
//                        title = R.string.subs_1_month_title
//                        icon = R.drawable.ic_scp_icon_laborant
                    }
                    3 -> {
//                        title = R.string.subs_3_month_title
//                        icon = R.drawable.ic_scp_icon_mns
                    }
                    6 -> {
//                        title = R.string.subs_6_month_title
//                        icon = R.drawable.ic_scp_icon_ns
                    }
                    12 -> {
//                        title = R.string.subs_12_month_title
//                        icon = R.drawable.ic_scp_icon_sns
                    }
                    else -> throw IllegalArgumentException("unexpected subs period")
                }

                items.add(
                        CurSubsViewModel(
                                title,
                                description,
                                ID_CURRENT_SUBS,
                                icon
                        )
                )
            }
        }
        //bottom panel
        val bgColor = R.color.bgSubsBottom
        items.add(
                DividerViewModel(
                        bgColor = bgColor,
                        height = resources.getDimensionPixelSize(R.dimen.defaultMarginMedium)
                )
        )

        //no ads
        val noAdsSubsEnabled = FirebaseRemoteConfig.getInstance()
                .getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)

        val textColor = R.color.subsTextColorBottom

        val subsFullOneMonth = toBuy
                .asSequence()
                .filter { it.productId !in inAppHelper.getNewNoAdsSubsSkus() }
                .first { SubscriptionsPresenter.getMonthFromSkuId(it.productId) == 1 }

        toBuy.sortedWith(
                Comparator { t1, t2 ->
                    t1.price_amount_micros.compareTo(t2.price_amount_micros)
                }
        )
                .forEach {
                    if (noAdsSubsEnabled && (it.productId in inAppHelper.getNewNoAdsSubsSkus())) {
                        items.add(
                                LabelViewModel(
                                        R.string.subs_no_ads_label,
                                        textColor = textColor,
                                        bgColor = bgColor
                                )
                        )
                        items.add(
                                InAppViewModel(
                                        R.string.subs_no_ads_title,
                                        R.string.subs_no_ads_description,
                                        it.price,
                                        it.productId,
                                        R.drawable.ic_adblock,
                                        bgColor
                                )
                        )
                    } else {
                        @StringRes
                        val label: Int
                        @StringRes
                        val title: Int
                        @StringRes
                        val description: Int = R.string.subs_full_description
                        @DrawableRes
                        val icon: Int

                        val month = SubscriptionsPresenter.getMonthFromSkuId(it.productId)
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
                                        if (month != 1) {
                                            (oneMonthPriceForMonths / 1000000L).toString() + SystemUtils.getCurrencySymbol2(it.price_currency_code)
                                        } else {
                                            ""
                                        },
                                        if (month != 1) {
                                            percent.toString()
                                        } else {
                                            ""
                                        }
                                )
                        )
                        items.add(
                                InAppViewModel(
                                        title,
                                        description,
                                        it.price,
                                        it.productId,
                                        icon,
                                        bgColor
                                )
                        )
                    }
                }

        adapter.items = items
        adapter.notifyDataSetChanged()
    }

    override fun navigateToDisableAds() = (baseActivity as SubscriptionsActivity)
            .showScreen(SubscriptionsScreenContract.Screen.FREE_ACTIONS)

    override fun navigateToLeaderboard() = (baseActivity as SubscriptionsActivity)
            .showScreen(SubscriptionsScreenContract.Screen.LEADERBOARD)

    override fun getToolbarTitle() = R.string.subs_activity_title

    override fun getToolbarTextColor() = android.R.color.white

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            Keys.HAS_NO_ADS_SUBSCRIPTION, Keys.HAS_SUBSCRIPTION -> {
                this@SubscriptionsFragment.getPresenter().getMarketData()
            }
            else -> {
                //do nothing
            }
        }//do nothing
    }

    companion object {

        const val REQUEST_CODE_SUBSCRIPTION = 1001

        @JvmStatic
        fun newInstance(): SubscriptionsFragment = SubscriptionsFragment()
    }
}
