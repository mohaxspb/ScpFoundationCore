package ru.kuchanov.scpcore.controller.adapter.viewmodel

import android.support.annotation.StringRes

/**
 * Created by mohax on 15.01.2018.
 *
 * for ScpCore
 */
data class LabelWithPercentViewModel(
        @StringRes val text: Int,
        val price: String,
        val percent: Int
) : MyListItem