package ru.kuchanov.scpcore.mvp.presenter.monetization

import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard.ReadHistoryViewModel
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper
import ru.kuchanov.scpcore.mvp.base.BasePresenter
import ru.kuchanov.scpcore.mvp.contract.monetization.ReadHistoryContract
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.subscribeBy
import timber.log.Timber

class ReadHistoryPresenter(
        myPreferencesManager: MyPreferenceManager,
        dbProviderFactory: DbProviderFactory,
        apiClient: ApiClient,
        inAppHelper: InAppHelper
) : BasePresenter<ReadHistoryContract.View>(
        myPreferencesManager,
        dbProviderFactory,
        apiClient,
        inAppHelper
), ReadHistoryContract.Presenter {

    override var data = mutableListOf<MyListItem>()

    override fun loadInitialData() {
        Timber.d("loadInitialData")
        mDbProviderFactory
                .dbProvider
                .allReadHistoryTransactions
                .map { transitions -> transitions.map { ReadHistoryViewModel(it) } }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view.showProgressCenter(true) }
                .doOnEach { view.showProgressCenter(false) }
                .subscribeBy(
                        onNext = {
                            Timber.d("it size: ${it.size}")
                            view.showData(it)
                        },
                        onError = {
                            Timber.e(it, "error while get read transactions list.")
                            view.showError(it)
                        }
                )
    }

    override fun onTranactionClicked(articleUrl: String) {
        view.openArticle(articleUrl)
    }

    override fun onTranactionDeleteClicked(id: Long) {
        mDbProviderFactory
                .dbProvider
                .deleteReadHistoryTransactionById(id)
                .subscribeBy(
                        onCompleted = { Timber.d("deleted") },
                        onError = {
                            Timber.e(it, "Error while delete transaction")
                            view.showError(it)
                        }
                )
    }
}