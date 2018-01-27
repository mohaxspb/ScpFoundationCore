package ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.subscriptions

import android.support.annotation.StringRes
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem

/**
 * Created by mohax on 15.01.2018.
 *
 * for ScpCore
 */
data class LabelWithPercentViewModel(
        @StringRes val text: Int,
        val price: String,
        val percent: String
) : MyListItem