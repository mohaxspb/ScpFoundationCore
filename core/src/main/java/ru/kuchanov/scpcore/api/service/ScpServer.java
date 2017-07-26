package ru.kuchanov.scpcore.api.service;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.kuchanov.scpcore.api.model.ArticleFromSearchTagsOnSite;
import rx.Observable;

/**
 * Created by mohax on 06.05.2017.
 * <p>
 * for scp_ru
 */
public interface ScpServer {

    @GET("find")
    Observable<List<ArticleFromSearchTagsOnSite>> getArticlesByTags(@Query("wiki") String wiki, @Query("tag") List<String> tags);

    @GET("list")
    Observable<List<String>> getTagsList(@Query("wiki") String wiki);
}