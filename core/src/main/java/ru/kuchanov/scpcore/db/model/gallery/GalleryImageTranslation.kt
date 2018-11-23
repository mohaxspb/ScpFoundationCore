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
        const val FIELD_ID = "id"
        const val FIELD_LANG_CODE = "langCode"
        const val FIELD_TRANSLATION = "translation"
        const val FIELD_AUTHOR_ID = "authorId"
        const val FIELD_APPROVED = "approved"
        const val FIELD_APPROVER_ID = "approverId"
        const val FIELD_CREATED = "created"
        const val FIELD_UPDATED = "updated"
    }

    constructor(
            translation: String
    ) : this() {
        this.translation = translation
    }
}