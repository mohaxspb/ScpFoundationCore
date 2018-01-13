package ru.kuchanov.scpcore.mvp.contract.materials;

import ru.kuchanov.scpcore.mvp.contract.DrawerMvp;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
public interface MaterialsScreenMvp extends DrawerMvp {

    interface View extends DrawerMvp.View {

        void onMaterialsListItemClicked(int position);
    }

    interface Presenter extends DrawerMvp.Presenter<View> {

    }
}