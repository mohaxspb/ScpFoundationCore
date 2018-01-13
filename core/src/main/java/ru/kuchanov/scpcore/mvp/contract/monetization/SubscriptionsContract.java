package ru.kuchanov.scpcore.mvp.contract.monetization;

import ru.kuchanov.scpcore.mvp.base.BaseMvp;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
public interface SubscriptionsContract extends BaseMvp {
    interface View extends BaseMvp.View {
    }

    interface Presenter extends BaseMvp.Presenter<View> {
    }
}