package ru.kuchanov.scpcore.ui.fragment.monetization

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import butterknife.ButterKnife
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.android.synthetic.main.fragment_subscriptions.*
import org.json.JSONException
import org.json.JSONObject
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.manager.InAppBillingServiceConnectionObservable
import ru.kuchanov.scpcore.monetization.util.InAppHelper
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsContract
import ru.kuchanov.scpcore.ui.base.BaseFragment
import timber.log.Timber

class SubscriptionsFragment :
        BaseFragment<SubscriptionsContract.View, SubscriptionsContract.Presenter>(),
        SubscriptionsContract.View {

//    @BindView(R2.id.progressCenter)
//    lateinit var progressCenter: ProgressBar
//    @BindView(R2.id.refresh)
//    internal var refresh: View? = null
//    @BindView(R2.id.recyclerView)
//    internal var recyclerView: RecyclerView? = null

//    @Inject
//    var mMyPreferenceManager: MyPreferenceManager? = null
//    @Inject
//    var mInAppHelper: InAppHelper? = null

//    private var mInAppBillingService: IInAppBillingService? = null

//    private val isDataLoaded: Boolean = false

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
    }

    override fun showProgressCenter(show: Boolean) = progressCenter.setVisibility(if (show) VISIBLE else GONE)

    override fun showRefreshButton(show: Boolean) = refresh.setVisibility(if (show) VISIBLE else GONE)

    //    @OnClick(R2.id.removeAdsOneDay)
    internal fun onRemoveAdsOneDayClick() {
        baseActivity.showFreeAdsDisablePopup()
    }

    //    @OnClick(R2.id.refresh)
    internal fun onRefreshClick() {
        getMarketData()
    }

    //    @OnClick(R2.id.refreshCurrentSubscriptions)
    internal fun onRefreshCurrentSubscriptionsClick() {
        getMarketData()
    }

    private fun getMarketData() {
        if (!isAdded) {
            return
        }
//        mInAppBillingService = baseActivity.getIInAppBillingService()

//        refresh!!.visibility = View.GONE
//        progressCenter!!.visibility = View.VISIBLE

//        val skuList = mInAppHelper!!.newSubsSkus
//        if (FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)) {
//            skuList.addAll(mInAppHelper!!.newNoAdsSubsSkus)
//        }

        //todo
//                mInAppHelper.getValidatedOwnedSubsObservable(mInAppBillingService)
//                        .flatMap(ownedItems -> mInAppHelper.getSubsListToBuyObservable(mInAppBillingService, skuList)
//                                .flatMap(toBuy -> Observable.just(new Pair<>(ownedItems, toBuy))))
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(
//                                ownedItemsAndSubscriptions -> {
//                                    if (!isAdded()) {
//                                        return;
//                                    }
//        //                            Timber.d("itemsOwned: %s", ownedItemsAndSubscriptions.first);
//        //                            Timber.d("subsToBuy: %s", ownedItemsAndSubscriptions.second);
//
//                                    isDataLoaded = true;
//                                    refresh.setVisibility(View.GONE);
//                                    progressCenter.setVisibility(View.GONE);
//                                    infoContainer.setVisibility(View.VISIBLE);
//
//                                    //show current subscription
//                                    @InAppHelper.SubscriptionType
//                                    int type = mInAppHelper.getSubscriptionTypeFromItemsList(ownedItemsAndSubscriptions.first);
//                                    switch (type) {
//                                        case InAppHelper.SubscriptionType.NONE:
//                                            currentSubscriptionValue.setText(getString(R.string.no_subscriptions));
//                                            break;
//                                        case InAppHelper.SubscriptionType.NO_ADS:
//                                            currentSubscriptionValue.setText(getString(R.string.subscription_no_ads_title));
//                                            break;
//                                        case InAppHelper.SubscriptionType.FULL_VERSION:
//                                            currentSubscriptionValue.setText(getString(R.string.subscription_full_version_title));
//                                            break;
//                                        default:
//                                            throw new IllegalArgumentException("unexected subs type: " + type);
//                                    }
//
//                                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//                                    recyclerView.setHasFixedSize(true);
//                                    SubscriptionsAdapter adapter = new SubscriptionsAdapter();
//                                    adapter.setData(ownedItemsAndSubscriptions.second);
//                                    adapter.setArticleClickListener(SubscriptionsFragment.this);
//                                    recyclerView.setAdapter(adapter);
//                                },
//                                e -> {
//                                    if (!isAdded()) {
//                                        return;
//                                    }
//                                    Timber.e(e, "error getting cur subs");
//                                    isDataLoaded = false;
//
//                                    Snackbar.make(mRoot, e.getMessage(), Snackbar.LENGTH_SHORT).show();
//                                    progressCenter.setVisibility(View.GONE);
//                                    refresh.setVisibility(View.VISIBLE);
//                                }
//                        );
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