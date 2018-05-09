package ru.kuchanov.scpcore.monetization.model

data class ScpArtAdsJson(val ads: List<ScpArtAd>) {

    data class ScpArtAd(
        val id: Int,
        val imgUrl: String
    )
}

