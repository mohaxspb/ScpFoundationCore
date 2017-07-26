package ru.kuchanov.scpcore.api.service;

import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.kuchanov.scpcore.api.model.response.LeaderBoardResponse;
import rx.Observable;

/**
 * Created by mohax on 06.05.2017.
 * <p>
 * for scp_ru
 */
public interface VpsServer {

    @GET("LeaderBoard")
    Observable<LeaderBoardResponse> getLeaderboard(@Query("lang") String lang);
}