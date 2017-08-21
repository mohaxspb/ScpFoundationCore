package ru.kuchanov.scpcore.ui.base;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hannesdorfmann.mosby3.mvp.MvpFragment;
import com.squareup.leakcanary.RefWatcher;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.kuchanov.scp.downloads.ScpParseException;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.mvp.base.BaseMvp;
import timber.log.Timber;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public abstract class BaseFragment<V extends BaseMvp.View, P extends BaseMvp.Presenter<V>>
        extends MvpFragment<V, P>
        implements BaseMvp.View {

    protected Unbinder mUnbinder;

    @Inject
    protected P mPresenter;
    private MaterialDialog mProgressDialog;

    @NonNull
    @Override
    public P createPresenter() {
        return mPresenter;
    }

    @BindView(R2.id.root)
    protected View mRoot;

    protected abstract int getLayoutResId();

    protected abstract void callInjections();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        callInjections();
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(isHasOptionsMenu());
    }

    /**
     * override it to enable menu for fragemnt
     *
     * @return if fragemnt has options menu
     */
    protected boolean isHasOptionsMenu() {
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(getMenuResId(), menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * override it to add menu when add fragment
     *
     * @return menu res id to add to activities menu
     */
    protected int getMenuResId() {
        return 0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Timber.d("onCreateView");
        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Timber.d("onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
        mPresenter.onCreate();
        initViews();
    }

    /**
     * called
     */
    protected abstract void initViews();

    @Override
    public void onDestroyView() {
        Timber.d("onDestroyView");
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this instanceof SharedPreferences.OnSharedPreferenceChangeListener) {
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .registerOnSharedPreferenceChangeListener(
                            (SharedPreferences.OnSharedPreferenceChangeListener) this
                    );
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this instanceof SharedPreferences.OnSharedPreferenceChangeListener) {
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .unregisterOnSharedPreferenceChangeListener(
                            (SharedPreferences.OnSharedPreferenceChangeListener) this
                    );
        }
    }

    @Override
    public void showError(Throwable throwable) {
        if (!isAdded()) {
            return;
        }
        String message = throwable.getMessage();
        if (throwable instanceof IOException) {
            message = getString(R.string.error_connection);
        } else if (throwable instanceof ScpParseException) {
            message = getString(R.string.error_parse);
        }
        Snackbar.make(mRoot, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showMessage(String message) {
        if (!isAdded()) {
            return;
        }
        Snackbar.make(mRoot, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showMessage(@StringRes int message) {
        if (!isAdded()) {
            return;
        }
        showMessage(getString(message));
    }

    @Override
    public void showMessageLong(String message) {
        if (!isAdded()) {
            return;
        }
        Snackbar.make(mRoot, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showMessageLong(@StringRes int message) {
        if (!isAdded()) {
            return;
        }
        showMessageLong(getString(message));
    }

    @Override
    public void showProgressDialog(String title) {
        if (!isAdded()) {
            return;
        }
        mProgressDialog = new MaterialDialog.Builder(getActivity())
                .progress(true, 0)
                .title(title)
                .cancelable(false)
                .show();
    }

    @Override
    public void showProgressDialog(@StringRes int title) {
        if (!isAdded()) {
            return;
        }
        showProgressDialog(getString(title));
    }

    @Override
    public void dismissProgressDialog() {
        if (!isAdded()) {
            return;
        }
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            return;
        }
        mProgressDialog.dismiss();
    }

    @Override
    public void showNeedLoginPopup() {
        if (!isAdded()) {
            return;
        }
        getBaseActivity().showNeedLoginPopup();
    }

    @Override
    public void showFreeAdsDisablePopup() {
        if (!isAdded()) {
            return;
        }
        getBaseActivity().showFreeAdsDisablePopup();
    }

    protected BaseActivity getBaseActivity() {
        if (!(getActivity() instanceof BaseActivity)) {
            throw new RuntimeException("Activity must extend BaseActivity");
        }
        return (BaseActivity) getActivity();
    }

    @Override
    public void showSnackBarWithAction(Constants.Firebase.CallToActionReason reason) {
        getBaseActivity().showSnackBarWithAction(reason);
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy");
        super.onDestroy();
        RefWatcher refWatcher = BaseApplication.getAppInstance().getRefWatcher();
        refWatcher.watch(this);
    }
}