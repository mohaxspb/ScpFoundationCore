package ru.kuchanov.scpcore.mvp.presenter


import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper
import ru.kuchanov.scpcore.mvp.contract.MainMvp
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.subscribeBy
import timber.log.Timber

class MainPresenter(
        myPreferencesManager: MyPreferenceManager,
        dbProviderFactory: DbProviderFactory,
        apiClient: ApiClient,
        inAppHelper: InAppHelper
) : BaseDrawerPresenter<MainMvp.View>(
        myPreferencesManager,
        dbProviderFactory,
        apiClient,
        inAppHelper
), MainMvp.Presenter {
    override fun onFirstViewAttached() {
        mDbProviderFactory
                .dbProvider
                .hasReadHistoryTransactions()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = {
                            if (it) {
                                view.showReadHistoryTransactionsSnackBar()
                            }
                        },
                        onError = { Timber.e(it, "Error while call to hasReadHistoryTransactions") }
                )
    }
}