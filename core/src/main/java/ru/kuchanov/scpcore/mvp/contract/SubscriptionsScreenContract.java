package ru.kuchanov.scpcore.mvp.contract;

import ru.kuchanov.scpcore.mvp.base.BaseActivityMvp;
import ru.kuchanov.scpcore.mvp.base.BaseMvp;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
public interface SubscriptionsScreenContract extends BaseActivityMvp {
    interface View extends BaseActivityMvp.View {
    }

    interface Presenter extends BaseActivityMvp.Presenter<View> {
    }
}