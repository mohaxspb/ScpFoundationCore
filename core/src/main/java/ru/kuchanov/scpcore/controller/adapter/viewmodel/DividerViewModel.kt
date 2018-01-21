package ru.kuchanov.scpcore.controller.adapter.viewmodel

import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import ru.kuchanov.scpcore.R

/**
 * Created by mohax on 15.01.2018.
 *
 * for ScpCore
 */
data class DividerViewModel(
        @ColorRes val bgColor: Int = R.color.bgSubsBottom,
        val height: Int
) : MyListItem