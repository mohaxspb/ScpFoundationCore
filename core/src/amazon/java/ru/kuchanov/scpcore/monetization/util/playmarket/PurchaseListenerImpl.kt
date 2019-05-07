package ru.kuchanov.scpcore.monetization.util.playmarket

import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.ProductDataResponse
import com.amazon.device.iap.model.ProductType.*
import com.amazon.device.iap.model.PurchaseResponse
import com.amazon.device.iap.model.PurchaseResponse.RequestStatus.*
import com.amazon.device.iap.model.PurchaseUpdatesResponse
import com.amazon.device.iap.model.UserDataResponse
import com.jakewharton.rxrelay.PublishRelay
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.InappPurchaseUtil
import timber.log.Timber


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
                Timber.d("onPurchaseResponse SUCCESSFUL")
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
}
