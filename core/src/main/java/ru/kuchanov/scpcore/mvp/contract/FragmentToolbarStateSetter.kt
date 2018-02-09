package ru.kuchanov.scpcore.mvp.contract

import android.support.annotation.ColorRes
import android.support.annotation.StringRes

/**
 * Created by mohax on 28.01.2018.
 *
 * for ScpCore
 */
interface FragmentToolbarStateSetter {
    @StringRes
    fun getToolbarTitle(): Int

    @ColorRes
    fun getToolbarTextColor(): Int
}