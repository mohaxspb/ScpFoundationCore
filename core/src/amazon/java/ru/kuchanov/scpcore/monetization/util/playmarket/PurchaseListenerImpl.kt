package ru.kuchanov.scpcore.monetization.util.playmarket

import android.content.SharedPreferences
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.*
import com.amazon.device.iap.model.ProductType.*
import com.amazon.device.iap.model.PurchaseResponse.RequestStatus.*
import com.jakewharton.rxrelay.PublishRelay
import ru.kuchanov.scpcore.monetization.model.Item
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.InappPurchaseUtil
import ru.kuchanov.scpcore.monetization.util.ItemsListWrapper
import ru.kuchanov.scpcore.monetization.util.SubscriptionWrapper
import ru.kuchanov.scpcore.monetization.util.SubscriptionsListWrapper
import timber.log.Timber


enum class AmazonPurchaseStatus {
    FULFILLED,
//    PAID,
//    UNAVAILABLE,
//    UNKNOWN
}

class PurchaseListenerImpl(
        private val subscriptionsToBuyRelay: PublishRelay<SubscriptionsListWrapper>,
        private val inappsToBuyRelay: PublishRelay<SubscriptionsListWrapper>,
        private val inappsBoughtRelay: PublishRelay<SubscriptionWrapper>,
        private val ownedSubsRelay: PublishRelay<ItemsListWrapper>,
        private val preferences: SharedPreferences
) : PurchasingListener {

    private var currentUserId: String? = null
    private var currentMarketplace: String? = null

    /**
     * Here is list of inapps and subs, available to buy
     */
    override fun onProductDataResponse(productDataResponse: ProductDataResponse?) {
        Timber.d("onProductDataResponse: %s", productDataResponse?.requestStatus?.name)

        when (productDataResponse?.requestStatus) {
            ProductDataResponse.RequestStatus.SUCCESSFUL -> {
                Timber.d("onProductDataResponse SUCCESSFUL")

                val products = productDataResponse.productData.values
                //we load different types separately
                when (products.firstOrNull()?.productType) {
                    CONSUMABLE -> {
                        val consumables = products.map {
                            Subscription(
                                    it.sku,
                                    InappPurchaseUtil.InappType.IN_APP,
                                    it.price,
                                    0, //price_amount_micros
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
                        inappsToBuyRelay.call(SubscriptionsListWrapper(subscriptions = consumables))
                    }
                    ENTITLED -> {
                        //we do not use it in this app
                    }
                    SUBSCRIPTION -> {
                        val subscriptions = products.map {
                            Timber.d("price: ${it.price}")
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
                                    null, //price_currency_code
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
                        subscriptionsToBuyRelay.call(SubscriptionsListWrapper(subscriptions = subscriptions))
                    }
                    null -> {
                        //do nothing...
                    }
                }
            }
            ProductDataResponse.RequestStatus.FAILED -> {
                Timber.d("onProductDataResponse FAILED")
                inappsToBuyRelay.call(
                        SubscriptionsListWrapper(
                                error = IllegalStateException("onProductDataResponse FAILED")
                        )
                )
                subscriptionsToBuyRelay.call(
                        SubscriptionsListWrapper(
                                error = IllegalStateException("onProductDataResponse FAILED")
                        )
                )
            }
            ProductDataResponse.RequestStatus.NOT_SUPPORTED -> {
                Timber.d("onProductDataResponse NOT_SUPPORTED")
                inappsToBuyRelay.call(
                        SubscriptionsListWrapper(
                                error = IllegalStateException("onProductDataResponse NOT_SUPPORTED")
                        )
                )
                subscriptionsToBuyRelay.call(
                        SubscriptionsListWrapper(
                                error = IllegalStateException("onProductDataResponse NOT_SUPPORTED")
                        )
                )
            }
            null -> {
                Timber.d("onProductDataResponse productDataResponse?.requestStatus null")
                inappsToBuyRelay.call(
                        SubscriptionsListWrapper(
                                error = IllegalStateException("onProductDataResponse NULL")
                        )
                )
                subscriptionsToBuyRelay.call(
                        SubscriptionsListWrapper(
                                error = IllegalStateException("onProductDataResponse NULL")
                        )
                )
            }
        }
    }

    override fun onPurchaseResponse(purchaseResponse: PurchaseResponse?) {
        Timber.d("onPurchaseResponse: %s", purchaseResponse)

        when (purchaseResponse?.requestStatus) {
            SUCCESSFUL -> {
                val receipt = purchaseResponse.receipt
                Timber.d("onPurchaseResponse SUCCESSFUL: ${receipt.toJSON()}")

//                iapManager.setAmazonUserId(response.getUserData().getUserId(), response.getUserData().getMarketplace());
                handleReceipt(receipt, purchaseResponse.userData)
            }
            FAILED -> {
                Timber.d("onPurchaseResponse FAILED")
                //nothing to do, as it called when user canceled purchase in amazon UI.
            }
            INVALID_SKU -> {
                Timber.d("onPurchaseResponse INVALID_SKU")
                inappsBoughtRelay.call(
                        SubscriptionWrapper(
                                error = IllegalStateException("onPurchaseResponse INVALID_SKU")
                        )
                )
                ownedSubsRelay.call(
                        ItemsListWrapper(
                                error = IllegalStateException("onPurchaseResponse INVALID_SKU")
                        )
                )
            }
            ALREADY_PURCHASED -> {
                Timber.d("onPurchaseResponse ALREADY_PURCHASED")
                //nothing to do
            }
            NOT_SUPPORTED -> {
                Timber.d("onPurchaseResponse NOT_SUPPORTED")
                inappsBoughtRelay.call(
                        SubscriptionWrapper(
                                error = IllegalStateException("onPurchaseResponse NOT_SUPPORTED")
                        )
                )
                ownedSubsRelay.call(
                        ItemsListWrapper(
                                error = IllegalStateException("onPurchaseResponse NOT_SUPPORTED")
                        )
                )
            }
            null -> {
                Timber.d("onPurchaseResponse purchaseResponse?.requestStatus is NULL")
                inappsBoughtRelay.call(
                        SubscriptionWrapper(
                                error = IllegalStateException("onPurchaseResponse NULL")
                        )
                )
                ownedSubsRelay.call(
                        ItemsListWrapper(
                                error = IllegalStateException("onPurchaseResponse NULL")
                        )
                )
            }
        }
    }

    override fun onPurchaseUpdatesResponse(purchaseUpdatesResponse: PurchaseUpdatesResponse?) {
        Timber.d("onPurchaseUpdatesResponse: $purchaseUpdatesResponse")

        when (purchaseUpdatesResponse?.requestStatus) {
            PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL -> {
                Timber.d("purchaseUpdatesResponse?.requestStatus is PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL")
                //todo think, if we need to handle it... We must handle subs, if we do not handle it in onPurchase response...
//                for (receipt in purchaseUpdatesResponse.receipts) {
//                    // Process receipts
//                    handleReceipt(receipt, purchaseUpdatesResponse.userData);
//                }
                val ownedNonCanceledSubs = purchaseUpdatesResponse
                        .receipts
                        .filter { it.productType == SUBSCRIPTION }
                        .filter { !it.isCanceled }
                ownedSubsRelay.call(ItemsListWrapper(items = ownedNonCanceledSubs.map { Item(sku = it.sku) }))

                //todo handle non consumed levelUp inapps

                if (purchaseUpdatesResponse.hasMore()) {
                    PurchasingService.getPurchaseUpdates(false)
                }
            }
            PurchaseUpdatesResponse.RequestStatus.FAILED -> {
                Timber.d("purchaseUpdatesResponse?.requestStatus is PurchaseUpdatesResponse.RequestStatus.FAILED")
                ownedSubsRelay.call(ItemsListWrapper(error = IllegalStateException("PurchaseUpdatesResponse.RequestStatus.FAILED")))
            }
            PurchaseUpdatesResponse.RequestStatus.NOT_SUPPORTED -> {
                Timber.d("purchaseUpdatesResponse?.requestStatus is PurchaseUpdatesResponse.RequestStatus.NOT_SUPPORTED")
                ownedSubsRelay.call(ItemsListWrapper(error = IllegalStateException("PurchaseUpdatesResponse.RequestStatus.NOT_SUPPORTED")))
            }
            null -> {
                Timber.d("purchaseUpdatesResponse?.requestStatus is NULL")
                ownedSubsRelay.call(ItemsListWrapper(error = IllegalStateException("PurchaseUpdatesResponse.RequestStatus is null")))
            }
        }
    }

    private fun handleReceipt(receipt: Receipt, userData: UserData) {
        Timber.d("handleReceipt: $receipt")
        when (receipt.productType!!) {
            CONSUMABLE ->
                // try to do your application logic to fulfill the customer purchase
                handleConsumablePurchase(receipt, userData)
            ENTITLED -> {
                //noting to do in Reader app
            }
            SUBSCRIPTION -> {
                handleSubscriptionPurchase(receipt, userData)
            }
        }
    }

    /**
     * This method contains the business logic to fulfill the customer's
     * purchase based on the receipt received from InAppPurchase SDK's
     * [PurchasingListener.onPurchaseResponse] or
     * [PurchasingListener.onPurchaseUpdatesResponse] method.
     *
     *
     * @param receipt
     * @param userData
     */
    private fun handleConsumablePurchase(receipt: Receipt, userData: UserData) {
        try {
            if (receipt.isCanceled) {
                revokeConsumablePurchase(receipt, userData)
            } else {
                if (!verifyReceiptFromYourService(receipt.receiptId, userData)) {
                    // if the purchase cannot be verified,
                    // show relevant error message to the customer.
                    Timber.e("Purchase cannot be verified, please retry later.")
                    inappsBoughtRelay.call(SubscriptionWrapper(error = IllegalStateException("Purchase cannot be verified, please retry later.")))
                } else {
                    if (receiptIsAlreadyFulfilled(receipt.receiptId, userData)) {
                        // if the receipt was fulfilled before, just notify Amazon
                        // Appstore it's Fulfilled again.
                        PurchasingService.notifyFulfillment(receipt.receiptId, FulfillmentResult.FULFILLED)
                    } else {
                        grantConsumablePurchase(receipt, userData)
                    }
                }
            }
        } catch (e: Throwable) {
            Timber.e(e, "Purchase cannot be completed, please retry")
            inappsBoughtRelay.call(SubscriptionWrapper(error = e))
        }
    }

    private fun handleSubscriptionPurchase(receipt: Receipt, userData: UserData) {
        Timber.d("handleSubscriptionPurchase: ${receipt.receiptId}")

        try {
            if (receipt.isCanceled) {
                revokeSubscription(receipt)
            } else {
                // We strongly recommend that you verify the receipt on server-side.
                if (!verifyReceiptFromYourService(receipt.receiptId, userData)) {
                    Timber.e("Purchase cannot be verified, please retry later.")
                    ownedSubsRelay.call(ItemsListWrapper(error = IllegalStateException("Purchase cannot be verified, please retry later.")))
                } else {
                    try {
                        //todo
                        // Set the purchase status to fulfilled for your application
//                        saveSubscriptionRecord(receipt, userData.userId)
                        ownedSubsRelay.call(ItemsListWrapper(items = listOf(Item(sku = receipt.sku))))
                        PurchasingService.notifyFulfillment(receipt.receiptId, FulfillmentResult.FULFILLED)
                    } catch (e: Throwable) {
                        // If for any reason the app is not able to fulfill the purchase,
                        // add your own error handling code here.
                        Timber.e(e, "Failed to grant subscription purchase")
                        ownedSubsRelay.call(ItemsListWrapper(error = e))
                    }
                }
            }
        } catch (e: Throwable) {
            Timber.e(e, "Purchase cannot be completed, please retry")
            ownedSubsRelay.call(ItemsListWrapper(error = e))
        }
    }


    private fun revokeSubscription(receipt: Receipt) {
        Timber.d("revokeSubscription: ${receipt.receiptId}")
        //todo
        //just pass subscription, as in receiver, we just.................. MAYBE, IM NOT SURE
    }

    private fun grantConsumablePurchase(receipt: Receipt, userData: UserData) {
        Timber.d("grantConsumablePurchase: ${receipt.receiptId}")

        try {
            // following sample code is a simple implementation, please
            // implement your own granting logic thread-safe, transactional and
            // robust

            // create the purchase information in your app/your server,
            // And grant the purchase to customer - give one orange to customer in this case
//            saveAmazonReceiptForUser(receipt.receiptId, userData.userId, AmazonPurchaseStatus.PAID)

            // Update purchase status in SQLite database success
            saveAmazonReceiptForUser(receipt.receiptId, userData.userId, AmazonPurchaseStatus.FULFILLED)

            //userIapData.setRemainingOranges(userIapData.getRemainingOranges() + 1)
            //some more shit, motherfucker
            //saveUserIapData()
            Timber.i("Successfully update purchase from PAID->FULFILLED for receipt id ${receipt.receiptId}")
            // update the status to Amazon Appstore. Once receive Fulfilled
            // status for the purchase, Amazon will not try to send the
            // purchase receipt to application any more
            PurchasingService.notifyFulfillment(receipt.receiptId, FulfillmentResult.FULFILLED)

            inappsBoughtRelay.call(
                    SubscriptionWrapper(
                            Subscription(
                                    receipt.sku,
                                    InappPurchaseUtil.InappType.IN_APP,
                                    "N/A", //receipt.price,
                                    -1,//price_amount_micros
                                    "N/A", //price_currency_code
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    0,
                                    null,
                                    0
                            )
                    )
            )
        } catch (e: Throwable) {
            // If for any reason the app is not able to fulfill the purchase,
            // add your own error handling code here.
            // Amazon will try to send the consumable purchase receipt again
            // next time you call PurchasingService.onPurchaseUpdatesResponse api
            Timber.e(e, "Failed to grant consumable purchase")
            inappsBoughtRelay.call(
                    SubscriptionWrapper(error = IllegalStateException("Failed to grant consumable purchase"))
            )
        }
    }

    private fun verifyReceiptFromYourService(receiptId: String?, userData: UserData): Boolean {
        Timber.d("verifyReceiptFromYourService receiptId: $receiptId, userData.userId: ${userData.userId}")
        //todo check on server somehow
        //later, I think
        return true
    }

    override fun onUserDataResponse(userDataResponse: UserDataResponse?) {
        Timber.d("onUserDataResponse: %s", userDataResponse)

        when (userDataResponse?.requestStatus) {
            UserDataResponse.RequestStatus.SUCCESSFUL -> {
                Timber.d("onUserDataResponse SUCCESSFUL")
                setUserData(userDataResponse.userData.userId, userDataResponse.userData.marketplace)
            }

            UserDataResponse.RequestStatus.FAILED -> {
                Timber.d("onUserDataResponse FAILED")
                setUserData()
            }
            UserDataResponse.RequestStatus.NOT_SUPPORTED -> {
                Timber.d("onUserDataResponse NOT_SUPPORTED")
                setUserData()
            }
            null -> {
                Timber.d("onUserDataResponse userDataResponse?.requestStatus is NULL")
                setUserData()
            }
        }
    }

    private fun setUserData(userId: String? = null, marketplace: String? = null) {
        currentUserId = userId
        currentMarketplace = marketplace
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
    private fun receiptIsAlreadyFulfilled(receiptId: String, userData: UserData): Boolean {
        // Following is a simple de-duplication logic implementation using
        // local prefs. We strongly recommend that you save purchase
        // information and implement the de-duplication logic on your server
        // side.
        val receiptRecord = getCachedAmazonInappStatusForReceiptIdAndUserId(receiptId, userData.userId)

        // Return true only if there is no local record for the receipt id/user
        // id or the receipt id is not marked as FULFILLED/UNAVAILABLE.

        Timber.d("receiptRecord: $receiptRecord")
        val receiptIsNotExists = receiptRecord == null
        val isFullfilledAlready = receiptRecord == AmazonPurchaseStatus.FULFILLED
        Timber.d("receiptIsNotExists: $receiptIsNotExists")
        Timber.d("isNotFullfilledAlready: $isFullfilledAlready")
        @Suppress("LiftReturnOrAssignment")
        if (receiptIsNotExists) {
            return false
        } else {
            return isFullfilledAlready
        }
    }

    private fun saveAmazonReceiptForUser(
            receiptId: String,
            userId: String,
            amazonPurchaseStatus: AmazonPurchaseStatus
    ) {
        preferences.edit().putString("${receiptId}_$userId", amazonPurchaseStatus.name).apply()
    }

    private fun getCachedAmazonInappStatusForReceiptIdAndUserId(
            receiptId: String,
            userId: String
    ): AmazonPurchaseStatus? {
        val statusFromPrefs = preferences.getString("${receiptId}_$userId", null)
        Timber.d("getCachedAmazonInappStatusForReceiptIdAndUserId: $statusFromPrefs")
        return statusFromPrefs?.let { AmazonPurchaseStatus.valueOf(it) }
    }

    private fun revokeConsumablePurchase(receipt: Receipt, userData: UserData) {
        //noting to do, I think...
        //see com.amazon.sample.iap.consumable.SampleIapManager#revokeConsumablePurchase() for details
        Timber.d("revokeConsumablePurchase receiptId: ${receipt.receiptId}, userData.userId: ${userData.userId}")
    }
}
