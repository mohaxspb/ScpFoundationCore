package ru.kuchanov.scpcore.api.service;

import java.util.List;

import retrofit2.http.GET;
import ru.kuchanov.scpcore.db.model.gallery.GalleryImage;
import rx.Observable;

/**
 * Created by mohax on 06.05.2017.
 * <p>
 * for scp_ru
 */
public interface ScpReaderServer {

    @GET("gallery/all")
    Observable<List<GalleryImage>> getGallery();
}