package ru.kuchanov.scpcore.monetization.util.mopub

import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubView
import timber.log.Timber

class ScpMopubBannerAdListener : MoPubView.BannerAdListener {
    override fun onBannerLoaded(banner: MoPubView) {
//        Timber.d("onBannerLoaded")
    }

    override fun onBannerFailed(banner: MoPubView, errorCode: MoPubErrorCode) {
        Timber.d("onBannerFailed: %s", errorCode)
    }

    override fun onBannerClicked(banner: MoPubView) {
//        Timber.d("onBannerClicked")
    }

    override fun onBannerExpanded(banner: MoPubView) {
//        Timber.d("onBannerExpanded")
    }

    override fun onBannerCollapsed(banner: MoPubView) {
//        Timber.d("onBannerCollapsed")
    }
}
