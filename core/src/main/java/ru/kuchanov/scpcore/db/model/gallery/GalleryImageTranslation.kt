package ru.kuchanov.scpcore.db.model.gallery

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class GalleryImageTranslation(
    @PrimaryKey
    var id: Int = 0,
    var langCode: String = "",
    var translation: String = "",
    var authorId: Int = 0,
    var approved: Boolean = false,
    var approverId: Int? = null,
    var created: String = "",
    var updated: String = ""
) : RealmObject() {
    companion object {
        @JvmField
        val FIELD_ID = "id"
        @JvmField
        val FIELD_LANG_CODE = "langCode"
        @JvmField
        val FIELD_TRANSLATION = "translation"
        @JvmField
        val FIELD_AUTHOR_ID = "authorId"
        @JvmField
        val FIELD_APPROVED = "approved"
        @JvmField
        val FIELD_APPROVER_ID = "approverId"
        @JvmField
        val FIELD_CREATED = "created"
        @JvmField
        val FIELD_UPDATED = "updated"
    }

    constructor(
        translation: String
    ) : this() {
        this.translation = translation
    }
}