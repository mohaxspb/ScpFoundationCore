package ru.kuchanov.scpcore.api.service;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Single;

/**
 * Created by mohax on 06.05.2017.
 */
public interface VpsServer {

    @GET("MyServlet")
    Single<String> getFirebaseTokenForVkUserId(
            @Query("provider") String provider,
            @Query("id") String userId

    );
}