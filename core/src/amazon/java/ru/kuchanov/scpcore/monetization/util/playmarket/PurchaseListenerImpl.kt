package ru.kuchanov.scpcore.monetization.util.playmarket

import android.content.SharedPreferences
import android.util.Log
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.*
import com.amazon.device.iap.model.ProductType.*
import com.amazon.device.iap.model.PurchaseResponse.RequestStatus.*
import com.jakewharton.rxrelay.PublishRelay
import ru.kuchanov.scpcore.monetization.model.Item
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.InappPurchaseUtil
import rx.Observable
import rx.Single
import timber.log.Timber


enum class AmazonPurchaseStatus {
    PAID, FULFILLED, UNAVAILABLE, UNKNOWN
}

class PurchaseListenerImpl(
        private val subscriptionsToBuyRelay: PublishRelay<List<Subscription>>,
        private val inappsToBuyRelay: PublishRelay<List<Subscription>>,
        private val inappsBoughtRelay: PublishRelay<Subscription>,
        private val ownedSubsRelay: PublishRelay<List<Subscription>>,
        private val preferences: SharedPreferences
) : PurchasingListener {

    private var currentUserId: String? = null
    private var currentMarketplace: String? = null

    /**
     * Here is list of inapps and subs, available to buy
     */
    override fun onProductDataResponse(productDataResponse: ProductDataResponse?) {
        Timber.d("onProductDataResponse: %s", productDataResponse)

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
                        inappsToBuyRelay.call(consumables)
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
                        subscriptionsToBuyRelay.call(subscriptions)
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

        when (purchaseResponse?.requestStatus) {
            SUCCESSFUL -> {
                val receipt = purchaseResponse.receipt
                Timber.d("onPurchaseResponse SUCCESSFUL: ${receipt.toJSON()}")

//                iapManager.setAmazonUserId(response.getUserData().getUserId(), response.getUserData().getMarketplace());
                handleReceipt(receipt, purchaseResponse.userData)
            }
            FAILED -> {
                Timber.d("onPurchaseResponse FAILED")
                //todo
            }
            INVALID_SKU -> {
                Timber.d("onPurchaseResponse INVALID_SKU")
                //todo
            }
            ALREADY_PURCHASED -> {
                Timber.d("onPurchaseResponse ALREADY_PURCHASED")
                //todo
            }
            NOT_SUPPORTED -> {
                Timber.d("onPurchaseResponse NOT_SUPPORTED")
                //todo
            }
            null -> {
                Timber.d("onPurchaseResponse purchaseResponse?.requestStatus is NULL")
                //todo
            }
        }
    }

    override fun onPurchaseUpdatesResponse(purchaseUpdatesResponse: PurchaseUpdatesResponse?) {
        Timber.d("onPurchaseUpdatesResponse: $purchaseUpdatesResponse")
        when (purchaseUpdatesResponse?.requestStatus) {
            PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL -> {
                Timber.d("purchaseUpdatesResponse?.requestStatus is PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL")
//                for (receipt in purchaseUpdatesResponse.receipts) {
//                    // Process receipts
//                    handleReceipt(receipt, purchaseUpdatesResponse.userData);
//                }
                val ownedNonCanceledSubs = purchaseUpdatesResponse
                        .receipts
                        .filter { it.productType == SUBSCRIPTION }
                        .filter { !it.isCanceled }
                ownedSubsRelay.call(ownedNonCanceledSubs.map {
                    Item(

                    )
                })
                if (purchaseUpdatesResponse.hasMore()) {
                    PurchasingService.getPurchaseUpdates(false)
                }
            }
            PurchaseUpdatesResponse.RequestStatus.FAILED -> {
                Timber.d("purchaseUpdatesResponse?.requestStatus is PurchaseUpdatesResponse.RequestStatus.FAILED")
                //todo
            }
            PurchaseUpdatesResponse.RequestStatus.NOT_SUPPORTED -> {
                Timber.d("purchaseUpdatesResponse?.requestStatus is PurchaseUpdatesResponse.RequestStatus.NOT_SUPPORTED")
                //todo
            }
            null -> {
                Timber.d("purchaseUpdatesResponse?.requestStatus is NULL")
                //todo
            }
        }
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
     * [PurchasingListener.onPurchaseUpdates] method.
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
                    //todo show error message
                    Timber.e("Purchase cannot be verified, please retry later.")
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
            //todo show error message
            Timber.e(e, "Purchase cannot be completed, please retry")
        }
    }

    private fun handleSubscriptionPurchase(receipt: Receipt, userData: UserData) {
        Timber.d("handleSubscriptionPurchase: $receipt")

        try {
            if (receipt.isCanceled) {
                // Check whether this receipt is for an expired or canceled subscription
                revokeSubscription(receipt, userData)
            } else {
                // We strongly recommend that you verify the receipt on server-side.
                if (!verifyReceiptFromYourService(receipt.receiptId, userData)) {
                    // if the purchase cannot be verified,
                    //todo show relevant error message to the customer.
                    Timber.e("Purchase cannot be verified, please retry later.")
                } else {
                    grantSubscriptionPurchase(receipt, userData)
                }
            }
        } catch (e: Throwable) {
            //todo handle error
//            mainActivity.showMessage("Purchase cannot be completed, please retry")
            Timber.e(e, "Purchase cannot be completed, please retry")
        }
    }

    private fun grantSubscriptionPurchase(receipt: Receipt, userData: UserData) {
        Timber.d("grantSubscriptionPurchase: $receipt")
        //todo

        try {
            //todo
            // Set the purchase status to fulfilled for your application
//            saveSubscriptionRecord(receipt, userData.userId)
            PurchasingService.notifyFulfillment(receipt.receiptId, FulfillmentResult.FULFILLED)
        } catch (e: Throwable) {
            // If for any reason the app is not able to fulfill the purchase,
            // add your own error handling code here.
            Timber.e(e, "Failed to grant subscription purchase")
        }

    }

    private fun revokeSubscription(receipt: Receipt, userData: UserData) {
        Timber.d("revokeSubscription: $receipt")
        //todo
    }

    private fun grantConsumablePurchase(receipt: Receipt, userData: UserData) {
        Timber.d("grantConsumablePurchase: $receipt/ $userData")

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
        } catch (e: Throwable) {
            // If for any reason the app is not able to fulfill the purchase,
            // add your own error handling code here.
            // Amazon will try to send the consumable purchase receipt again
            // next time you call PurchasingService.getPurchaseUpdates api
            //todo handle error
            Timber.e(e, "Failed to grant consumable purchase")
        }
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
    }
}