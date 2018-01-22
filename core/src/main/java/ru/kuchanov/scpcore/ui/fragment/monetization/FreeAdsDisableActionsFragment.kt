package ru.kuchanov.scpcore.ui.fragment.monetization

import android.support.v7.widget.LinearLayoutManager
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.mvp.contract.monetization.FreeAdsDisableActionsContract
import ru.kuchanov.scpcore.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_free_ads_disable_actions.*

/**
 * Created by mohax on 22.01.2018.
 *
 * for ScpCore
 */
class FreeAdsDisableActionsFragment :
        BaseFragment<FreeAdsDisableActionsContract.View, FreeAdsDisableActionsContract.Presenter>(),
        FreeAdsDisableActionsContract.View {

    override fun getLayoutResId() = R.layout.fragment_free_ads_disable_actions

    override fun callInjections() = BaseApplication.getAppComponent().inject(this)

    override fun initViews() {
        //todo
        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
    }

    override fun showData(data: List<MyListItem>) {
        //todo
    }

    companion object {
        @JvmStatic
        fun newInstance(): FreeAdsDisableActionsFragment {
            return FreeAdsDisableActionsFragment()
        }
    }
}