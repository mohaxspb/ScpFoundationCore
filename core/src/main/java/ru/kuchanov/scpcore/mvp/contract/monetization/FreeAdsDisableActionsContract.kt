package ru.kuchanov.scpcore.mvp.contract.monetization

import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.mvp.base.BaseMvp

/**
 * Created by y.kuchanov on 21.12.16.
 *
 *
 * for scp_ru
 */
interface FreeAdsDisableActionsContract : BaseMvp {
    interface View : BaseMvp.View {
        fun showData(data: List<MyListItem>)
    }

    interface Presenter : BaseMvp.Presenter<View> {
        val data: List<MyListItem>

        fun createData()
    }
}