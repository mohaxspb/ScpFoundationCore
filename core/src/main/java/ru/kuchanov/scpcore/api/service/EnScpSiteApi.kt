package ru.kuchanov.scpcore.api.service

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import ru.kuchanov.scpcore.api.model.response.scpreaderapi.AccessTokenResponse
import rx.Single

/**
 * @see http://www.scp-wiki.net/tag-search
 */
interface EnScpSiteApi {

    /**
     * @param tags comma separated tags
     * @param sort i.e `rating desc`
     */
    @FormUrlEncoded
    @POST("tools/tagGet.php")
    fun getArticlesByTags(
            @Field("tags") tags: String,
            @Field("sort") sort: String
    ): Single<List<EnSiteTagsSearchResult>>
}

const val EN_SITE_TAG_SORT = "rating desc"