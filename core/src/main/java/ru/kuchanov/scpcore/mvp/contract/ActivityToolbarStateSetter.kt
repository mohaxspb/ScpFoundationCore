package ru.kuchanov.scpcore.mvp.contract

import android.support.annotation.ColorRes
import android.support.annotation.StringRes

/**
 * Created by mohax on 28.01.2018.
 *
 * for ScpCore
 */
interface ActivityToolbarStateSetter {
    fun setToolbarTitle(title: String)

    fun setToolbarTitle(@StringRes title: Int)

    fun setToolbarTextColor(@ColorRes toolbarTextColor: Int)
}