package ru.kuchanov.scpcore.ui.util

import android.graphics.Typeface
import android.support.v4.content.res.ResourcesCompat
import ru.kuchanov.scpcore.BaseApplication

object FontUtils {

    @JvmStatic
    fun getTypeFaceFromName(fontName: String): Typeface? {
        val context = BaseApplication.getAppInstance()
        val fontId = context.resources.getIdentifier(
            fontName.replace("font/", "").replace(".ttf", ""),
            "font",
            context.packageName
        )
        return ResourcesCompat.getFont(context, fontId)
    }
}