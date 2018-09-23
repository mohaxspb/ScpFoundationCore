package ru.kuchanov.scpcore.api.service;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.kuchanov.scpcore.api.model.response.LeaderboardUsersUpdateDates;
import ru.kuchanov.scpcore.db.model.LeaderboardUser;
import ru.kuchanov.scpcore.db.model.gallery.GalleryImage;
import rx.Single;

/**
 * Created by mohax on 06.05.2017.
 * <p>
 * for scp_ru
 */
public interface ScpReaderServer {

    @GET("gallery/all")
    Single<List<GalleryImage>> getGallery();

    @GET("firebase/{langEnum}/users/leaderboard")
    Single<List<LeaderboardUser>> getLeaderboardUsers(
            @Path("langEnum") final String langEnum,
            @Query("offset") final int offset,
            @Query("limit") final int limit
    );

    @GET("firebase/updateDataDates")
    Single<List<LeaderboardUsersUpdateDates>> getLeaderboardUsersUpdateDates();
}