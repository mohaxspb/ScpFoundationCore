package ru.kuchanov.scpcore.monetization.util.playmarket

import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.ProductDataResponse
import com.amazon.device.iap.model.PurchaseResponse
import com.amazon.device.iap.model.PurchaseUpdatesResponse
import com.amazon.device.iap.model.UserDataResponse
import timber.log.Timber


class PurchaseListenerImpl : PurchasingListener {

    private var currentUserId: String? = null
    private var currentMarketplace: String? = null

    var reset = false

    override fun onProductDataResponse(productDataResponse: ProductDataResponse?) {
        Timber.d("onProductDataResponse: %s", productDataResponse)
        //todo

        when (productDataResponse?.requestStatus) {
            ProductDataResponse.RequestStatus.SUCCESSFUL -> {
                Timber.d("SUCCESSFUL")
            }
            ProductDataResponse.RequestStatus.FAILED -> {
                Timber.d("FAILED")
            }
            ProductDataResponse.RequestStatus.NOT_SUPPORTED -> {
                Timber.d("NOT_SUPPORTED")
            }
            null -> {
                Timber.d("null")
            }
        }
    }

    override fun onPurchaseResponse(purchaseResponse: PurchaseResponse?) {
        Timber.d("onPurchaseResponse: %s", purchaseResponse)
        //todo
    }

    override fun onPurchaseUpdatesResponse(purchaseUpdatesResponse: PurchaseUpdatesResponse?) {
        Timber.d("onPurchaseUpdatesResponse: %s", purchaseUpdatesResponse)
        //todo

        when (purchaseUpdatesResponse?.requestStatus) {
            PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL -> {
                Timber.d("purchaseUpdatesResponse?.requestStatus is PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL")
                for (receipt in purchaseUpdatesResponse.receipts) {
                    // Process receipts
                }
                if (purchaseUpdatesResponse.hasMore()) {
                    PurchasingService.getPurchaseUpdates(reset)
                }
            }
            PurchaseUpdatesResponse.RequestStatus.FAILED -> {
                Timber.d("purchaseUpdatesResponse?.requestStatus is PurchaseUpdatesResponse.RequestStatus.FAILED")
            }
            PurchaseUpdatesResponse.RequestStatus.NOT_SUPPORTED -> {
                Timber.d("purchaseUpdatesResponse?.requestStatus is PurchaseUpdatesResponse.RequestStatus.NOT_SUPPORTED")

            }
            null -> {
                Timber.d("purchaseUpdatesResponse?.requestStatus is NULL")
            }
        }

    }

    override fun onUserDataResponse(userDataResponse: UserDataResponse?) {
        Timber.d("onUserDataResponse: %s", userDataResponse)
        //todo

        when (userDataResponse?.requestStatus) {
            UserDataResponse.RequestStatus.SUCCESSFUL -> {
                currentUserId = userDataResponse.userData.userId
                currentMarketplace = userDataResponse.userData.marketplace
            }

            UserDataResponse.RequestStatus.FAILED -> {

            }
            UserDataResponse.RequestStatus.NOT_SUPPORTED -> {
            }
        }// Fail gracefully.
    }
}
