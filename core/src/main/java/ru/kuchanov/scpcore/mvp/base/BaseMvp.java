package ru.kuchanov.scpcore.mvp.base;

import android.support.annotation.StringRes;

import com.android.vending.billing.IInAppBillingService;
import com.hannesdorfmann.mosby.mvp.MvpPresenter;
import com.hannesdorfmann.mosby.mvp.MvpView;

import org.jetbrains.annotations.NotNull;

import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.db.model.User;
import ru.kuchanov.scpcore.mvp.contract.DataSyncActions;
import ru.kuchanov.scpcore.ui.activity.BaseActivity;

/**
 * Created by mohax on 14.01.2017.
 * <p>
 * for scp_ru
 */
public interface BaseMvp {

    interface View extends MvpView {

        void showMessage(String message);

        void showMessage(@StringRes int message);

        void showMessageLong(String message);

        void showMessageLong(@StringRes int message);

        void showError(Throwable throwable);

        void showProgressDialog(String title);

        void showProgressDialog(@StringRes int title);

        void dismissProgressDialog();

        void showSnackBarWithAction(Constants.Firebase.CallToActionReason reason);

        void showNeedLoginPopup();

        void showFreeAdsDisablePopup();

        void showOfferFreeTrialSubscriptionPopup();

        void showOfferLoginForLevelUpPopup();

        void showInAppErrorDialog(@NotNull final String errorMessage);
    }

    interface Presenter<V extends MvpView> extends MvpPresenter<V>, DataSyncActions {

        void onCreate();

        void getUserFromDb();

        User getUser();

        void onUserChanged(User user);

        void onPurchaseClick(String id, BaseActivity baseActivity, boolean ignoreUserCheck);

        void onLevelUpRetryClick(IInAppBillingService inAppBillingService);

        void updateUserScoreForScoreAction(@ScoreAction String action, @NotNull BasePresenter.AddScoreListener addScoreListener);
    }
}