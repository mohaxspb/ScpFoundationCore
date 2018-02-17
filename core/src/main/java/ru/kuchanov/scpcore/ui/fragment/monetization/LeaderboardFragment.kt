package ru.kuchanov.scpcore.ui.fragment.monetization

import android.graphics.Bitmap
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.google.firebase.auth.FirebaseAuth
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import kotlinx.android.synthetic.main.fragment_leaderboard.*
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.DividerDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.LabelDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.leaderboard.LeaderboardDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.subscriptions.InAppDelegate
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard.LeaderboardUserViewModel
import ru.kuchanov.scpcore.manager.InAppBillingServiceConnectionObservable
import ru.kuchanov.scpcore.mvp.contract.monetization.LeaderboardContract
import ru.kuchanov.scpcore.ui.activity.BaseActivity
import ru.kuchanov.scpcore.ui.fragment.BaseFragment
import java.text.SimpleDateFormat
import java.util.*


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
            if(!isAdded){
                return@subscribe
            }
            if (connected && isAdded && activity is BaseActivity<*, *>) {
                getPresenter().inAppService = (activity as BaseActivity<*, *>).getIInAppBillingService()
                if (!getPresenter().isDataLoaded) {
                    getPresenter().loadData()
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        fastScroller.setRecyclerView(recyclerView)

        (recyclerView.getItemAnimator() as DefaultItemAnimator).supportsChangeAnimations = false
        val animator = recyclerView.itemAnimator

        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
        recyclerView.itemAnimator.changeDuration = 0

        swipeRefresh.setColorSchemeResources(R.color.zbs_color_red)
        swipeRefresh.setOnRefreshListener { mPresenter.updateLeaderboardFromApi() }

        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(DividerDelegate())
        delegateManager.addDelegate(LabelDelegate())
        delegateManager.addDelegate(LeaderboardDelegate())
        delegateManager.addDelegate(InAppDelegate { presenter.onSubscriptionClick(it, this) })

        adapter = ListDelegationAdapter(delegateManager)
        recyclerView.adapter = adapter

        if (presenter.data.isEmpty()) {
            enableSwipeRefresh(false)
            getPresenter().loadData()
        } else {
            showProgressCenter(false)
            presenter.apply { showData(data); onUserChanged(presenter.myUser); showUpdateDate(updateTime) }
        }

        refresh.setOnClickListener { getPresenter().loadData() }
    }

    override fun showProgressCenter(show: Boolean) {
        progressContainer.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showData(data: List<MyListItem>) {
        adapter.items = data
        adapter.notifyDataSetChanged()
    }

    override fun showSwipeRefreshProgress(show: Boolean) {
        swipeRefresh.isRefreshing = show
    }

    override fun enableSwipeRefresh(enable: Boolean) {
        swipeRefresh.isEnabled = enable
    }

    override fun showUser(myUser: LeaderboardUserViewModel?) {
        if (myUser == null) {
            userDataView.visibility = View.GONE
            return
        }
        userDataView.visibility = View.VISIBLE
        val user = myUser.user
        chartPlaceTextView.text = (myUser.position + 1).toString()

        Glide.with(context)
                .load(user.avatar)
                .asBitmap()
                .centerCrop()
                .error(R.mipmap.ic_launcher)
                .into(object : BitmapImageViewTarget(avatarImageView) {
                    override fun setResource(resource: Bitmap) {
                        val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.resources, resource)
                        circularBitmapDrawable.isCircular = true
                        avatarImageView.setImageDrawable(circularBitmapDrawable)
                    }
                })

        nameTextView.text = user.fullName
        readArticlesCountTextView.text = context.getString(R.string.leaderboard_articles_read, user.numOfReadArticles)
        userScoreTextView.text = user.score.toString()

        val levelViewModel = myUser.levelViewModel
        val level = myUser.levelViewModel.level
        levelNumTextView.text = level.id.toString()
        levelTextView.text = context.getString(R.string.level_num, level.id)
        if (levelViewModel.isMaxLevel) {
            maxLevelTextView.visibility = View.VISIBLE
            experienceProgressBar.max = 1
            experienceProgressBar.progress = 1
            expToNextLevelTextView.text = ""
        } else {
            maxLevelTextView.visibility = View.GONE
            experienceProgressBar.max = levelViewModel.nextLevelScore
            experienceProgressBar.progress = user.score - level.score
            expToNextLevelTextView.text = context.getString(R.string.score_num, levelViewModel.scoreToNextLevel)
        }
    }

    override fun showUpdateDate(lastUpdated: Long) {
        baseActivity.getSupportActionBar()?.apply {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = lastUpdated
            val simpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val refreshed = simpleDateFormat.format(calendar.time)

            subtitle = getString(R.string.refreshed, refreshed)
        }
    }

    override fun showRefreshButton(show: Boolean) {
        refresh.visibility = if (show) VISIBLE else GONE
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


