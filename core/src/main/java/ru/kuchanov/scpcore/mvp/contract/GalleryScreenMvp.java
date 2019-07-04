package ru.kuchanov.scpcore.mvp.contract;

import java.util.List;

import ru.kuchanov.scpcore.db.model.gallery.GalleryImage;

/**
 * Created by y.kuchanov on 21.12.16.
 */
public interface GalleryScreenMvp extends DrawerMvp {
    interface View extends DrawerMvp.View {
        void showData(List<GalleryImage> data);
    }

    interface Presenter extends DrawerMvp.Presenter<View> {
        List<GalleryImage> getData();

        void setData(List<GalleryImage> data);
    }
}
