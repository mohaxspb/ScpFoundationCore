package ru.kuchanov.scpcore.db.model.gallery

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R

open class GalleryImage(
        @PrimaryKey
        var id: Int = 0,
        var vkId: Int = 0,
        var imageUrl: String = "",
        var authorId: Int = 0,
        var approved: Boolean = false,
        var approverId: Int? = null,
        var created: String = "",
        var updated: String = "",
        var galleryImageTranslations: RealmList<GalleryImageTranslation> = RealmList()
) : RealmObject() {
    companion object {
        const val FIELD_ID = "id"
        const val FIELD_VK_ID = "vkId"
        const val FIELD_IMAGE_URL = "imageUrl"
        const val FIELD_AUTHOR_ID = "authorId"
        const val FIELD_APPROVED = "approved"
        const val FIELD_APPROVER_ID = "approverId"
        const val FIELD_CREATED = "created"
        const val FIELD_UPDATED = "updated"
        const val FIELD_GALLERY_IMAGE_TRANSLATIONS = "galleryImageTranslations"

        @JvmStatic
        fun getApiImageAddress(galleryImage: GalleryImage): String {
            return if (galleryImage.id == 0) {
                //so its manually constructed, not managed object, use imageUrl field
                galleryImage.imageUrl
            } else {
                //todo use constant for another address
                BaseApplication.getAppInstance().getString(R.string.scp_reader_api_url) + Constants.Api.GALLERY_FILES_PATH + galleryImage.id
            }
        }
    }

    constructor(
            imageUrl: String,
            galleryImageTranslation: GalleryImageTranslation
    ) : this() {
        this.galleryImageTranslations.add(galleryImageTranslation)
        this.imageUrl = imageUrl
    }
}