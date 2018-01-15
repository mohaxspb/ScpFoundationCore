package ru.kuchanov.scpcore.controller.adapter.viewmodel

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes

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
        @DrawableRes val icon: Int
) : MyListItem