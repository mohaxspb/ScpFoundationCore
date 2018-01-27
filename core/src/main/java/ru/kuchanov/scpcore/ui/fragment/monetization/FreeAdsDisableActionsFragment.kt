package ru.kuchanov.scpcore.ui.fragment.monetization

import android.support.v7.widget.LinearLayoutManager
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import kotlinx.android.synthetic.main.fragment_free_ads_disable_actions.*
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.delegate.LabelDelegate
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.mvp.contract.monetization.FreeAdsDisableActionsContract
import ru.kuchanov.scpcore.ui.base.BaseFragment
import ru.kuchanov.scpcore.util.toStringWithLineBreaks
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
        delegateManager.addDelegate(LabelDelegate())
        //todo add delegate

        adapter = ListDelegationAdapter(delegateManager)
        recyclerView.adapter = adapter

        if (presenter.data.isEmpty()) {
            presenter.createData()
        }
        showData(presenter.data)
    }

    override fun showData(data: List<MyListItem>) {
        Timber.d("showData: ${data.toStringWithLineBreaks()}")


    }

    companion object {
        @JvmStatic
        fun newInstance(): FreeAdsDisableActionsFragment = FreeAdsDisableActionsFragment()
    }
}


