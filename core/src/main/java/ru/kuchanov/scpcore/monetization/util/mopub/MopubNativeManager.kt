package ru.kuchanov.scpcore.monetization.util.mopub

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxrelay.PublishRelay
import com.mopub.nativeads.*
import org.joda.time.Period
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import rx.Observable
import rx.Single
import rx.lang.kotlin.subscribeBy
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MopubNativeManager @Inject constructor(val context: Context) {

    private val nativeAdsWithTime = mutableMapOf<Long, NativeAd>()

    private val nativeAdsRelay = PublishRelay.create<List<NativeAd>>()

    private val moPubNativeNetworkListener = object : MoPubNative.MoPubNativeNetworkListener {
        override fun onNativeLoad(nativeAd: NativeAd) {
            Timber.d("onNativeLoad")

            nativeAd.setMoPubNativeEventListener(eventListener)

            //put into Map to handle cache (time stamp to clear after 1 hour)
            nativeAdsWithTime[System.currentTimeMillis()] = nativeAd

            nativeAdsRelay.call(nativeAdsWithTime.values.toList())

            if (nativeAdsWithTime.size < Constants.NUM_OF_NATIVE_ADS_PER_SCREEN) {
                requestNativeAd()
            }
        }

        override fun onNativeFail(errorCode: NativeErrorCode) {
            Timber.d("onNativeFailL: %s", errorCode)

            //request one more after some minutes
            Single.just(true)
                    .delay(5, TimeUnit.MINUTES)
                    .subscribeBy { requestNativeAd() }
        }
    }

    private val moPubNative = MoPubNative(context, MopubHelper.getNativeAdId(), moPubNativeNetworkListener)

    init {
        moPubNative.registerAdRenderer(moPubStaticNativeAdRenderer)
    }

    fun activate() {
        //start making requests until we get 3 ads.
        requestNativeAd()

        //also handle cache
        //each 10 minutes check if native ad is older than 1 hour and delete it and request new one
        Observable.interval(10, 10, TimeUnit.MINUTES)
                .map {
                    nativeAdsWithTime
                            .filter {
                                val hourInMillis = Period.hours(1).toStandardDuration().millis
                                System.currentTimeMillis() - it.key > hourInMillis
                            }
                }
                .subscribeBy(
                        onNext = {
                            nativeAdsWithTime.clear()
                            nativeAdsWithTime.putAll(it)
                            if (nativeAdsWithTime.size < Constants.NUM_OF_NATIVE_ADS_PER_SCREEN) {
                                requestNativeAd()
                            }
                        },
                        onError = {
                            //fixme there is error. Maybe, we need to unsubscribe while in background...
                            //test with smaller interval
                            Timber.e(it, "Unexpected error while clear native ads map")
                        }
                )
    }

    fun getNativeAds() = nativeAdsWithTime.values.toList()

    fun getNativeAdsWithUpdates() =
            nativeAdsRelay.asObservable()

    private fun requestNativeAd() {
        Timber.d("requestNativeAd")
        moPubNative.makeRequest(mRequestParameters)
    }

    fun getRenderedNativeAdsView(
            nativeAd: NativeAd,
            bannerContainer: ViewGroup,
            convertView: View?
    ): View {
        // When standalone, any range will be fine.
        val adapterHelper = AdapterHelper(context, 0, 3)
        // Retrieve the pre-built ad view that AdapterHelper prepared for us.
        return adapterHelper.getAdView(
                convertView,
                bannerContainer,
                nativeAd,
                ViewBinder.Builder(0).build()
        )
    }

    companion object {
        private val desiredAssets = EnumSet.of(
                RequestParameters.NativeAdAsset.TITLE,
                RequestParameters.NativeAdAsset.TEXT,
                RequestParameters.NativeAdAsset.CALL_TO_ACTION_TEXT,
                RequestParameters.NativeAdAsset.MAIN_IMAGE,
                RequestParameters.NativeAdAsset.ICON_IMAGE,
                RequestParameters.NativeAdAsset.STAR_RATING
        )

        private val mRequestParameters = RequestParameters.Builder()
                .desiredAssets(desiredAssets)
                .build()

        private val eventListener = object : NativeAd.MoPubNativeEventListener {
            override fun onImpression(view: View?) {
                Timber.d("NativeAd onImpression")
            }

            override fun onClick(view: View?) {
                Timber.d("NativeAd onImpression")
            }
        }

        private val viewBinder = ViewBinder.Builder(R.layout.view_mopub_native)
                .mainImageId(R.id.native_main_image)
                .iconImageId(R.id.native_icon_image)
                .titleId(R.id.native_title)
                .textId(R.id.native_text)
                .privacyInformationIconImageId(R.id.native_privacy_information_icon_image)
                .build()

        private val moPubStaticNativeAdRenderer = MoPubStaticNativeAdRenderer(viewBinder)
    }
}
