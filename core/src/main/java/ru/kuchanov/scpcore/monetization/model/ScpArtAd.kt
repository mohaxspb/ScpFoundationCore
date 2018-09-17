package ru.kuchanov.scpcore.monetization.model

data class ScpArtAdsJson(val ads: List<ScpArtAd>) {

    companion object {
        //todo
        const val DEFAULT_JSON = """---"""
    }

    data class ScpArtAd(
        val id: Int,
        val imgUrl: String,
        val logoUrl: String,
        val title: String,
        val subTitle: String,
        val ctaButtonText: String,
        val redirectUrl: String
    )
}

