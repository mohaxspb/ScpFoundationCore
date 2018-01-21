package ru.kuchanov.scpcore.controller.adapter.viewmodel

import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import ru.kuchanov.scpcore.R

/**
 * Created by mohax on 15.01.2018.
 *
 * for ScpCore
 */
data class LabelViewModel(
        @StringRes val text: Int,
        @ColorRes val bgColor: Int = R.color.bgSubsTop,
        @ColorRes val textColor: Int = android.R.color.white
) : MyListItem