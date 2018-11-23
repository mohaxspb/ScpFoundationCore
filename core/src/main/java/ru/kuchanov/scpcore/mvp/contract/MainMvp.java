package ru.kuchanov.scpcore.mvp.contract;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
public interface MainMvp extends DrawerMvp {

    interface View extends DrawerMvp.View {
        void setToolbarTitleByDrawerItemId(int id);
    }

    interface Presenter extends DrawerMvp.Presenter<View> {
    }
}