package ru.dante.scpfoundation.api.service

import retrofit2.http.GET
import ru.dante.scpfoundation.api.model.response.RandomArticleResponse
import rx.Observable

/**
 * Created by kuchanov on 04/03/2018.
 */
interface ScpRuApi {

    @GET("wikidot_random_page")
    fun getRandomUrl(): Observable<RandomArticleResponse>

    companion object {
        const val API_URL = "https://scpdb.org/api/"
    }
}