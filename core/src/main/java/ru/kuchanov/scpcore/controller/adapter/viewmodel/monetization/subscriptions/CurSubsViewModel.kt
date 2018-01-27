package ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.subscriptions

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem

/**
 * Created by mohax on 15.01.2018.
 *
 * for ScpCore
 */
data class CurSubsViewModel(
        @StringRes val title: Int,
        @StringRes val description: Int,
        val id: String,
        @DrawableRes val icon: Int
) : MyListItem