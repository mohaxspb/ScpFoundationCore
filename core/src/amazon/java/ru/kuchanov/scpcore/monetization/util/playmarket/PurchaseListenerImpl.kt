package ru.kuchanov.scpcore.monetization.util.playmarket

import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.*
import com.amazon.device.iap.model.ProductType.*
import com.amazon.device.iap.model.PurchaseResponse.RequestStatus.*
import com.jakewharton.rxrelay.PublishRelay
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.InappPurchaseUtil
import timber.log.Timber
import com.amazon.device.iap.model.FulfillmentResult


class PurchaseListenerImpl(
        val subscriptionsRelay: PublishRelay<List<Subscription>>,
        val inappsRelay: PublishRelay<List<Subscription>>
) : PurchasingListener {

    private var currentUserId: String? = null
    private var currentMarketplace: String? = null

    private var reset = false

    override fun onProductDataResponse(productDataResponse: ProductDataResponse?) {
        Timber.d("onProductDataResponse: %s", productDataResponse)

        when (productDataResponse?.requestStatus) {
            ProductDataResponse.RequestStatus.SUCCESSFUL -> {
                Timber.d("onProductDataResponse SUCCESSFUL")
//                productDataResponse.productData.entries.forEach {
//                    Timber.d("${it.key}/${it.value.productType}/${it.value.title}")
//                }

                val products = productDataResponse.productData.values
                //we load different types separately
                when (products.firstOrNull()?.productType) {
                    CONSUMABLE -> {
                        val consumables = products.map {
                            Subscription(
                                    it.sku,
                                    InappPurchaseUtil.InappType.IN_APP,
                                    it.price,
//                                    it.price.toLong(), //price_amount_micros
                                    0,
                                    "N/A", //price_currency_code
                                    it.title,
                                    it.description,
                                    null,
                                    null,
                                    null,
                                    0,
                                    null,
                                    0
                            )
                        }
                        inappsRelay.call(consumables)
                    }
                    ENTITLED -> {
                        //we do not use it in this app
                    }
                    SUBSCRIPTION -> {
                        val subscriptions = products.map {
                            val re = Regex("[^\\d.,]")
                            val priceParsed = re.replace(it.price, "").replace(",", ".")
                            Timber.d("priceParsed: $priceParsed")
                            val priceAsDouble = priceParsed.toDouble()
                            val priceAsMicros = (priceAsDouble * 1000000L).toLong()
                            Subscription(
                                    it.sku,
                                    InappPurchaseUtil.InappType.SUBS,
                                    it.price,
                                    priceAsMicros,//price_amount_micros
                                    "N/A", //price_currency_code
                                    it.title,
                                    it.description,
                                    null,
                                    null,
                                    null,
                                    0,
                                    null,
                                    0
                            )
                        }
                        subscriptionsRelay.call(subscriptions)
                    }
                    null -> {
                        //do nothing...
                    }
                }

            }
            ProductDataResponse.RequestStatus.FAILED -> {
                Timber.d("onProductDataResponse FAILED")
            }
            ProductDataResponse.RequestStatus.NOT_SUPPORTED -> {
                Timber.d("onProductDataResponse NOT_SUPPORTED")
            }
            null -> {
                Timber.d("onProductDataResponse productDataResponse?.requestStatus null")
            }
        }
    }

    override fun onPurchaseResponse(purchaseResponse: PurchaseResponse?) {
        Timber.d("onPurchaseResponse: %s", purchaseResponse)
        //todo

        when (purchaseResponse?.requestStatus) {
            SUCCESSFUL -> {
                val receipt = purchaseResponse.receipt
                Timber.d("onPurchaseResponse SUCCESSFUL: ${receipt.toJSON()}")

//                iapManager.setAmazonUserId(response.getUserData().getUserId(), response.getUserData().getMarketplace());
                iapManager.handleReceipt(receipt, response.getUserData());
                iapManager.refreshOranges();
            }
            FAILED -> {
                Timber.d("onPurchaseResponse FAILED")
            }
            INVALID_SKU -> {
                Timber.d("onPurchaseResponse INVALID_SKU")
            }
            ALREADY_PURCHASED -> {
                Timber.d("onPurchaseResponse ALREADY_PURCHASED")
            }
            NOT_SUPPORTED -> {
                Timber.d("onPurchaseResponse NOT_SUPPORTED")
            }
            null -> {
                Timber.d("onPurchaseResponse purchaseResponse?.requestStatus is NULL")
            }
        }
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
                Timber.d("onUserDataResponse SUCCESSFUL")
                currentUserId = userDataResponse.userData.userId
                currentMarketplace = userDataResponse.userData.marketplace
            }

            UserDataResponse.RequestStatus.FAILED -> {
                Timber.d("onUserDataResponse FAILED")

            }
            UserDataResponse.RequestStatus.NOT_SUPPORTED -> {
                Timber.d("onUserDataResponse NOT_SUPPORTED")
            }
            null -> {
                Timber.d("onUserDataResponse userDataResponse?.requestStatus is NULL")
            }
        }
    }

    /////////////////////////sdfsdfsdfsdfsdf


    fun handleReceipt(receipt: Receipt, userData: UserData) {
        when (receipt.productType!!) {
            CONSUMABLE ->
                // try to do your application logic to fulfill the customer purchase
                handleConsumablePurchase(receipt, userData)
            ENTITLED -> {
                //noting to do in Reader app
            }
            SUBSCRIPTION -> {
                //todo
            }
        }
    }

    /**
     * This method contains the business logic to fulfill the customer's
     * purchase based on the receipt received from InAppPurchase SDK's
     * [PurchasingListener.onPurchaseResponse] or
     * [PurchasingListener.onPurchaseUpdates] method.
     *
     *
     * @param receipt
     * @param userData
     */
    fun handleConsumablePurchase(receipt: Receipt, userData: UserData) {
        try {
            if (receipt.isCanceled) {
                revokeConsumablePurchase(receipt, userData)
            } else {
                // We strongly recommend that you verify the receipt server-side
                if (!verifyReceiptFromYourService(receipt.receiptId, userData)) {
                    // if the purchase cannot be verified,
                    // show relevant error message to the customer.
                    //todo show error message
                    Timber.d("Purchase cannot be verified, please retry later.")
                    return
                }
                if (receiptAlreadyFulfilled(receipt.receiptId, userData)) {
                    // if the receipt was fulfilled before, just notify Amazon
                    // Appstore it's Fulfilled again.
                    PurchasingService.notifyFulfillment(receipt.receiptId, FulfillmentResult.FULFILLED)
                    return
                }

                grantConsumablePurchase(receipt, userData)
            }
            return
        } catch (e: Throwable) {
            mainActivity.showMessage("Purchase cannot be completed, please retry")
        }

        //
    }

    private fun verifyReceiptFromYourService(receiptId: String?, userData: UserData): Boolean {
        //todo check on server somehow
        //later, I think
        return true
    }

    /**
     * Developer should implement de-duplication logic based on the receiptId
     * received from Amazon Appstore. The receiptId is a unique identifier for
     * every purchase, but the same purchase receipt can be pushed to your app
     * multiple times in the event of connectivity issue while calling
     * notifyFulfillment. So if the given receiptId was tracked and fulfilled by
     * the app before, you should not grant the purchase content to the customer
     * again, otherwise you are giving the item for free.
     *
     *
     * @param receiptId
     * @param userData
     * @return
     */
    private fun receiptAlreadyFulfilled(receiptId: String, userData: UserData): Boolean {
        // TODO Following is a simple de-duplication logic implementation using
        // local SQLite database. We strongly recommend that you save purchase
        // information and implement the de-duplication logic on your server
        // side.

        val receiptRecord = dataSource.getPurchaseRecord(receiptId, userData.userId) ?: return false

        // Return true only if there is no local record for the receipt id/user
        // id or the receipt id is not marked as FULFILLED/UNAVAILABLE.
        return !(PurchaseStatus.FULFILLED === receiptRecord.getStatus() || PurchaseStatus.UNAVAILABLE === receiptRecord.getStatus())

    }

    private fun revokeConsumablePurchase(receipt: Receipt, userData: UserData) {
        //noting to do, I think...
        //see com.amazon.sample.iap.consumable.SampleIapManager#revokeConsumablePurchase() for details
    }
}
