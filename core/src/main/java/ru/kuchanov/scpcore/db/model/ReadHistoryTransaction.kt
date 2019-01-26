package ru.kuchanov.scpcore.db.model

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class ReadHistoryTransaction(
        @PrimaryKey
        @Index
        var id: Long = 0,
        var title: String? = "",
        var url: String = "",
        var created: Long = 0
) : RealmObject() {

    override fun toString(): String {
        return "ReadHistoryTransaction(id=$id, title=$title, url=$url, created=$created)"
    }

    companion object {
        @JvmField
        val FIELD_ID = "id"
        @JvmField
        val FIELD_TITLE = "title"
        @JvmField
        val FIELD_URL = "url"
        @JvmField
        val FIELD_CREATED = "created"
    }
}