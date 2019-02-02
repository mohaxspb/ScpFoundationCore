package ru.kuchanov.scpcore.mvp.contract.monetization

import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.mvp.base.BaseMvp
import ru.kuchanov.scpcore.mvp.contract.FragmentToolbarStateSetter

interface ReadHistoryContract : BaseMvp {

    interface View : BaseMvp.View, FragmentToolbarStateSetter {
        fun showProgressCenter(show: Boolean)
        fun showData(data: List<MyListItem>)
        fun openArticle(articleUrl: String)
    }

    interface Presenter : BaseMvp.Presenter<View> {
        var data: MutableList<MyListItem>

        fun loadInitialData()
        fun onTranactionClicked(articleUrl: String)
        fun onTranactionDeleteClicked(id: Long)
    }
}