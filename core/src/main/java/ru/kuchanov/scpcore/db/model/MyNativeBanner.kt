package ru.kuchanov.scpcore.db.model

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class MyNativeBanner(
        @PrimaryKey
        @Index
        var id: Long = 0,
        var imageUrl: String? = "",
        var logoUrl: String? = "",
        var title: String? = "",
        var subTitle: String? = "",
        var ctaButtonText: String? = "",
        var redirectUrl: String? = "",
        var enabled: Boolean? = false,
        var authorId: Long? = 0,
        var created: String? = "",
        var updated: String? = "",
        var bannerType: String? = null
) : RealmObject() {

    companion object {
        @JvmField
        val FIELD_ID = "id"
        @JvmField
        val FIELD_IMAGE_URL = "imageUrl"
        @JvmField
        val FIELD_LOGO_URL = "logoUrl"
        @JvmField
        val FIELD_TITLE = "title"
        @JvmField
        val FIELD_SUB_TITLE = "subTitle"
        @JvmField
        val FIELD_CTA_BUTTON_TEXT = "ctaButtonText"
        @JvmField
        val FIELD_REDIRECT_URL = "redirectUrl"
        @JvmField
        val FIELD_ENABLED = "enabled"
        @JvmField
        val FIELD_AUTHOR_ID = "authorId"
        @JvmField
        val FIELD_CREATED = "created"
        @JvmField
        val FIELD_UPDATED = "updated"
        @JvmField
        val FIELD_BANNER_TYPE = "bannerType"
    }

    override fun toString(): String {
        return "MyNativeBanner(id=$id, imageUrl=$imageUrl, logoUrl=$logoUrl, title=$title, subTitle=$subTitle, ctaButtonText=$ctaButtonText, redirectUrl=$redirectUrl, enabled=$enabled, authorId=$authorId, created=$created, updated=$updated, bannerType=$bannerType)"
    }
}

enum class BannerType {
    QUIZ, ART
}
