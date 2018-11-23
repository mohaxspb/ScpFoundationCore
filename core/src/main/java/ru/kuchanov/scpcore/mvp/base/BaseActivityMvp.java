package ru.kuchanov.scpcore.mvp.base;

import android.content.Intent;
import android.support.annotation.Nullable;

import com.android.vending.billing.IInAppBillingService;

import ru.kuchanov.scpcore.mvp.contract.LoginActions;

/**
 * Created by mohax on 23.03.2017.
 * <p>
 * for scp_ru
 */
public interface BaseActivityMvp {

    interface View extends BaseMvp.View, LoginActions.View, MonetizationActions {

        @Nullable
        IInAppBillingService getIInAppBillingService();
    }

    interface Presenter<V extends View> extends BaseMvp.Presenter<V>, LoginActions.Presenter {

        void onActivityStarted();

        void onActivityStopped();

        void onInviteReceived(String inviteId);

        void onInviteSent(String inviteId);

        /**
         * @return true if handled in presenter
         */
        boolean onActivityResult(int requestCode, int resultCode, Intent data);
    }
}