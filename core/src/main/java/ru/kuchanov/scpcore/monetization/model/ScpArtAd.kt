package ru.kuchanov.scpcore.monetization.model

data class ScpArtAdsJson(val ads: List<ScpArtAd>) {

    companion object {
        const val DEFAULT_JSON = """
            {
              "ads": [
                {
                  "id": 1,
                  "imgUrl": "https://scpfoundation.app/scp/scp-art/bookShop.jpg",
                  "logoUrl": "https://scpfoundation.app/scp/scp-art/scpLogoSite.png",
                  "title": "Книги SCP Foundation уже в продаже!",
                  "subTitle": "Спрашивайте в книжных магазинах своего города или закажите доставку в любой уголок страны",
                  "ctaButtonText": "Подробнее",
                  "redirectUrl": "http://artscp.com/promo?utm_source=ru.kuchanov.scpfoundation&utm_medium=referral&utm_campaign=app-ads&utm_term=1"
                },
                {
                  "id": 2,
                  "imgUrl": "https://scpfoundation.app/scp/scp-art/book_v3.jpg",
                  "logoUrl": "https://scpfoundation.app/scp/scp-art/scpLogoSite.png",
                  "title": "Скоро выходит 3 том книги об SCP",
                  "subTitle": "Успей сделать предзаказ, и получить книгу первым! Предложение ограничено",
                  "ctaButtonText": "Подробнее",
                  "redirectUrl": "https://artscp.com/artbook/7/"
                },
                {
                  "id": 3,
                  "imgUrl": "https://scpfoundation.app/scp/scp-art/telegram.jpg",
                  "logoUrl": "https://scpfoundation.app/scp/scp-art/scpLogoSite.png",
                  "title": "Теперь у нас есть Телеграм-канал!",
                  "subTitle": "И мы всегда будем с тобой на связи, агент",
                  "ctaButtonText": "Вступить!",
                  "redirectUrl": "http://t.me/artscp"
                }
              ]
            }
        """
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
