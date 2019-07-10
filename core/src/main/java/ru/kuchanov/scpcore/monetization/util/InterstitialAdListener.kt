package ru.kuchanov.scpcore.monetization.util

import ru.kuchanov.scpcore.manager.MyPreferenceManager

interface InterstitialAdListener {

    @JvmDefault
    fun onInterstitialClosed(preferences: MyPreferenceManager) {
        preferences.lastTimeAdsShows = System.currentTimeMillis()
        preferences.numOfInterstitialsShown = preferences.numOfInterstitialsShown + 1
    }
}
