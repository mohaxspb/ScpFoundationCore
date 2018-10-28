package ru.kuchanov.scpcore.api.service

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.api.model.response.scpreaderapi.AccessTokenResponse
import rx.Single

interface ScpReaderAuthApi {

    enum class FirebaseInstance(val lang: String) {

        EN("en"),
        RU("ru"),
        PL("pl"),
        DE("de"),
        FR("fr"),
        ES("es"),
        IT("it"),
        PT("pt"),
        CH("ch");

        companion object {

            @JvmStatic
            fun getFirebaseInstanceForLang(lang: String): FirebaseInstance? {
                for (firebaseInstance in FirebaseInstance.values()) {
                    if (firebaseInstance.lang == lang) {
                        return firebaseInstance
                    }
                }
                return null
            }
        }
    }

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

    @FormUrlEncoded
    @POST("auth/socialLogin")
    abstract fun socialLogin(
        @Field("provider") socialProvider: Constants.Firebase.SocialProvider,
        @Field("token") token: String,
        @Field("langId") langId: FirebaseInstance,
        @Field("clientId") clientId: String,
        @Field("clientSecret") clientSecret: String
    ): Single<AccessTokenResponse>
}