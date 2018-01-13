package ru.kuchanov.scpcore.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsScreenContract
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsScreenContract.Screen.*
import ru.kuchanov.scpcore.ui.base.BaseActivity
import ru.kuchanov.scpcore.ui.fragment.monetization.SubscriptionsFragment

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

        //todo
        if (savedInstanceState == null) {
            presenter.showSubscriptionsScreen()
        }
    }

    override fun showScreen(screen: SubscriptionsScreenContract.Screen) {
        val fragment: Fragment = when (screen) {
            SUBS -> SubscriptionsFragment.newInstance()
            FREE_ACTIONS -> TODO()
            LEADERBOARD -> TODO()
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.content, fragment, screen.name)
                .addToBackStack(screen.name)
                .commitAllowingStateLoss()
    }

    override fun getLayoutResId() = R.layout.activity_subscribtions

    override fun callInjections() = BaseApplication.getAppComponent().inject(this)

    override fun getMenuResId() = 0

    override fun isBannerEnabled() = false

    companion object {

        fun start(context: Context) {
            context.startActivity(Intent(context, SubscriptionsActivity::class.java))
        }
    }
}
