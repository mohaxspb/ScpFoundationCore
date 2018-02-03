package ru.kuchanov.scpcore.ui.fragment.monetization

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import kotlinx.android.synthetic.main.fragment_leaderboard.*
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.DividerDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.LabelDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.freeadsdisable.RewardedVideoDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.leaderboard.LeaderboardDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.subscriptions.InAppDelegate
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.db.model.User
import ru.kuchanov.scpcore.manager.InAppBillingServiceConnectionObservable
import ru.kuchanov.scpcore.mvp.contract.monetization.LeaderboardContract
import ru.kuchanov.scpcore.ui.fragment.BaseFragment

/**
 * Created by mohax on 22.01.2018.
 *
 * for ScpCore
 */
class LeaderboardFragment :
        BaseFragment<LeaderboardContract.View, LeaderboardContract.Presenter>(),
        LeaderboardContract.View {

    private lateinit var adapter: ListDelegationAdapter<List<MyListItem>>

    override fun getLayoutResId() = R.layout.fragment_leaderboard

    override fun callInjections() = BaseApplication.getAppComponent().inject(this)

    override fun initViews() {
        InAppBillingServiceConnectionObservable.getInstance().serviceStatusObservable.subscribe { connected ->
            if (connected!! && !getPresenter().isDataLoaded) {
                getPresenter().loadData(baseActivity.getIInAppBillingService())
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(DividerDelegate())
        delegateManager.addDelegate(RewardedVideoDelegate { presenter.onRewardedVideoClick() })
        delegateManager.addDelegate(LabelDelegate())
        delegateManager.addDelegate(LeaderboardDelegate())
        delegateManager.addDelegate(InAppDelegate { presenter.onSubscriptionClick(it, this, baseActivity.getIInAppBillingService()) })

        adapter = ListDelegationAdapter(delegateManager)
        recyclerView.adapter = adapter

        if (presenter.data.isEmpty()) {
            baseActivity.getIInAppBillingService()?.apply { getPresenter().loadData(this) }
        } else {
            showProgressCenter(false)
            presenter.apply { showData(data) }
        }
    }

    override fun showProgressCenter(show: Boolean) = progressContainer.setVisibility(if (show) View.VISIBLE else View.GONE)

    override fun showData(data: List<MyListItem>) {
        adapter.items = data
        adapter.notifyDataSetChanged()
    }

    override fun showUser(myUser: User) {
        //todo
    }

    override fun showUpdateDate(lastUpdated: Long, timeZone: String) {
        //todo
    }

    override fun showRefreshButton(show: Boolean) {
        //todo
    }

    override fun onRewardedVideoClick() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            baseActivity.showOfferLoginPopup { _, _ -> baseActivity.startRewardedVideoFlow() }
        } else {
            baseActivity.startRewardedVideoFlow()
        }
    }

    override fun getToolbarTitle(): Int = R.string.leaderboard_activity_title

    override fun getToolbarTextColor(): Int = android.R.color.white

    companion object {

        @JvmStatic
        fun newInstance(): LeaderboardFragment = LeaderboardFragment()
    }
}


