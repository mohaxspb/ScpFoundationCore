package ru.kuchanov.scpcore.mvp.contract.tags;

import ru.kuchanov.scpcore.mvp.contract.DrawerMvp;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
public interface TagsScreenMvp extends DrawerMvp {

    interface View extends DrawerMvp.View {
    }

    interface Presenter extends DrawerMvp.Presenter<View> {
    }
}