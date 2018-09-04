package ru.kuchanov.scpcore.ui.activity;

import com.google.android.gms.auth.api.Auth;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import org.jetbrains.annotations.NotNull;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import javax.inject.Inject;

import butterknife.BindView;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.api.model.remoteconfig.LevelsJson;
import ru.kuchanov.scpcore.db.model.User;
import ru.kuchanov.scpcore.monetization.model.PurchaseData;
import ru.kuchanov.scpcore.mvp.contract.DrawerMvp;
import ru.kuchanov.scpcore.ui.holder.HeaderViewHolderLogined;
import ru.kuchanov.scpcore.ui.holder.HeaderViewHolderUnlogined;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static ru.kuchanov.scpcore.Constants.Firebase.Analitics.EventName;
import static ru.kuchanov.scpcore.Constants.Firebase.Analitics.EventParam;
import static ru.kuchanov.scpcore.Constants.Firebase.Analitics.StartScreen;

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

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name) {
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

        updateUser(mPresenter.getUser());
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
        Timber.d("updateUser: %s", user);
        if (user != null) {
            for (int i = 0; i < mNavigationView.getHeaderCount(); i++) {
                mNavigationView.removeHeaderView(mNavigationView.getHeaderView(i));
            }
            final View headerLogined = LayoutInflater.from(this).inflate(R.layout.drawer_header_logined, mNavigationView, false);
            mNavigationView.addHeaderView(headerLogined);

            final HeaderViewHolderLogined headerViewHolder = new HeaderViewHolderLogined(headerLogined);

            headerViewHolder.logout.setOnClickListener(view -> new MaterialDialog.Builder(BaseDrawerActivity.this)
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

            headerViewHolder.levelContainer.setOnClickListener(mOnLevelUpClickListener);

            headerViewHolder.inapp.setOnClickListener(view -> {
                SubscriptionsActivity.start(this);

                final Bundle bundle = new Bundle();
                bundle.putString(EventParam.PLACE, StartScreen.DRAWER_HEADER_LOGINED);
                FirebaseAnalytics.getInstance(BaseDrawerActivity.this).logEvent(EventName.SUBSCRIPTIONS_SHOWN, bundle);
            });

            headerViewHolder.name.setText(user.fullName);
            Glide.with(this)
                    .load(user.avatar)
                    .asBitmap()
                    .centerCrop()
                    .into(new BitmapImageViewTarget(headerViewHolder.avatar) {
                        @Override
                        protected void setResource(final Bitmap resource) {
                            final RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            headerViewHolder.avatar.setImageDrawable(circularBitmapDrawable);
                        }
                    });

            //score and level
            final LevelsJson levelsJson = LevelsJson.Companion.getLevelsJson();
//            Timber.d("levelsJson: %s", levelsJson);

            final LevelsJson.Level level = levelsJson.getLevelForScore(user.score);
//            Timber.d("level: %s", level);
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
                bundle.putString(Constants.Firebase.Analitics.EventParam.PLACE,
                        Constants.Firebase.Analitics.EventValue.SCORE_1000_REACHED
                );
                FirebaseAnalytics.getInstance(this)
                        .logEvent(Constants.Firebase.Analitics.EventName.FREE_TRIAL_OFFER_SHOWN, bundle);

                mMyPreferenceManager.setFreeTrialOfferedAfterGetting1000Score();
                showOfferFreeTrialSubscriptionPopup();
            }
        } else {
            for (int i = 0; i < mNavigationView.getHeaderCount(); i++) {
                mNavigationView.removeHeaderView(mNavigationView.getHeaderView(i));
            }
            final View headerUnlogined = LayoutInflater.from(this).inflate(R.layout.drawer_header_unlogined, mNavigationView, false);
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

    private final View.OnClickListener mOnLevelUpClickListener = view -> mInAppHelper.getInAppsListToBuyObservable(getIInAppBillingService()).subscribe(
            items -> new MaterialDialog.Builder(view.getContext())
                    .title(R.string.dialog_level_up_title)
                    .content(R.string.dialog_level_up_content)
                    .neutralText(android.R.string.cancel)
                    .positiveText(R.string.dialog_level_up_ok_text)
                    .onPositive((dialog1, which) -> {
                        Timber.d("onPositive");
                        try {
                            Bundle buyIntentBundle = getIInAppBillingService().getBuyIntent(
                                    3,
                                    getPackageName(),
                                    items.get(0).productId,
                                    "inapp",
                                    String.valueOf(System.currentTimeMillis())
                            );
                            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                            for (String key : buyIntentBundle.keySet()) {
                                Timber.d("%s: %s", key, buyIntentBundle.get(key));
                            }
                            if (pendingIntent != null) {
                                Timber.d("startIntentSenderForResult");
                                startIntentSenderForResult(pendingIntent.getIntentSender(), REQUEST_CODE_INAPP, new Intent(), 0, 0, 0, null);
                            } else {
                                Timber.e("pendingIntent is NULL!");
                                mInAppHelper.getOwnedInAppsObservable(getIInAppBillingService())
                                        .flatMap(itemsOwned -> mInAppHelper.consumeInApp(itemsOwned.get(0).sku, itemsOwned.get(0).purchaseData.purchaseToken, getIInAppBillingService()))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                result -> Timber.d("consumed result: %s", result),
                                                Timber::e
                                        );
                            }
                        } catch (Exception e) {
                            Timber.e(e, "error ");
                            Snackbar.make(mRoot, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    })
                    .show(),
            this::showError
    );

    @Override
    public void showLeaderboard() {
        SubscriptionsActivity.start(this, SubscriptionsActivity.TYPE_LEADERBOARD);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Timber.d("onActivityResult requestCode/resultCode: %s/%s", requestCode, resultCode);
        if (requestCode == REQUEST_CODE_INAPP) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    Timber.d("error_inapp data is NULL");
                    showMessage(R.string.error_inapp);
                    return;
                }
//            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
                final String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
//            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
                Timber.d("purchaseData %s", purchaseData);
                final PurchaseData item = mGson.fromJson(purchaseData, PurchaseData.class);
                Timber.d("You have bought the %s", item.productId);

//                final Bundle bundle = new Bundle();
//                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, item.productId);
//                bundle.putFloat(FirebaseAnalytics.Param.VALUE, .5f);
//                FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, bundle);

                if (item.productId.equals(getString(R.string.inapp_skus).split(",")[0])) {
                    //levelUp 5
                    //add 10 000 score
                    mInAppHelper.consumeInApp(item.productId, item.purchaseToken, getIInAppBillingService())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    result -> {
                                        Timber.d("consume inapp successful, so update user score");
                                        mPresenter.updateUserScoreForInapp(item.productId);

                                        if (!mMyPreferenceManager.isHasAnySubscription()) {
                                            showOfferSubscriptionPopup();
                                        }
                                    },
                                    e -> {
                                        Timber.e(e, "error while consume inapp... X3 what to do)))");
                                        showError(e);
                                    }
                            );
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}