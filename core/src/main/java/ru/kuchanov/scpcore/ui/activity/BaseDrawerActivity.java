package ru.kuchanov.scpcore.ui.activity;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import butterknife.BindView;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.api.model.remoteconfig.LevelsJson;
import ru.kuchanov.scpcore.db.model.User;
import ru.kuchanov.scpcore.mvp.contract.DrawerMvp;
import ru.kuchanov.scpcore.ui.holder.drawer.HeaderViewHolderLogined;
import ru.kuchanov.scpcore.ui.holder.drawer.HeaderViewHolderUnlogined;
import timber.log.Timber;

import static ru.kuchanov.scpcore.Constants.Firebase.Analytics.EventName;
import static ru.kuchanov.scpcore.Constants.Firebase.Analytics.EventParam;
import static ru.kuchanov.scpcore.Constants.Firebase.Analytics.StartScreen;

/**
 * Created by mohax on 02.01.2017.
 * <p>
 * for scp_ru
 */
public abstract class BaseDrawerActivity<V extends DrawerMvp.View, P extends DrawerMvp.Presenter<V>>
        extends BaseActivity<V, P>
        implements DrawerMvp.View {

    public static final int REQUEST_CODE_INAPP = 1421;

    private static final String STATE_CUR_DRAWER_ITEM_ID = "STATE_CUR_DRAWER_ITEM_ID";

    protected static final int SELECTED_DRAWER_ITEM_NONE = -1;

    @Inject
    Gson mGson;

    @BindView(R2.id.root)
    protected DrawerLayout mDrawerLayout;

    @BindView(R2.id.navigationView)
    protected NavigationView mNavigationView;

    protected ActionBarDrawerToggle mDrawerToggle;

    protected int mCurrentSelectedDrawerItemId;

    protected abstract int getDefaultNavItemId();

    @Override
    protected void onSaveInstanceState(@NotNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CUR_DRAWER_ITEM_ID, mCurrentSelectedDrawerItemId);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrentSelectedDrawerItemId = getDefaultNavItemId();

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);

            actionBar.setDisplayHomeAsUpEnabled(true);

            mDrawerToggle = new ActionBarDrawerToggle(
                    this,
                    mDrawerLayout,
                    R.string.app_name, R.string.app_name
            ) {
                @Override
                public void onDrawerClosed(final View view) {
                    supportInvalidateOptionsMenu();
                }

                @Override
                public void onDrawerOpened(final View drawerView) {
                }
            };
            mDrawerToggle.setDrawerIndicatorEnabled(isDrawerIndicatorEnabled());

            mDrawerLayout.addDrawerListener(mDrawerToggle);
        }

        setupNavigationView(savedInstanceState);

        updateUser(mPresenter.getUser());
    }

    private void setupNavigationView(@Nullable final Bundle savedInstanceState) {
        mNavigationView.getMenu().findItem(R.id.gallery).setVisible(mMyPreferenceManager.imagesEnabled());

        mNavigationView.setNavigationItemSelectedListener(item -> {
            mPresenter.onNavigationItemClicked(item.getItemId());
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return onNavigationItemClicked(item.getItemId());
        });

        if (savedInstanceState != null) {
            mCurrentSelectedDrawerItemId = savedInstanceState.getInt(STATE_CUR_DRAWER_ITEM_ID);
        }
        if (mCurrentSelectedDrawerItemId != SELECTED_DRAWER_ITEM_NONE) {
            mNavigationView.setCheckedItem(mCurrentSelectedDrawerItemId);
        } else {
            mNavigationView.getMenu().setGroupCheckable(0, false, true);
        }
    }

    /**
     * @return true if need to show hamburger. False if want show arrow
     */
    protected abstract boolean isDrawerIndicatorEnabled();

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Timber.d("onOptionsItemSelected with id: %s", item);
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isDrawerIndicatorEnabled()) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onReceiveRandomUrl(final String url) {
        startArticleActivity(url);
    }

    @Override
    public void updateUser(@Nullable final User user) {
//        Timber.d("updateUser: %s", user);
        if (user != null) {
            for (int i = 0; i < mNavigationView.getHeaderCount(); i++) {
                mNavigationView.removeHeaderView(mNavigationView.getHeaderView(i));
            }
            final View headerLogined = LayoutInflater.from(this).inflate(R.layout.drawer_header_logined, mNavigationView, false);
            mNavigationView.addHeaderView(headerLogined);

            final HeaderViewHolderLogined headerViewHolder = new HeaderViewHolderLogined(headerLogined);

            headerViewHolder.logout.setOnClickListener(view -> new MaterialDialog
                    .Builder(BaseDrawerActivity.this)
                    .title(R.string.warning)
                    .content(R.string.dialog_logout_content)
                    .negativeText(R.string.close)
                    .onNegative((dialog, which) -> dialog.dismiss())
                    .positiveText(R.string.logout)
                    .onPositive((dialog, which) -> {
                        dialog.dismiss();
                        //logout from google, then logout from other
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(status -> mPresenter.logoutUser());
                    })
                    .show()
            );

            headerViewHolder.levelContainer.setOnClickListener(view ->
                    mPresenter.onPurchaseClick(
                            mInAppHelper.getNewInAppsSkus().get(0),
                            false
                    )
            );

            headerViewHolder.inapp.setOnClickListener(view -> {
                SubscriptionsActivity.start(this);

                final Bundle bundle = new Bundle();
                bundle.putString(EventParam.PLACE, StartScreen.DRAWER_HEADER_LOGINED);
                FirebaseAnalytics.getInstance(BaseDrawerActivity.this).logEvent(EventName.SUBSCRIPTIONS_SHOWN, bundle);
            });

            headerViewHolder.name.setText(user.fullName);
            Glide.with(this)
                    .asBitmap()
                    .load(user.avatar)
                    .centerCrop()
                    .into(new BitmapImageViewTarget(headerViewHolder.avatar) {
                        @Override
                        protected void setResource(final Bitmap resource) {
                            final RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            headerViewHolder.avatar.setImageDrawable(circularBitmapDrawable);
                        }
                    });

            //score and level
            final LevelsJson levelsJson = LevelsJson.getLevelsJson();
//            Timber.d("levelsJson: %s", levelsJson);

            final LevelsJson.Level level = levelsJson.getLevelForScore(user.score);
//            Timber.d("level: %s", level);
            if (level == null) {
                return;
            }
            if (level.getId() == LevelsJson.MAX_LEVEL_ID) {
                headerViewHolder.circleProgress.setMaxValue(level.getScore());
                headerViewHolder.circleProgress.setValue(level.getScore());

                headerViewHolder.level.setText(level.getTitle());
                headerViewHolder.levelNum.setText(String.valueOf(level.getId()));

                headerViewHolder.avatar.setOnClickListener(view -> showLeaderboard());
            } else {
                final String levelTitle = level.getTitle();

                final LevelsJson.Level nextLevel = levelsJson.getLevels().get(level.getId() + 1);
                final int nextLevelScore = nextLevel.getScore();

                final int max = nextLevelScore - level.getScore();
                final int value = user.score - level.getScore();

                headerViewHolder.circleProgress.setMaxValue(max);
                headerViewHolder.circleProgress.setValue(value);

                headerViewHolder.level.setText(levelTitle);
                headerViewHolder.levelNum.setText(String.valueOf(level.getId()));

                headerViewHolder.avatar.setOnClickListener(view -> showLeaderboard());
            }

            //check if user score is greater than 1000 and offer him/her a free trial if there is no subscription owned
            if (!mMyPreferenceManager.isHasAnySubscription()
                    && user.score >= 1000
                    //do not show it after level up gain, where we add 10000 score
                    && mPresenter.getUser().score < 10000
                    && !mMyPreferenceManager.isFreeTrialOfferedAfterGetting1000Score()) {
                final Bundle bundle = new Bundle();
                bundle.putString(Constants.Firebase.Analytics.EventParam.PLACE,
                        Constants.Firebase.Analytics.EventValue.SCORE_1000_REACHED
                );
                FirebaseAnalytics.getInstance(this)
                        .logEvent(Constants.Firebase.Analytics.EventName.FREE_TRIAL_OFFER_SHOWN, bundle);

                mMyPreferenceManager.setFreeTrialOfferedAfterGetting1000Score();
                showOfferFreeTrialSubscriptionPopup();
            }
        } else {
            for (int i = 0; i < mNavigationView.getHeaderCount(); i++) {
                mNavigationView.removeHeaderView(mNavigationView.getHeaderView(i));
            }
            final View headerUnlogined = LayoutInflater.from(this).inflate(
                    R.layout.drawer_header_unlogined,
                    mNavigationView,
                    false
            );
            mNavigationView.addHeaderView(headerUnlogined);

            final HeaderViewHolderUnlogined headerViewHolder = new HeaderViewHolderUnlogined(headerUnlogined);

            headerViewHolder.mLogin.setOnClickListener(view -> showLoginProvidersPopup());

            headerViewHolder.mLoginInfo.setOnClickListener(view -> new MaterialDialog.Builder(this)
                    .content(R.string.login_advantages)
                    .title(R.string.login_advantages_title)
                    .positiveText(android.R.string.ok)
                    .show());
        }
    }

    @Override
    public void showLeaderboard() {
        SubscriptionsActivity.start(this, SubscriptionsActivity.TYPE_LEADERBOARD);
    }
}