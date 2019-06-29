package ru.kuchanov.scpcore.monetization.model

import com.google.gson.GsonBuilder

/**
 * Created by mohax on 14.01.2017.
 */
data class Item(
        val rawPurchaseData: String = "{ \"purchaseToken\":\"\"}",
        val signature: String = "",
        var sku: String,
        val continuationToken: String = ""
) {

    var purchaseData: PurchaseData

    init {
        this.purchaseData = GSON.fromJson(rawPurchaseData, PurchaseData::class.java)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val item = other as Item?

        return sku == item!!.sku
    }

    override fun hashCode(): Int {
        return sku.hashCode()
    }

    companion object {
        private val GSON = GsonBuilder().create()
    }
}
