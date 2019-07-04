package ru.kuchanov.scpcore.monetization.util.mopub

import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.util.InterstitialAdListener
import timber.log.Timber
import javax.inject.Inject


open class MopubInterstitialAdListener : MoPubInterstitial.InterstitialAdListener, InterstitialAdListener {

    @Inject
    lateinit var myPreferenceManager: MyPreferenceManager

    init {
        BaseApplication.getAppComponent().inject(this)
    }

    /**
     * The interstitial has been cached and is ready to be shown.
     */
    override fun onInterstitialLoaded(interstitial: MoPubInterstitial) {
        Timber.d("onInterstitialLoaded")
    }

    override fun onInterstitialFailed(interstitial: MoPubInterstitial, errorCode: MoPubErrorCode) {
        Timber.d("onInterstitialFailed: %s", errorCode)
    }

    /**
     * The interstitial has been shown. Pause / save state accordingly.
     */
    override fun onInterstitialShown(interstitial: MoPubInterstitial) {
        Timber.d("onInterstitialShown")
    }

    override fun onInterstitialClicked(interstitial: MoPubInterstitial) {
        Timber.d("onInterstitialClicked")
    }

    /**
     * The interstitial has being dismissed. Resume / load state accordingly.
     * <p>
     * Writes lastTime ads was shown to prefs. So do not forgot to call super
     * <p>
     * also increases num of shown Interstitials
     */
    override fun onInterstitialDismissed(interstitial: MoPubInterstitial) {
        Timber.d("onInterstitialDismissed")
        onInterstitialClosed(myPreferenceManager)
    }
}
