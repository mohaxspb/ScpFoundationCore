package ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.subscriptions

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem

/**
 * Created by mohax on 15.01.2018.
 *
 * for ScpCore
 */
data class InAppViewModel(
        @StringRes val title: Int,
        @StringRes val description: Int,
        val price: String,
        val id: String,
        @DrawableRes val icon: Int,
        @ColorRes val background: Int = R.color.bgSubsTop
) : MyListItem