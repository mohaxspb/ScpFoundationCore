package ru.kuchanov.scpcore.monetization.util.mopub

import android.content.Context
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.BuildConfig
import ru.kuchanov.scpcore.R

class MopubHelper {

    object Constants {
        const val BANNER_TEST_ID = "b195f8dd8ded45fe847ad89ed1d016da"
        const val INTERSTITIAL_TEST_ID = "24534e1901884e398f1253216226017e"
        const val REWARDED_VIDEO_TEST_ID = "920b6145fb1546cf8b5cf2ac34638bb7"
        const val NATIVE_TEST_ID = "11a17b188668469fb0412708c3d16813"
    }

    companion object {
        private val context: Context by lazy {
            BaseApplication.getAppInstance()
        }

        @JvmStatic
        fun getBannerAdId() =
                if (BuildConfig.DEBUG || BuildConfig.FLAVOR_mode == "dev") {
                    Constants.BANNER_TEST_ID
                } else {
                    context.getString(R.string.mopub_ad_unit_id_banner)
                }

        @JvmStatic
        fun getInterstitialAdId() =
                if (BuildConfig.DEBUG || BuildConfig.FLAVOR_mode == "dev") {
                    Constants.INTERSTITIAL_TEST_ID
                } else {
                    context.getString(R.string.mopub_ad_unit_id_interstitial)
                }

        @JvmStatic
        fun getRewardedVideoAdId() =
                if (BuildConfig.DEBUG || BuildConfig.FLAVOR_mode == "dev") {
                    Constants.REWARDED_VIDEO_TEST_ID
                } else {
                    context.getString(R.string.mopub_ad_unit_id_rewarded_video)
                }

        @JvmStatic
        fun getNativeAdId() =
                if (BuildConfig.DEBUG || BuildConfig.FLAVOR_mode == "dev") {
                    Constants.NATIVE_TEST_ID
                } else {
                    context.getString(R.string.mopub_ad_unit_id_native)
                }
    }
}