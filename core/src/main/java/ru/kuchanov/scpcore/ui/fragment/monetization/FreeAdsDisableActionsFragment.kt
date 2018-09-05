package ru.kuchanov.scpcore.ui.fragment.monetization

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.vk.sdk.VKScope
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import com.vk.sdk.api.model.VKApiPhoto
import com.vk.sdk.api.model.VKPhotoArray
import com.vk.sdk.dialogs.VKShareDialog
import com.vk.sdk.dialogs.VKShareDialogBuilder
import kotlinx.android.synthetic.main.fragment_free_ads_disable_actions.*
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.ConstantValues
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.DividerDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.LabelDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.freeadsdisable.*
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.mvp.base.BasePresenter
import ru.kuchanov.scpcore.mvp.contract.DataSyncActions
import ru.kuchanov.scpcore.mvp.contract.monetization.FreeAdsDisableActionsContract
import ru.kuchanov.scpcore.ui.fragment.BaseFragment
import ru.kuchanov.scpcore.util.IntentUtils
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by mohax on 22.01.2018.
 *
 * for ScpCore
 */
class FreeAdsDisableActionsFragment :
        BaseFragment<FreeAdsDisableActionsContract.View, FreeAdsDisableActionsContract.Presenter>(),
        FreeAdsDisableActionsContract.View {

    @Inject
    lateinit var constantValues: ConstantValues

    private lateinit var adapter: ListDelegationAdapter<List<MyListItem>>

    override fun getLayoutResId() = R.layout.fragment_free_ads_disable_actions

    override fun callInjections() = BaseApplication.getAppComponent().inject(this)

    override fun initViews() {
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(DividerDelegate())
        delegateManager.addDelegate(InviteFriendsDelegate { presenter.onInviteFriendsClick() })
        delegateManager.addDelegate(RewardedVideoDelegate { presenter.onRewardedVideoClick() })
        delegateManager.addDelegate(DisableAdsForAuthDelegate { presenter.onAuthClick() })
        delegateManager.addDelegate(LabelDelegate())
        delegateManager.addDelegate(AppToInstallDelegate { presenter.onAppInstallClick(it) })
        delegateManager.addDelegate(VkGroupToJoinDelegate { presenter.onVkGroupClick(it) })
        delegateManager.addDelegate(VkShareAppDelegate { presenter.onVkShareAppClick() })

        adapter = ListDelegationAdapter(delegateManager)
        recyclerView.adapter = adapter

        if (presenter.data.isEmpty()) {
            presenter.createData()
        }
        showData(presenter.data)
    }

    override fun showData(data: List<MyListItem>) {
        adapter.items = data
        adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    override fun onInviteFriendsClick() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            baseActivity?.showOfferLoginPopup { _, _ -> IntentUtils.firebaseInvite(activity) }
        } else {
            IntentUtils.firebaseInvite(activity)
        }
    }

    override fun onRewardedVideoClick() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            baseActivity?.showOfferLoginPopup { _, _ -> baseActivity?.startRewardedVideoFlow() }
        } else {
            baseActivity?.startRewardedVideoFlow()
        }
    }

    override fun onAuthClick() = baseActivity?.showLoginProvidersPopup() ?: Unit


    override fun onAppInstallClick(id: String) {
        if (FirebaseAuth.getInstance().currentUser == null) {
            baseActivity?.showOfferLoginPopup { _, _ ->
                val linkToMarket = "https://play.google.com/store/apps/details?id=$id&utm_source=scpReader&utm_medium=free_ads_disable&utm_campaign=${constantValues.appLang}"
                IntentUtils.openUrl(linkToMarket)
            }
        } else {
            val linkToMarket = "https://play.google.com/store/apps/details?id=$id&utm_source=scpReader&utm_medium=free_ads_disable&utm_campaign=${constantValues.appLang}"
            IntentUtils.openUrl(linkToMarket)
        }
    }

    override fun showVkShareDialog() {
        val builder = VKShareDialogBuilder()
        builder.setText(getString(R.string.share_app_vk_text))

        val photos = VKPhotoArray()
        photos.add(VKApiPhoto(VK_APP_SHARE_IMAGE))
        builder.setUploadedPhotos(photos)
        builder.setAttachmentLink(
            getString(R.string.app_name),
            getString(R.string.share_app_vk_link, BaseApplication.getAppInstance().packageName)
        )
        builder.setShareDialogListener(object : VKShareDialog.VKShareDialogListener {
            override fun onVkShareComplete(postId: Int) {
                FirebaseAnalytics.getInstance(BaseApplication.getAppInstance()).logEvent(
                    Constants.Firebase.Analitics.EventName.VK_APP_SHARED,
                    Bundle()
                )

                presenter.applyAwardFromVkShare()

                presenter.updateUserScoreForScoreAction(
                    DataSyncActions.ScoreAction.VK_APP_SHARE,
                    object : BasePresenter.AddScoreListener {
                        override fun onSuccess() {
                            presenter.createData()
                            showData(presenter.data)
                        }

                        override fun onError() {}

                    }
                )
            }

            override fun onVkShareCancel() {
            }

            override fun onVkShareError(error: VKError) {
                Timber.e("error: $error/${error.errorMessage}")
                showError(Exception(error.errorMessage))
            }
        })
        builder.show(fragmentManager, "VK_SHARE_DIALOG");
    }

    override fun onVkLoginAttempt() = VKSdk.login(baseActivity!!, VKScope.EMAIL, VKScope.GROUPS, VKScope.WALL)

    override fun getToolbarTitle(): Int = R.string.free_ads_activity_title

    override fun getToolbarTextColor(): Int = R.color.freeAdsTextColor

    companion object {

        @JvmStatic
        fun newInstance(): FreeAdsDisableActionsFragment = FreeAdsDisableActionsFragment()

        const val VK_APP_SHARE_IMAGE = "photo-599638_456239255"
    }
}


