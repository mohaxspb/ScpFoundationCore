package ru.kuchanov.scpcore.mvp.contract.article;

import ru.kuchanov.scpcore.mvp.contract.DrawerMvp;

/**
 * Created by y.kuchanov on 21.12.16.
 */
public interface ArticleScreenMvp extends DrawerMvp {
    interface View extends DrawerMvp.View {
    }

    interface Presenter extends DrawerMvp.Presenter<View> {
    }
}
