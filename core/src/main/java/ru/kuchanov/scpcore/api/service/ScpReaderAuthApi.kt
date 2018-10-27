package ru.kuchanov.scpcore.api.service

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import ru.kuchanov.scpcore.api.model.response.scpreaderapi.AccessTokenResponse
import rx.Single

interface ScpReaderAuthApi {

    @FormUrlEncoded
    @POST("oauth/token")
    fun getAccessToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String,
        @Field("username") user: String,
        @Field("password") password: String
    ): Single<AccessTokenResponse>

    @FormUrlEncoded
    @POST("oauth/token")
    fun getAccessTokenByRefreshToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String
    ): Single<AccessTokenResponse>
}