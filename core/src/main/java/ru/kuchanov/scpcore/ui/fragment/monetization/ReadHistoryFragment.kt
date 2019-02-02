package ru.kuchanov.scpcore.ui.fragment.monetization

import android.annotation.SuppressLint
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import kotlinx.android.synthetic.main.fragment_read_history.*
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.leaderboard.ReadHistoryDelegate
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.mvp.contract.monetization.ReadHistoryContract
import ru.kuchanov.scpcore.ui.fragment.BaseFragment
import java.text.SimpleDateFormat
import java.util.*


class ReadHistoryFragment :
        BaseFragment<ReadHistoryContract.View, ReadHistoryContract.Presenter>(),
        ReadHistoryContract.View {

    companion object {
        @SuppressLint("ConstantLocale")
        val simpleDateFormat = SimpleDateFormat("HH:mm dd.MM.yy", Locale.getDefault())

        @JvmField
        val TAG: String = ReadHistoryFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(): ReadHistoryFragment = ReadHistoryFragment()
    }

    private lateinit var adapter: ListDelegationAdapter<List<MyListItem>>

    override fun getLayoutResId() = R.layout.fragment_read_history

    override fun callInjections() = BaseApplication.getAppComponent().inject(this)

    override fun initViews() {
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        fastScroller.setRecyclerView(recyclerView)

        val animator = recyclerView.itemAnimator
        if (animator is DefaultItemAnimator) {
            animator.supportsChangeAnimations = false
        } else if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
        recyclerView.itemAnimator?.changeDuration = 0

        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(ReadHistoryDelegate(
                { presenter.onTranactionClicked(it) },
                { presenter.onTranactionDeleteClicked(it) }
        ))

        adapter = ListDelegationAdapter(delegateManager)
        recyclerView.adapter = adapter

        if (presenter.data.isNotEmpty()) {
            showProgressCenter(false)
            presenter.apply {
                showData(data)
            }
        } else {
            presenter.loadInitialData()
        }
    }

    override fun showProgressCenter(show: Boolean) {
        progressCenter.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showData(data: List<MyListItem>) {
        adapter.items = data
        adapter.notifyDataSetChanged()
    }

    override fun openArticle(articleUrl: String) {
        baseActivity?.startArticleActivity(listOf(articleUrl), 0)
    }

    override fun getToolbarTitle(): Int = R.string.read_history

    override fun getToolbarTextColor(): Int = android.R.color.white
}
