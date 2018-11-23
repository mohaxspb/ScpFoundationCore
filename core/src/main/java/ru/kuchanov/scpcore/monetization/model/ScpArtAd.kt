package ru.kuchanov.scpcore.monetization.model

data class ScpArtAdsJson(val ads: List<ScpArtAd>) {

    companion object {
        const val DEFAULT_JSON = """{   "ads": [     {       "id": 1,       "imgUrl": "https://scpfoundation.app/scp/scp-art/bookShop.jpg",       "logoUrl": "https://scpfoundation.app/scp/scp-art/scpLogoSite.png",       "title": "Книги SCP Foundation уже в продаже!",       "subTitle": "Спрашивайте в книжных магазинах своего города или закажите доставку в любой уголок страны",       "ctaButtonText": "Подробнее",       "redirectUrl": "http://artscp.com/promo?utm_source=ru.kuchanov.scpfoundation&utm_medium=referral&utm_campaign=app-ads&utm_term=1"     },     {       "id": 2,       "imgUrl": "https://scpfoundation.app/scp/scp-art/vk.jpg",       "logoUrl": "https://scpfoundation.app/scp/scp-art/scpLogoSite.png",       "title": "Сообщество SCP Foundation Вконтакте",       "subTitle": "Официальное сообщество вселенной SCP Foundation.Свежие иллюстрации и новости из мира SCP",       "ctaButtonText": "Вступить!",       "redirectUrl": "https://vk.com/artscp"     },     {       "id": 3,       "imgUrl": "https://scpfoundation.app/scp/scp-art/fb.jpg",       "logoUrl": "https://scpfoundation.app/scp/scp-art/scpLogoSite.png",       "title": "Сообщество SCP Foundation в Facebook",       "subTitle": "Официальное сообщество вселенной SCP Foundation.Свежие иллюстрации и новости из мира SCP",       "ctaButtonText": "Вступить!",       "redirectUrl": "https://www.facebook.com/artscp.project"     }   ] }"""
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

