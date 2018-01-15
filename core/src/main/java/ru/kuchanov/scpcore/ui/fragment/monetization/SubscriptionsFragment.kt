package ru.kuchanov.scpcore.ui.fragment.monetization

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import butterknife.ButterKnife
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import kotlinx.android.synthetic.main.fragment_subscriptions.*
import org.json.JSONException
import org.json.JSONObject
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.delegate.TextDelegate
import ru.kuchanov.scpcore.controller.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.viewmodel.TextViewModel
import ru.kuchanov.scpcore.manager.InAppBillingServiceConnectionObservable
import ru.kuchanov.scpcore.monetization.util.InAppHelper
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsContract
import ru.kuchanov.scpcore.ui.adapter.BaseRecyclerAdapter
import ru.kuchanov.scpcore.ui.base.BaseFragment
import timber.log.Timber

class SubscriptionsFragment :
        BaseFragment<SubscriptionsContract.View, SubscriptionsContract.Presenter>(),
        SubscriptionsContract.View {

    private val items: MutableList<MyListItem> = mutableListOf()
    private lateinit var adapter: ListDelegationAdapter<List<MyListItem>>

    override fun callInjections() = BaseApplication.getAppComponent().inject(this)

    override fun getLayoutResId(): Int = R.layout.fragment_subscriptions

    override fun initViews() {
        InAppBillingServiceConnectionObservable.getInstance().serviceStatusObservable
                .subscribe { connected ->
                    if (connected!! && !getPresenter().isDataLoaded) {
                        getPresenter().getMarketData(baseActivity.getIInAppBillingService())
                    }
                }

//        getPresenter().getMarketData(baseActivity.getIInAppBillingService())

        val freeDownloadEnabled = FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_ALL_ENABLED_FOR_FREE)

        //        dialogTitle.setText(freeDownloadEnabled
        //                ? R.string.dialog_title_subscriptions : R.string.dialog_title_subscriptions_disabled_free_downloads);
        //        freeActions.setText(freeDownloadEnabled
        //                ? R.string.remove_ads_for_free : R.string.remove_ads_for_free_disabled_free_downloads);

        recyclerView.layoutManager = LinearLayoutManager(activity)
        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(TextDelegate())
        adapter = ListDelegationAdapter(delegateManager);
        recyclerView.adapter = adapter
    }

    override fun showProgressCenter(show: Boolean) = progressCenter.setVisibility(if (show) VISIBLE else GONE)

    override fun showRefreshButton(show: Boolean) = refresh.setVisibility(if (show) VISIBLE else GONE)

    //    @OnClick(R2.id.removeAdsOneDay)
    internal fun onRemoveAdsOneDayClick() {
        baseActivity.showFreeAdsDisablePopup()
    }

    //    @OnClick(R2.id.refresh)
    internal fun onRefreshClick() {
        getPresenter().getMarketData(baseActivity.getIInAppBillingService())
    }

    //    @OnClick(R2.id.refreshCurrentSubscriptions)
    internal fun onRefreshCurrentSubscriptionsClick() {
        getPresenter().getMarketData(baseActivity.getIInAppBillingService())
    }

    override fun showData() {
        items.add(TextViewModel(getString(R.string.subs_main_text)))
        //todo

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