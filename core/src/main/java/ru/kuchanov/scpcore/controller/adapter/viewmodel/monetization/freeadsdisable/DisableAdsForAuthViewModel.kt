package ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.freeadsdisable

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem

/**
 * Created by mohax on 27.01.2018.
 *
 * for ScpCore
 */
data class DisableAdsForAuthViewModel(
        @StringRes val title: Int,
        val subtitle: String,
        @DrawableRes val icon: Int = R.drawable.ic_login_variant,
        @ColorRes val textColor: Int = R.color.freeAdsTextColor,
        @ColorRes val backgroundColor: Int = R.color.freeAdsBackgroundColor,
        @ColorRes val cardBackgroundColor: Int = R.color.freeAdsCardBackgroundColor
) : MyListItem