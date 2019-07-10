package ru.kuchanov.scpcore.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.ui.activity.BaseActivity;
import ru.kuchanov.scpcore.ui.activity.SubscriptionsActivity;
import ru.kuchanov.scpcore.util.AttributeGetter;
import timber.log.Timber;

import static ru.kuchanov.scpcore.Constants.Firebase.Analytics.EventName;
import static ru.kuchanov.scpcore.Constants.Firebase.Analytics.EventParam;
import static ru.kuchanov.scpcore.Constants.Firebase.Analytics.StartScreen;

/**
 * Created by mohax on 14.01.2017.
 * <p>
 * for scp_ru
 */
public abstract class BaseBottomSheetDialogFragment extends BottomSheetDialogFragment {

    @BindView(R2.id.root)
    protected View mRoot;

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callInjection();
    }

    protected abstract void callInjection();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    protected BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback;

    protected BottomSheetBehavior.BottomSheetCallback getBottomSheetBehaviorCallback() {
        if (mBottomSheetBehaviorCallback == null) {
            mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

                @Override
                public void onStateChanged(@NonNull final View bottomSheet, final int newState) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss();
                    }
                }

                @Override
                public void onSlide(@NonNull final View bottomSheet, final float slideOffset) {
                }
            };
        }
        return mBottomSheetBehaviorCallback;
    }

    @Override
    public void setupDialog(final Dialog dialog, final int style) {
        super.setupDialog(dialog, style);
        final View contentView = View.inflate(getContext(), getLayoutResId(), null);
        dialog.setContentView(contentView);

        unbinder = ButterKnife.bind(this, dialog);

        final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        final CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(getBottomSheetBehaviorCallback());
        }
    }

    protected abstract int getLayoutResId();

    /**
     * do not use activities method as SnackBar can't be shown over bottomsheet dialog
     */
    public void showSnackBarWithAction(final Constants.Firebase.CallToActionReason reason) {
        Timber.d("showSnackBarWithAction: %s", reason);
        final Snackbar snackbar;
        switch (reason) {
            case REMOVE_ADS:
                snackbar = Snackbar.make(mRoot, R.string.remove_ads, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.yes_bliad, v -> {
                    SubscriptionsActivity.start(getActivity());

                    final Bundle bundle = new Bundle();
                    bundle.putString(EventParam.PLACE, StartScreen.SNACK_BAR);
                    FirebaseAnalytics.getInstance(getActivity()).logEvent(EventName.SUBSCRIPTIONS_SHOWN, bundle);
                });
                break;
            case ENABLE_FONTS:
                snackbar = Snackbar.make(mRoot, R.string.only_premium, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.activate, action -> {
                    SubscriptionsActivity.start(getActivity());

                    final Bundle bundle = new Bundle();
                    bundle.putString(EventParam.PLACE, StartScreen.FONT);
                    FirebaseAnalytics.getInstance(getActivity()).logEvent(EventName.SUBSCRIPTIONS_SHOWN, bundle);
                });
                break;
            case ENABLE_AUTO_SYNC:
                snackbar = Snackbar.make(mRoot, R.string.auto_sync_disabled, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.turn_on, v -> {
                    SubscriptionsActivity.start(getActivity());

                    final Bundle bundle = new Bundle();
                    bundle.putString(EventParam.PLACE, StartScreen.AUTO_SYNC_SNACKBAR);
                    FirebaseAnalytics.getInstance(getActivity()).logEvent(EventName.SUBSCRIPTIONS_SHOWN, bundle);
                });
                break;
            case SYNC_NEED_AUTH:
                snackbar = Snackbar.make(mRoot, R.string.sync_need_auth, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.authorize, v -> {
                    snackbar.dismiss();
                    getBaseActivity().showLoginProvidersPopup();
                });
                break;
            default:
                throw new IllegalArgumentException("unexpected callToActionReason");
        }
        snackbar.setActionTextColor(AttributeGetter.getColor(getActivity(), R.attr.snackbarActionTextColor));
        snackbar.show();
    }
}