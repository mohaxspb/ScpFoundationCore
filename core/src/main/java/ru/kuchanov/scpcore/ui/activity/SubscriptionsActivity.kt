package ru.kuchanov.scpcore.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_subscribtions.*
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.mvp.contract.FragmentToolbarStateSetter
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsScreenContract
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsScreenContract.Screen.*
import ru.kuchanov.scpcore.ui.fragment.monetization.FreeAdsDisableActionsFragment
import ru.kuchanov.scpcore.ui.fragment.monetization.LeaderboardFragment
import ru.kuchanov.scpcore.ui.fragment.monetization.SubscriptionsFragment
import timber.log.Timber


/**
 * Created by mohax on 17.09.2017.
 *
 * for ScpCore
 */
class SubscriptionsActivity :
        BaseActivity<SubscriptionsScreenContract.View, SubscriptionsScreenContract.Presenter>(),
        SubscriptionsScreenContract.View {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val screenToShowType = intent.getIntExtra(EXTRA_TYPE, TYPE_SUBS)

        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            title = getString(
                when (screenToShowType) {
                    TYPE_SUBS -> R.string.subs_activity_title
                    TYPE_DISABLE_ADS_FOR_FREE -> R.string.free_ads_activity_title
                    TYPE_LEADERBOARD -> R.string.subs_leaderboard_activity_title
                    else -> throw IllegalArgumentException("unexpected type: $screenToShowType")
                })
        }
        toolbar.setTitleTextColor(
            ContextCompat.getColor(
                this@SubscriptionsActivity, when (screenToShowType) {
                    TYPE_SUBS, TYPE_LEADERBOARD -> android.R.color.white
                    TYPE_DISABLE_ADS_FOR_FREE -> R.color.freeAdsTextColor
                    else -> throw IllegalArgumentException("unexpected type: $screenToShowType")
                }))

        if (savedInstanceState == null) {
            when (screenToShowType) {
                TYPE_SUBS -> presenter.showSubscriptionsScreen()
                TYPE_DISABLE_ADS_FOR_FREE -> presenter.showDisableAdsForFreeScreen()
                TYPE_LEADERBOARD -> presenter.showLeaderboardScreen()
                else -> throw IllegalArgumentException("unexpected type: $screenToShowType")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showScreen(screen: SubscriptionsScreenContract.Screen) {
        val fragment: Fragment = when (screen) {
            SUBS -> SubscriptionsFragment.newInstance()
            FREE_ACTIONS -> FreeAdsDisableActionsFragment.newInstance()
            LEADERBOARD -> LeaderboardFragment.newInstance()
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.content, fragment, screen.name)
                .addToBackStack(screen.name)
                .commitAllowingStateLoss()

        if (fragment is FragmentToolbarStateSetter) {
            setToolbarTextColor(fragment.getToolbarTextColor())
            setToolbarTitle(getString(fragment.getToolbarTitle()))
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount <= 1) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun getLayoutResId() = R.layout.activity_subscribtions

    override fun callInjections() = BaseApplication.getAppComponent().inject(this)

    override fun getMenuResId() = 0

    override fun isBannerEnabled() = false

    override fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
        supportActionBar?.subtitle = ""
    }

    override fun setToolbarTitle(@StringRes title: Int) = setToolbarTitle(getString(title))

    override fun setToolbarTextColor(toolbarTextColor: Int) {
        val color = ContextCompat.getColor(this, toolbarTextColor)
        toolbar.setTitleTextColor(color)
        toolbar.setSubtitleTextColor(color)
        toolbar.navigationIcon?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult called in SubscriptionsActivity")
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {

        @JvmOverloads
        @JvmStatic
        fun start(context: Context, type: Int = TYPE_SUBS) {
            val intent = Intent(context, SubscriptionsActivity::class.java)
            intent.putExtra(EXTRA_TYPE, type)
            context.startActivity(intent)
        }

        @JvmStatic
        val EXTRA_TYPE = "EXTRA_TYPE"

        @JvmField
        val TYPE_SUBS = 0
        @JvmField
        val TYPE_DISABLE_ADS_FOR_FREE = 1
        @JvmField
        val TYPE_LEADERBOARD = 2
    }
}

