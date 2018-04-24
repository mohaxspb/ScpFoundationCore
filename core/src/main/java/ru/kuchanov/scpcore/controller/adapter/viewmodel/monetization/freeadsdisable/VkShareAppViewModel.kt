package ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.freeadsdisable

import android.support.annotation.ColorRes
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem

/**
 * Created by mohax on 27.01.2018.
 *
 * for ScpCore
 */
data class VkShareAppViewModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconUrl: String,
    @ColorRes val textColor: Int = R.color.freeAdsTextColor,
    @ColorRes val backgroundColor: Int = R.color.freeAdsBackgroundColor,
    @ColorRes val cardBackgroundColor: Int = R.color.freeAdsCardBackgroundColor
) : MyListItem