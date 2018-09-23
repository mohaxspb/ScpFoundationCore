package ru.kuchanov.scpcore.ui.util

import android.graphics.Typeface
import android.support.v4.content.res.ResourcesCompat
import ru.kuchanov.scpcore.BaseApplication

object FontUtils {

    const val DEFAULT_FONT_NAME = "roboto_regular"

    @JvmStatic
    fun getTypeFaceFromName(fontName: String): Typeface? {
        val context = BaseApplication.getAppInstance()
        val fontNameCleared = fontName.replace("font/", "").replace(".ttf", "")
        val fontId = context.resources.getIdentifier(
            fontNameCleared,
            "font",
            context.packageName
        )
        return if (fontId != 0) ResourcesCompat.getFont(context, fontId) else null
    }
}