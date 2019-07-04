package ru.kuchanov.scpcore.ui.dialog

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.SwitchCompat
import android.text.Html
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.OnCheckedChanged
import butterknife.OnClick
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.R2
import ru.kuchanov.scpcore.manager.MyNotificationManager
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper
import ru.kuchanov.scpcore.ui.activity.SubscriptionsActivity
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.subscribeBy
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class AdsSettingsBottomSheetDialogFragment : BaseBottomSheetDialogFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var mMyPreferenceManager: MyPreferenceManager

    @Inject
    lateinit var mMyNotificationManager: MyNotificationManager

    @Inject
    lateinit var inAppHelper: InAppHelper

    //ads
    //todo replace with kotlin synthetic
    @BindView(R2.id.adsInListsBannerSwitch)
    lateinit var adsInListsBannerSwitch: SwitchCompat

    @BindView(R2.id.adsInListsNativeSwitch)
    lateinit var adsInListsNativeSwitch: SwitchCompat

    @BindView(R2.id.adsInArticleBannerSwitch)
    lateinit var adsInArticleBannerSwitch: SwitchCompat

    @BindView(R2.id.adsInArticleNativeSwitch)
    lateinit var adsInArticleNativeSwitch: SwitchCompat

    @BindView(R2.id.adsRemovedTextView)
    lateinit var adsRemovedTextView: View

    @BindView(R2.id.adsRemoveActionsView)
    lateinit var adsRemoveActionsView: View

    @BindView(R2.id.removeAdsForMonth)
    lateinit var removeAdsForMonth: TextView

    @BindView(R2.id.removeAdsForFree)
    lateinit var removeAdsForFree: TextView

    override fun getLayoutResId() = R.layout.ads_settings

    protected override fun callInjection() {
        BaseApplication.getAppComponent().inject(this)
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        adsInListsBannerSwitch.isChecked = mMyPreferenceManager.isBannerInArticlesListsEnabled
        adsInListsNativeSwitch.isChecked = !mMyPreferenceManager.isBannerInArticlesListsEnabled
        adsInArticleBannerSwitch.isChecked = mMyPreferenceManager.isBannerInArticleEnabled
        adsInArticleNativeSwitch.isChecked = !mMyPreferenceManager.isBannerInArticleEnabled

        //set text for remove ads price
        val noAdsSku: String
        if (FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)) {
            noAdsSku = inAppHelper.getNewNoAdsSubsSkus().first()
        } else {
            noAdsSku = inAppHelper.getNewSubsSkus().first()
        }

        val skus = ArrayList<String>()
        skus.add(noAdsSku)
        inAppHelper.getSubsListToBuyObservable(skus)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { subscriptions ->
                    if (subscriptions.isEmpty()) {
                        Single.error(IllegalArgumentException("empty subs list"))
                    } else {
                        Single.just(subscriptions)
                    }
                }
                .subscribeBy(
                        onSuccess = {
                            removeAdsForMonth.text = Html.fromHtml(
                                    getString(
                                            R.string.remove_ads_for_month,
                                            "<br/><b><font color = #4CAF50>${it.first().price}</font></b>"))
                        },
                        onError = { Timber.e(it) }
                )

        removeAdsForFree.text = Html.fromHtml(getString(R.string.remove_ads_for_free_for_hours))

        setRemoveAdsViewVisibility()
    }

    private fun setRemoveAdsViewVisibility() {
        val hasAnySubscription = mMyPreferenceManager.isHasAnySubscription

        adsRemovedTextView.visibility = if (hasAnySubscription) View.VISIBLE else View.INVISIBLE
        adsRemoveActionsView.visibility = if (hasAnySubscription) View.INVISIBLE else View.VISIBLE
    }

    @OnCheckedChanged(R2.id.adsInListsBannerSwitch)
    internal fun onBannerInArticlesListsEnabledCheckChangeListener(checked: Boolean) {
        Timber.d("setBannerInArticlesListsEnabled: %s", checked)
        mMyPreferenceManager.isBannerInArticlesListsEnabled = checked
        adsInListsNativeSwitch.isChecked = !checked
    }

    @OnCheckedChanged(R2.id.adsInListsNativeSwitch)
    internal fun onNativeInArticlesListsEnabledCheckChangeListener(checked: Boolean) {
        Timber.d("setNativeInArticlesListsEnabled: %s", checked)
        mMyPreferenceManager.isBannerInArticlesListsEnabled = !checked
        adsInListsBannerSwitch.isChecked = !checked
    }

    @OnCheckedChanged(R2.id.adsInArticleBannerSwitch)
    internal fun onBannerInArticleEnabledCheckChangeListener(checked: Boolean) {
        Timber.d("setBannerInArticleEnabled: %s", checked)
        mMyPreferenceManager.isBannerInArticleEnabled = checked
        adsInArticleNativeSwitch.isChecked = !checked
    }

    @OnCheckedChanged(R2.id.adsInArticleNativeSwitch)
    internal fun onNativeInArticleEnabledCheckChangeListener(checked: Boolean) {
        Timber.d("setNativeInArticleEnabled: %s", checked)
        mMyPreferenceManager.isBannerInArticleEnabled = !checked
        adsInArticleBannerSwitch.isChecked = !checked
    }

    @OnClick(R2.id.removeAdsForMonth)
    internal fun onRemoveAdsForMonthClick() {
        Timber.d("onRemoveAdsForMonthClick")
        val noAdsSku: String
        if (FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)) {
            noAdsSku = inAppHelper.getNewNoAdsSubsSkus()[0]
        } else {
            noAdsSku = inAppHelper.getNewSubsSkus()[0]
        }

        baseActivity.createPresenter().onPurchaseClick(noAdsSku, true)

        val bundle = Bundle()
        bundle.putString(Constants.Firebase.Analytics.EventParam.PLACE, Constants.Firebase.Analytics.StartScreen.REMOVE_ADS_SETTINGS)
        FirebaseAnalytics.getInstance(getBaseActivity()).logEvent(Constants.Firebase.Analytics.EventName.SUBSCRIPTIONS_SHOWN, bundle)
    }

    @OnClick(R2.id.removeAdsForFree)
    internal fun onRemoveAdsForFreeClick() {
        Timber.d("onRemoveAdsForFreeClick")
        dismiss()
        SubscriptionsActivity.start(getBaseActivity(), SubscriptionsActivity.TYPE_DISABLE_ADS_FOR_FREE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialog1 ->
            val d = dialog1 as BottomSheetDialog

            val bottomSheet = d.findViewById<FrameLayout>(android.support.design.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        // Do something with your dialog like setContentView() or whatever
        return dialog
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(activity).registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(activity).unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (!isAdded) {
            return
        }
        when (key) {
            MyPreferenceManager.Keys.HAS_NO_ADS_SUBSCRIPTION, MyPreferenceManager.Keys.HAS_SUBSCRIPTION -> setRemoveAdsViewVisibility()
            else -> {
                //do nothing
            }
        }//do nothing
    }

    companion object {
        @JvmStatic
        fun newInstance() = AdsSettingsBottomSheetDialogFragment()
    }
}
