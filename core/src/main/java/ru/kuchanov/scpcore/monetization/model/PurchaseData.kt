package ru.kuchanov.scpcore.monetization.model

/**
 * Created by mohax on 03.06.2017.
 *
 */
data class PurchaseData(
        val autoRenewing: String? = null,
        val orderId: String? = null,
        val packageName: String? = null,
        val productId: String? = null,
        val purchaseTime: String? = null,
        val developerPayload: String? = null,
        val purchaseToken: String = ""
)
