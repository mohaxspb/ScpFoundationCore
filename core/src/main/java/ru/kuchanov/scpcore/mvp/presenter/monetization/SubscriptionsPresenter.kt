package ru.kuchanov.scpcore.mvp.presenter.monetization

import android.support.v4.app.Fragment
import com.android.vending.billing.IInAppBillingService
import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.util.InAppHelper
import ru.kuchanov.scpcore.mvp.base.BasePresenter
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsContract
import timber.log.Timber

/**
 * Created by mohax on 13.01.2018.
 *
 * for ScpCore
 */
class SubscriptionsPresenter(
        myPreferencesManager: MyPreferenceManager,
        dbProviderFactory: DbProviderFactory,
        apiClient: ApiClient
) : BasePresenter<SubscriptionsContract.View>(
        myPreferencesManager,
        dbProviderFactory,
        apiClient
), SubscriptionsContract.Presenter {

    override fun onSubscriptionClick(id: String, target: Fragment, inAppBillingService: IInAppBillingService) {
        try {
            InAppHelper.startSubsBuy(target, inAppBillingService, InAppHelper.InappType.SUBS, id)
        } catch (e: Exception) {
            Timber.e(e)
            view.showError(e)
        }
    }
}