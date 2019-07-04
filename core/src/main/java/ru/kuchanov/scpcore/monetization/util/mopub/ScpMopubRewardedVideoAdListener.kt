package ru.kuchanov.scpcore.monetization.util.mopub

import com.mopub.common.MoPubReward
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubRewardedVideoListener
import timber.log.Timber


open class ScpMopubRewardedVideoAdListener : MoPubRewardedVideoListener {

    /**
     * Called when a rewarded video is closed. At this point your application should resume.
     */
    override fun onRewardedVideoClosed(adUnitId: String) {
        Timber.d("onRewardedVideoClosed")
    }

    /**
     * Called when a rewarded video is completed and the user should be rewarded.
     * <p>
     * You can query the reward object with boolean isSuccessful(), String getLabel(), and int getAmount().
     */
    override fun onRewardedVideoCompleted(adUnitIds: MutableSet<String>, reward: MoPubReward) {
        Timber.d("onRewardedVideoCompleted: $reward")
    }

    /**
     * Called when there is an error during video playback.
     */
    override fun onRewardedVideoPlaybackError(adUnitId: String, errorCode: MoPubErrorCode) {
        Timber.d("onRewardedVideoPlaybackError: $errorCode")
    }

    /**
     * Called when a video fails to load for the given adUnitId.
     * The provided error code will provide more insight into the reason for the failure to load.
     */
    override fun onRewardedVideoLoadFailure(adUnitId: String, errorCode: MoPubErrorCode) {
        Timber.d("onRewardedVideoLoadFailure: $errorCode")
    }

    override fun onRewardedVideoClicked(adUnitId: String) {
        Timber.d("onRewardedVideoClicked")
    }

    /**
     * Called when a rewarded video starts playing.
     */
    override fun onRewardedVideoStarted(adUnitId: String) {
        Timber.d("onRewardedVideoStarted")
    }

    /**
     * Called when the video for the given adUnitId has loaded.
     * At this point you should be able to call
     * MoPubRewardedVideos.showRewardedVideo(String) to show the video.
     */
    override fun onRewardedVideoLoadSuccess(adUnitId: String) {
        Timber.d("onRewardedVideoLoadSuccess")
    }
}
