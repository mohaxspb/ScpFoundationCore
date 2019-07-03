package ru.kuchanov.scpcore.monetization.util.mopub

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxrelay.PublishRelay
import com.mopub.nativeads.*
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class MopubNativeManager @Inject constructor(val context: Context) {

    val nativeAds = mutableListOf<NativeAd>()

    private val nativeAdsRelay = PublishRelay.create<List<NativeAd>>()

    private val moPubNativeNetworkListener = object : MoPubNative.MoPubNativeNetworkListener {
        override fun onNativeLoad(nativeAd: NativeAd) {
            Timber.d("onNativeLoad")

            nativeAd.setMoPubNativeEventListener(eventListener)

            //todo put into Map to handle cache (time stamp to clear after 1 hour)
            nativeAds += nativeAd
            nativeAdsRelay.call(nativeAds)

            if (nativeAds.size < Constants.NUM_OF_NATIVE_ADS_PER_SCREEN) {
                requestNativeAd();
            }
        }

        override fun onNativeFail(errorCode: NativeErrorCode) {
            Timber.d("onNativeFailL: %s", errorCode)
        }
    }

    private val moPubNative = MoPubNative(context, MopubHelper.getNativeAdId(), moPubNativeNetworkListener)

    init {
        moPubNative.registerAdRenderer(moPubStaticNativeAdRenderer)
    }

    fun activate() {
        //todo start making requests until we get 3 ads.
        //also handle cache
        //todo so it should be main method to cal
    }

    fun getNativeAdsWithUpdates() =
            nativeAdsRelay.asObservable()

    fun requestNativeAd() {
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
