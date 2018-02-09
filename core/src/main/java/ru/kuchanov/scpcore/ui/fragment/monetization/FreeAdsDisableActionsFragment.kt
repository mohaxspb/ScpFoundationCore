package ru.kuchanov.scpcore.ui.fragment.monetization

import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import com.vk.sdk.VKScope
import com.vk.sdk.VKSdk
import kotlinx.android.synthetic.main.fragment_free_ads_disable_actions.*
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.DividerDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.LabelDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.freeadsdisable.*
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.mvp.contract.monetization.FreeAdsDisableActionsContract
import ru.kuchanov.scpcore.ui.fragment.BaseFragment
import ru.kuchanov.scpcore.util.IntentUtils
import timber.log.Timber

/**
 * Created by mohax on 22.01.2018.
 *
 * for ScpCore
 */
class FreeAdsDisableActionsFragment :
        BaseFragment<FreeAdsDisableActionsContract.View, FreeAdsDisableActionsContract.Presenter>(),
        FreeAdsDisableActionsContract.View {

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
        Timber.d("onInviteFriendsClick")
        if (FirebaseAuth.getInstance().currentUser == null) {
            baseActivity.showOfferLoginPopup { _, _ -> IntentUtils.firebaseInvite(activity) }
        } else {
            IntentUtils.firebaseInvite(activity)
        }
    }

    override fun onRewardedVideoClick() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            baseActivity.showOfferLoginPopup { _, _ -> baseActivity.startRewardedVideoFlow() }
        } else {
            baseActivity.startRewardedVideoFlow()
        }
    }

    override fun onAuthClick() = baseActivity.showLoginProvidersPopup()

    override fun onAppInstallClick(id: String) {
        if (FirebaseAuth.getInstance().currentUser == null) {
            baseActivity.showOfferLoginPopup { _, _ -> IntentUtils.tryOpenPlayMarket(activity, id) }
        } else {
            IntentUtils.tryOpenPlayMarket(activity, id)
        }
    }

    override fun onVkLoginAttempt() = VKSdk.login(activity, VKScope.EMAIL, VKScope.GROUPS)

    override fun getToolbarTitle(): Int = R.string.free_ads_activity_title

    override fun getToolbarTextColor(): Int = R.color.freeAdsTextColor

    companion object {

        @JvmStatic
        fun newInstance(): FreeAdsDisableActionsFragment = FreeAdsDisableActionsFragment()
    }
}


