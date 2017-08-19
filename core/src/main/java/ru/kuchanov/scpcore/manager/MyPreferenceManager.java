package ru.kuchanov.scpcore.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import org.joda.time.Period;

import java.util.ArrayList;

import ru.kuchanov.scp.downloads.MyPreferenceManagerModel;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.monetization.model.ApplicationsResponse;
import ru.kuchanov.scpcore.monetization.model.PlayMarketApplication;
import ru.kuchanov.scpcore.monetization.model.VkGroupToJoin;
import ru.kuchanov.scpcore.monetization.model.VkGroupsToJoinResponse;
import ru.kuchanov.scpcore.ui.dialog.SetttingsBottomSheetDialogFragment;
import timber.log.Timber;

import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.APP_INSTALL_REWARD_IN_MILLIS;
import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.AUTH_COOLDOWN_IN_MILLIS;
import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.FREE_VK_GROUPS_JOIN_REWARD;
import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.PERIOD_BETWEEN_INTERSTITIAL_IN_MILLIS;
import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.REWARDED_VIDEO_COOLDOWN_IN_MILLIS;

/**
 * Created by y.kuchanov on 22.12.16.
 * <p>
 * for scp_ru
 */
public class MyPreferenceManager implements MyPreferenceManagerModel {

    /**
     * check if user joined app vk group each 1 day
     */
    private static final long PERIOD_BETWEEN_APP_VK_GROUP_JOINED_CHECK_IN_MILLIS = Period.days(1).toStandardDuration().getMillis(); //Period.days(1).getDays()
    /**
     * update user subs every 6 hours
     */
    private static final long PERIOD_BETWEEN_SUBSCRIPTIONS_INVALIDATION_IN_MILLIS = Period.hours(6).toStandardDuration().getMillis();
    /**
     * used to calculate is it time to request new Interstitial ads (5 min)
     */
    private static final long PERIOD_BEFORE_INTERSTITIAL_MUST_BE_SHOWN_IN_MILLIS = Period.minutes(5).toStandardDuration().getMillis();
    /**
     * offer free trial every 7 days
     */
    private static final long FREE_TRIAL_OFFERED_PERIOD = Period.days(7).toStandardDuration().getMillis();

    private static final int NUM_OF_DISABLE_ADS_REWARDS_COUNT_BEFORE_OFFER_SHOWING = 3;

    public interface Keys {
        String NIGHT_MODE = "NIGHT_MODE";
        String TEXT_SCALE_UI = "TEXT_SCALE_UI";
        String TEXT_SCALE_ARTICLE = "TEXT_SCALE_ARTICLE";
        String DESIGN_LIST_TYPE = "DESIGN_LIST_TYPE";

        String NOTIFICATION_IS_ON = "NOTIFICATION_IS_ON";
        String NOTIFICATION_PERIOD = "NOTIFICATION_PERIOD";
        String NOTIFICATION_VIBRATION_IS_ON = "NOTIFICATION_VIBRATION_IS_ON";
        String NOTIFICATION_LED_IS_ON = "NOTIFICATION_LED_IS_ON";
        String NOTIFICATION_SOUND_IS_ON = "NOTIFICATION_SOUND_IS_ON";

        String ADS_LAST_TIME_SHOWS = "ADS_LAST_TIME_SHOWS";
        String ADS_REWARDED_DESCRIPTION_IS_SHOWN = "ADS_REWARDED_DESCRIPTION_IS_SHOWN";
        String ADS_NUM_OF_INTERSTITIALS_SHOWN = "ADS_NUM_OF_INTERSTITIALS_SHOWN";

        String LICENCE_ACCEPTED = "LICENCE_ACCEPTED";
        String CUR_APP_VERSION = "CUR_APP_VERSION";
        String DESIGN_FONT_PATH = "DESIGN_FONT_PATH";
        String PACKAGE_INSTALLED = "PACKAGE_INSTALLED";
        String VK_GROUP_JOINED = "VK_GROUP_JOINED";
        String HAS_SUBSCRIPTION = "HAS_SUBSCRIPTION";
        String HAS_NO_ADS_SUBSCRIPTION = "HAS_NO_ADS_SUBSCRIPTION";
        String AUTO_SYNC_ATTEMPTS = "AUTO_SYNC_ATTEMPTS";
        String UNSYNCED_SCORE = "UNSYNCED_SCORE";
        String UNSYNCED_VK_GROUPS = "UNSYNCED_VK_GROUPS";
        String UNSYNCED_APPS = "UNSYNCED_APPS";
        String APP_VK_GROUP_JOINED_LAST_TIME_CHECKED = "APP_VK_GROUP_JOINED_LAST_TIME_CHECKED";
        String APP_VK_GROUP_JOINED = "APP_VK_GROUP_JOINED";
        String DATA_RESTORED = "DATA_RESTORED";
        String TIME_FOR_WHICH_BANNERS_DISABLED = "TIME_FOR_WHICH_BANNERS_DISABLED";
        String LAST_TIME_SUBSCRIPTIONS_INVALIDATED = "LAST_TIME_SUBSCRIPTIONS_INVALIDATED";
        String PERSONAL_DATA_ACCEPTED = "PERSONAL_DATA_ACCEPTED";
        String AWARD_FROM_AUTH_GAINED = "AWARD_FROM_AUTH_GAINED";
        String FREE_ADS_DISABLE_REWARD_GAINED_COUNT = "FREE_ADS_DISABLE_REWARD_GAINED_COUNT";
        String FREE_TRIAL_OFFERED_PERIODICAL = "FREE_TRIAL_OFFERED_PERIODICAL";
    }

    private Gson mGson;

    private SharedPreferences mPreferences;

    public MyPreferenceManager(Context context, Gson gson) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mGson = gson;
    }

    public void setIsNightMode(boolean isInNightMode) {
        mPreferences.edit().putBoolean(Keys.NIGHT_MODE, isInNightMode).apply();
    }

    public boolean isNightMode() {
        return mPreferences.getBoolean(Keys.NIGHT_MODE, false);
    }

    public void setUiTextScale(float uiTextScale) {
        mPreferences.edit().putFloat(Keys.TEXT_SCALE_UI, uiTextScale).apply();
    }

    public float getUiTextScale() {
        return mPreferences.getFloat(Keys.TEXT_SCALE_UI, .75f);
    }

    public float getArticleTextScale() {
        return mPreferences.getFloat(Keys.TEXT_SCALE_ARTICLE, .75f);
    }

    public void setArticleTextScale(float textScale) {
        mPreferences.edit().putFloat(Keys.TEXT_SCALE_ARTICLE, textScale).apply();
    }

    //design settings
    public boolean isDesignListNewEnabled() {
        return !mPreferences.getString(Keys.DESIGN_LIST_TYPE, SetttingsBottomSheetDialogFragment.ListItemType.MIDDLE).equals(SetttingsBottomSheetDialogFragment.ListItemType.MIN);
    }

    public void setListDesignType(@SetttingsBottomSheetDialogFragment.ListItemType String type) {
        mPreferences.edit().putString(Keys.DESIGN_LIST_TYPE, type).apply();
    }

    @SetttingsBottomSheetDialogFragment.ListItemType
    public String getListDesignType() {
        @SetttingsBottomSheetDialogFragment.ListItemType
        String type = mPreferences.getString(Keys.DESIGN_LIST_TYPE, SetttingsBottomSheetDialogFragment.ListItemType.MIDDLE);
        return type;
    }

    public void setFontPath(String type) {
        mPreferences.edit().putString(Keys.DESIGN_FONT_PATH, type).apply();
    }

    public String getFontPath() {
        return mPreferences.getString(Keys.DESIGN_FONT_PATH, "fonts/Roboto-Regular.ttf");
    }

    //new arts notifications
    int getNotificationPeriodInMinutes() {
        return mPreferences.getInt(Keys.NOTIFICATION_PERIOD, 60);
    }

//    public void setNotificationPeriodInMinutes(int minutes) {
//        mPreferences.edit().putInt(Keys.NOTIFICATION_PERIOD, minutes).apply();
//    }

    public boolean isNotificationEnabled() {
        return mPreferences.getBoolean(Keys.NOTIFICATION_IS_ON, true);
    }

    public void setNotificationEnabled(boolean enabled) {
        mPreferences.edit().putBoolean(Keys.NOTIFICATION_IS_ON, enabled).apply();
    }

    public boolean isNotificationVibrationEnabled() {
        return mPreferences.getBoolean(Keys.NOTIFICATION_VIBRATION_IS_ON, false);
    }

    public void setNotificationVibrationEnabled(boolean enabled) {
        mPreferences.edit().putBoolean(Keys.NOTIFICATION_VIBRATION_IS_ON, enabled).apply();
    }

    public boolean isNotificationLedEnabled() {
        return mPreferences.getBoolean(Keys.NOTIFICATION_LED_IS_ON, false);
    }

    public void setNotificationLedEnabled(boolean enabled) {
        mPreferences.edit().putBoolean(Keys.NOTIFICATION_LED_IS_ON, enabled).apply();
    }

    public boolean isNotificationSoundEnabled() {
        return mPreferences.getBoolean(Keys.NOTIFICATION_SOUND_IS_ON, false);
    }

    public void setNotificationSoundEnabled(boolean enabled) {
        mPreferences.edit().putBoolean(Keys.NOTIFICATION_SOUND_IS_ON, enabled).apply();
    }

    //ads
    public boolean isTimeToShowAds() {
        return System.currentTimeMillis() - getLastTimeAdsShows() >=
                FirebaseRemoteConfig.getInstance().getLong(PERIOD_BETWEEN_INTERSTITIAL_IN_MILLIS);
    }

    /**
     * @return true if there is less then some minutes before we must show it
     */
    public boolean isTimeToLoadAds() {
        //i.e. 1 hour - (17:56-17:00) = 4 min, which we compate to 5 min
        return FirebaseRemoteConfig.getInstance().getLong(PERIOD_BETWEEN_INTERSTITIAL_IN_MILLIS) -
                (System.currentTimeMillis() - getLastTimeAdsShows())
                <= PERIOD_BEFORE_INTERSTITIAL_MUST_BE_SHOWN_IN_MILLIS;

    }

    public void applyAwardFromAds() {
        long time = System.currentTimeMillis()
                + FirebaseRemoteConfig.getInstance().getLong(REWARDED_VIDEO_COOLDOWN_IN_MILLIS);
        setLastTimeAdsShows(time);
        //also set time for which we should disable banners
        setTimeForWhichBannersDisabled(time);

        setFreeAdsDisableRewardGainedCount(getFreeAdsDisableRewardGainedCount() + 1);
    }

    public boolean isRewardedDescriptionShown() {
        return mPreferences.getBoolean(Keys.ADS_REWARDED_DESCRIPTION_IS_SHOWN, false);
    }

    public void setRewardedDescriptionIsNotShown(boolean isShown) {
        mPreferences.edit().putBoolean(Keys.ADS_REWARDED_DESCRIPTION_IS_SHOWN, isShown).apply();
    }

    public void setLastTimeAdsShows(long timeInMillis) {
        mPreferences.edit().putLong(Keys.ADS_LAST_TIME_SHOWS, timeInMillis).apply();
    }

    private long getLastTimeAdsShows() {
        long timeFromLastShow = mPreferences.getLong(Keys.ADS_LAST_TIME_SHOWS, 0);
        if (timeFromLastShow == 0) {
            setLastTimeAdsShows(System.currentTimeMillis());
        }
        return timeFromLastShow;
    }

    public void setNumOfInterstitialsShown(int numOfInterstitialsShown) {
        mPreferences.edit().putInt(Keys.ADS_NUM_OF_INTERSTITIALS_SHOWN, numOfInterstitialsShown).apply();
    }

    public int getNumOfInterstitialsShown() {
        return mPreferences.getInt(Keys.ADS_NUM_OF_INTERSTITIALS_SHOWN, 0);
    }

    public boolean isTimeToShowVideoInsteadOfInterstitial() {
        return getNumOfInterstitialsShown() >=
                FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.NUM_OF_INTERSITIAL_BETWEEN_REWARDED);
    }

    //banner disable from free ads disable options
    public boolean isTimeToShowBannerAds() {
        return System.currentTimeMillis() >= getTimeForWhichBannersDisabled();
    }

    private void setTimeForWhichBannersDisabled(long timeInMillis) {
        mPreferences.edit().putLong(Keys.TIME_FOR_WHICH_BANNERS_DISABLED, timeInMillis).apply();
    }

    private long getTimeForWhichBannersDisabled() {
        return mPreferences.getLong(Keys.TIME_FOR_WHICH_BANNERS_DISABLED, 0);
    }

    //app installs
    public boolean isAppInstalledForPackage(String packageName) {
        return mPreferences.getBoolean(Keys.PACKAGE_INSTALLED + packageName, false);
    }

    public void setAppInstalledForPackage(String packageName) {
        mPreferences.edit().putBoolean(Keys.PACKAGE_INSTALLED + packageName, true).apply();
    }

    public void applyAwardForAppInstall() {
        long time = System.currentTimeMillis() +
                FirebaseRemoteConfig.getInstance().getLong(APP_INSTALL_REWARD_IN_MILLIS);

        setLastTimeAdsShows(time);
        //also set time for which we should disable banners
        setTimeForWhichBannersDisabled(time);

        setFreeAdsDisableRewardGainedCount(getFreeAdsDisableRewardGainedCount() + 1);
    }

    //vk groups join
    public boolean isVkGroupJoined(String id) {
        return mPreferences.getBoolean(Keys.VK_GROUP_JOINED + id, false);
    }

    public void setVkGroupJoined(String id) {
        mPreferences.edit().putBoolean(Keys.VK_GROUP_JOINED + id, true).apply();
        if (id.equals(FirebaseRemoteConfig.getInstance().getString(Constants.Firebase.RemoteConfigKeys.VK_APP_GROUP_ID))) {
            setAppVkGroupJoined(true);
        }
    }

    public boolean isAppVkGroupJoined() {
        return mPreferences.getBoolean(Keys.APP_VK_GROUP_JOINED, false);
    }

    public void setAppVkGroupJoined(boolean joined) {
        mPreferences.edit().putBoolean(Keys.APP_VK_GROUP_JOINED, joined).apply();
    }

    public void applyAwardVkGroupJoined() {
        long time = System.currentTimeMillis()
                + FirebaseRemoteConfig.getInstance().getLong(FREE_VK_GROUPS_JOIN_REWARD);
        setLastTimeAdsShows(time);
        //also set time for which we should disable banners
        setTimeForWhichBannersDisabled(time);

        setFreeAdsDisableRewardGainedCount(getFreeAdsDisableRewardGainedCount() + 1);
    }

    public void applyAwardSignIn() {
        long time = System.currentTimeMillis()
                + FirebaseRemoteConfig.getInstance().getLong(AUTH_COOLDOWN_IN_MILLIS);
        setLastTimeAdsShows(time);
        //also set time for which we should disable banners
        setTimeForWhichBannersDisabled(time);

        setUserAwardedFromAuth(true);

        setFreeAdsDisableRewardGainedCount(getFreeAdsDisableRewardGainedCount() + 1);
    }

    public void setUserAwardedFromAuth(boolean awardedFromAuth) {
        mPreferences.edit().putBoolean(Keys.AWARD_FROM_AUTH_GAINED, awardedFromAuth).apply();
    }

    public boolean isUserAwardedFromAuth() {
        return mPreferences.getBoolean(Keys.AWARD_FROM_AUTH_GAINED, false);
    }

    //subscription
    public void setHasSubscription(boolean hasSubscription) {
        mPreferences.edit().putBoolean(Keys.HAS_SUBSCRIPTION, hasSubscription).apply();
    }

    public boolean isHasSubscription() {
        return mPreferences.getBoolean(Keys.HAS_SUBSCRIPTION, false);
////       FIX ME test
//        return true;
    }

    /**
     * @return true if user has any subscription (no ads or full version)
     */
    public boolean isHasAnySubscription() {
        return isHasNoAdsSubscription() || isHasSubscription();
    }

    /**
     * its a subscription that only removes ads
     */
    public void setHasNoAdsSubscription(boolean hasSubscription) {
        mPreferences.edit().putBoolean(Keys.HAS_NO_ADS_SUBSCRIPTION, hasSubscription).apply();
    }

    /**
     * its a subscription that only removes ads
     */
    public boolean isHasNoAdsSubscription() {
        return mPreferences.getBoolean(Keys.HAS_NO_ADS_SUBSCRIPTION, false);
    }

    private int getFreeAdsDisableRewardGainedCount() {
        return mPreferences.getInt(Keys.FREE_ADS_DISABLE_REWARD_GAINED_COUNT, 0);
    }

    public void setFreeAdsDisableRewardGainedCount(int count) {
        mPreferences.edit().putInt(Keys.FREE_ADS_DISABLE_REWARD_GAINED_COUNT, count).apply();
    }

    public boolean isTimeOfferFreeTrialFromDisableAdsOption() {
        return getFreeAdsDisableRewardGainedCount() >= NUM_OF_DISABLE_ADS_REWARDS_COUNT_BEFORE_OFFER_SHOWING;
    }

    public void setLastTimePeriodicalFreeTrialOffered(long timeInMillis) {
        mPreferences.edit().putLong(Keys.FREE_TRIAL_OFFERED_PERIODICAL, timeInMillis).apply();
    }

    private long getLastTimePeriodicalFreeTrialOffered() {
        long timeFromLastShow = mPreferences.getLong(Keys.FREE_TRIAL_OFFERED_PERIODICAL, 0);
        if (timeFromLastShow == 0) {
            setLastTimeAdsShows(System.currentTimeMillis());
        }
        return timeFromLastShow;
    }

    public boolean isTimeToPeriodicalOfferFreeTrial() {
        Timber.d("getLastTimePeriodicalFreeTrialOffered/FREE_TRIAL_OFFERED_PERIOD: %s/%s", getLastTimePeriodicalFreeTrialOffered(), FREE_TRIAL_OFFERED_PERIOD);
        return System.currentTimeMillis() - getLastTimePeriodicalFreeTrialOffered() >= FREE_TRIAL_OFFERED_PERIOD;
    }
    //subscriptions end

    @Override
    public boolean isDownloadAllEnabledForFree() {
        return FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_ALL_ENABLED_FOR_FREE);
    }

    @Override
    public int getScorePerArt() {
        return (int) FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_SCORE_PER_ARTICLE);
    }

    @Override
    public int getFreeOfflineLimit() {
        return (int) FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_FREE_ARTICLES_LIMIT);
    }

    //auto sync
    public void setNumOfAttemptsToAutoSync(long numOfAttemptsToAutoSync) {
        mPreferences.edit().putLong(Keys.AUTO_SYNC_ATTEMPTS, numOfAttemptsToAutoSync).apply();
    }

    public long getNumOfAttemptsToAutoSync() {
        return mPreferences.getLong(Keys.AUTO_SYNC_ATTEMPTS, 0);
    }

    public void addUnsyncedScore(int scoreToAdd) {
        int newTotalScore = getNumOfUnsyncedScore() + scoreToAdd;
        mPreferences.edit().putInt(Keys.UNSYNCED_SCORE, newTotalScore).apply();
    }

    public void addUnsyncedVkGroup(String id) {
        VkGroupsToJoinResponse data = getUnsyncedVkGroupsJson();
        if (data == null) {
            data = new VkGroupsToJoinResponse();
            data.items = new ArrayList<>();
        }
        VkGroupToJoin item = new VkGroupToJoin(id);
        if (!data.items.contains(item)) {
            data.items.add(item);
            mPreferences.edit().putString(Keys.UNSYNCED_VK_GROUPS, mGson.toJson(data)).apply();
        }
    }

    public void deleteUnsyncedVkGroups() {
        mPreferences.edit().remove(Keys.UNSYNCED_VK_GROUPS).apply();
    }

    public VkGroupsToJoinResponse getUnsyncedVkGroupsJson() {
        VkGroupsToJoinResponse data = null;
        try {
            data = mGson.fromJson(mPreferences.getString(Keys.UNSYNCED_VK_GROUPS, null), VkGroupsToJoinResponse.class);
        } catch (Exception e) {
            Timber.e(e);
        }
        return data;
    }

    public void addUnsyncedApp(String id) {
        ApplicationsResponse data = getUnsyncedAppsJson();
        if (data == null) {
            data = new ApplicationsResponse();
            data.items = new ArrayList<>();
        }
        PlayMarketApplication item = new PlayMarketApplication(id);
        if (!data.items.contains(item)) {
            data.items.add(item);
            mPreferences.edit().putString(Keys.UNSYNCED_APPS, mGson.toJson(data)).apply();
        }
    }

    public void deleteUnsyncedApps() {
        mPreferences.edit().remove(Keys.UNSYNCED_APPS).apply();
    }

    public ApplicationsResponse getUnsyncedAppsJson() {
        ApplicationsResponse data = null;
        try {
            data = mGson.fromJson(mPreferences.getString(Keys.UNSYNCED_APPS, null), ApplicationsResponse.class);
        } catch (Exception e) {
            Timber.e(e);
        }
        return data;
    }

    public void setNumOfUnsyncedScore(int totalScore) {
        mPreferences.edit().putInt(Keys.UNSYNCED_SCORE, totalScore).apply();
    }

    public int getNumOfUnsyncedScore() {
        return mPreferences.getInt(Keys.UNSYNCED_SCORE, 0);
    }

    //check vk group joined
    public void setLastTimeAppVkGroupJoinedChecked(long timeInMillis) {
        mPreferences.edit().putLong(Keys.APP_VK_GROUP_JOINED_LAST_TIME_CHECKED, timeInMillis).apply();
    }

    private long getLastTimeAppVkGroupJoinedChecked() {
        long timeFromLastShow = mPreferences.getLong(Keys.APP_VK_GROUP_JOINED_LAST_TIME_CHECKED, 0);
        if (timeFromLastShow == 0) {
            setLastTimeAppVkGroupJoinedChecked(System.currentTimeMillis());
        }
        return timeFromLastShow;
    }

    public boolean isTimeToCheckAppVkGroupJoined() {
        return System.currentTimeMillis() - getLastTimeAppVkGroupJoinedChecked() >= PERIOD_BETWEEN_APP_VK_GROUP_JOINED_CHECK_IN_MILLIS;
    }

    //periodical update of subscriptions
    public void setLastTimeSubscriptionsValidated(long timeInMillis) {
        mPreferences.edit().putLong(Keys.LAST_TIME_SUBSCRIPTIONS_INVALIDATED, timeInMillis).apply();
    }

    private long getLastTimeSubscriptionsValidated() {
        long timeFromLastShow = mPreferences.getLong(Keys.LAST_TIME_SUBSCRIPTIONS_INVALIDATED, 0);
        if (timeFromLastShow == 0) {
            setLastTimeSubscriptionsValidated(System.currentTimeMillis());
        }
        return timeFromLastShow;
    }

    public boolean isTimeToValidateSubscriptions() {
        return System.currentTimeMillis() - getLastTimeSubscriptionsValidated() >= PERIOD_BETWEEN_SUBSCRIPTIONS_INVALIDATION_IN_MILLIS;
    }

    //utils
    public boolean isLicenceAccepted() {
        return mPreferences.getBoolean(Keys.LICENCE_ACCEPTED, false);
    }

    public void setLicenceAccepted(boolean accepted) {
        mPreferences.edit().putBoolean(Keys.LICENCE_ACCEPTED, accepted).apply();
    }

    public boolean isPersonalDataAccepted() {
        return mPreferences.getBoolean(Keys.PERSONAL_DATA_ACCEPTED, false);
    }

    public void setPersonalDataAccepted(boolean accepted) {
        mPreferences.edit().putBoolean(Keys.PERSONAL_DATA_ACCEPTED, accepted).apply();
    }

    public boolean isDataRestored() {
        return mPreferences.getBoolean(Keys.DATA_RESTORED, false);
    }

    public void setDataIsRestored(boolean restored) {
        mPreferences.edit().putBoolean(Keys.DATA_RESTORED, restored).apply();
    }

    public int getCurAppVersion() {
        return mPreferences.getInt(Keys.CUR_APP_VERSION, 0);
    }

    public void setCurAppVersion(int versionCode) {
        mPreferences.edit().putInt(Keys.CUR_APP_VERSION, versionCode).apply();
    }
}