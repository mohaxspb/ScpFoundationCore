package ru.kuchanov.scpcore;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public interface Constants {

    interface Api {
        int ZERO_OFFSET = 0;

        String MOST_RECENT_URL = "/most-recently-created/p/";
        String MOST_RATED_URL = "/top-rated-pages/p/";
        /**
         * first arg is searchQuery with SPACEs replaced by "%20"
         * second - num of page
         */
        String SEARCH_URL = "/search:site/a/p/q/%1$s/p/%2$s";
        String RANDOM_PAGE_SCRIPT_URL = "https://node.scpfoundation.net/wikidot_random_page";

        int NUM_OF_ARTICLES_ON_RECENT_PAGE = 30;
        int NUM_OF_ARTICLES_ON_RATED_PAGE = 20;
        int NUM_OF_ARTICLES_ON_SEARCH_PAGE = 10;
        //gallery
        int GALLERY_VK_GROUP_ID = -98801766;
        int GALLERY_VK_ALBUM_ID = 219430203;

        String NOT_TRANSLATED_ARTICLE_UTIL_URL = "not_translated_yet_article_which_we_cant_show";
        String NOT_TRANSLATED_ARTICLE_URL_DELIMITER = "___";
    }

    interface Urls {
        String MAIN = "http://scpfoundation.ru/";

        String RATE = "http://scpfoundation.ru/top-rated-pages";
        String NEW_ARTICLES = "http://scpfoundation.ru/most-recently-created";
        String OBJECTS_1 = "http://scpfoundation.ru/scp-list";
        String OBJECTS_2 = "http://scpfoundation.ru/scp-list-2";
        String OBJECTS_3 = "http://scpfoundation.ru/scp-list-3";
        String OBJECTS_4 = "http://scpfoundation.ru/scp-list-4";
        String OBJECTS_RU = "http://scpfoundation.ru/scp-list-ru";

        //materials
        String EXPERIMENTS = "http://scpfoundation.ru/experiment-logs";
        String INCEDENTS = "http://scpfoundation.ru/incident-reports";
        String INTERVIEWS = "http://scpfoundation.ru/eye-witness-interviews";
        String JOKES = "http://scpfoundation.ru/scp-list-j";
        String ARCHIVE = "http://scpfoundation.ru/archive";
        String OTHERS = "http://scpfoundation.ru/other";
        String LEAKS = "http://scpfoundation.ru/the-leak";

        String ABOUT_SCP = "http://scpfoundation.ru/about-the-scp-foundation";
        String NEWS = "http://scpfoundation.ru/news";
        String STORIES = "http://scpfoundation.ru/stories";

        String SEARCH = "SEARCH";
        String FAVORITES = "FAVORITES";
        String OFFLINE = "OFFLINE";

        String[] ALL_LINKS_ARRAY = {MAIN, RATE, NEW_ARTICLES, EXPERIMENTS, INCEDENTS, INTERVIEWS, OTHERS, STORIES, LEAKS, OBJECTS_1, OBJECTS_2, OBJECTS_3, OBJECTS_RU, NEWS};
        String BASE_API_URL = "http://scpfoundation.ru";
    }

    interface Firebase {

        enum CallToActionReason {
            REMOVE_ADS, ENABLE_AUTO_SYNC, SYNC_NEED_AUTH, ENABLE_FONTS
        }

        enum SocialProvider {
            VK(R.string.social_provider_vk_title, R.drawable.ic_social_vk),
            GOOGLE(R.string.social_provider_google_title, R.drawable.ic_social_google),
            FACEBOOK(R.string.social_provider_facebook_title, R.drawable.ic_social_facebook);

            @StringRes
            private final int title;

            @DrawableRes
            private final int icon;

            SocialProvider(int title, int icon) {
                this.title = title;
                this.icon = icon;
            }

            @DrawableRes
            public int getIcon() {
                return icon;
            }

            public int getTitle() {
                return title;
            }
        }

        interface Refs {
            String USERS = "users";
            String ARTICLES = "articles";
            String SCORE = "score";
            String VK_GROUPS = "vkGroups";
            String APPS = "apps";
            String INAPP = "inapp";
            String CRACKED = "cracked";
            String SOCIAL_PROVIDER = "socialProviders";
            String SIGN_IN_REWARD_GAINED = "signInRewardGained";
        }

        interface RemoteConfigKeys {
            //ads
            String NUM_OF_GALLERY_PHOTOS_BETWEEN_INTERSITIAL = "num_of_gallery_photos_between_intersitial";
            String PERIOD_BETWEEN_INTERSTITIAL_IN_MILLIS = "period_between_interstitial_in_millis";
            String NUM_OF_INTERSITIAL_BETWEEN_REWARDED = "num_of_intersitial_between_rewarded";
            String NUM_OF_SYNC_ATTEMPTS_BEFORE_CALL_TO_ACTION = "num_of_sync_attempts_before_call_to_action";
            //enabled options
            String FREE_INVITES_ENABLED = "free_invites_enabled";
            String FREE_APPS_INSTALL_ENABLED = "free_apps_install_enabled";
            String FREE_REWARDED_VIDEO_ENABLED = "free_rewarded_video_enabled";
            String FREE_VK_GROUPS_ENABLED = "free_vk_groups_enabled";
            String FREE_AUTH_ENABLED = "free_auth_enabled";
            //actions variants
            String APPS_TO_INSTALL_JSON = "apps_to_install_json";
            String VK_GROUPS_TO_JOIN_JSON = "vk_groups_to_join_json";
            //disable ads
            String APP_INSTALL_REWARD_IN_MILLIS = "app_install_reward_in_millis";
            String FREE_VK_GROUPS_JOIN_REWARD = "free_vk_groups_join_reward";
            String REWARDED_VIDEO_COOLDOWN_IN_MILLIS = "rewarded_video_cooldown_in_millis";
            String AUTH_COOLDOWN_IN_MILLIS = "auth_cooldown_in_millis";
            //score multipliers
            String VK_APP_GROUP_ID = "vk_app_group_id";
            String SCORE_MULTIPLIER_SUBSCRIPTION = "score_multiplier_subscription";
            String SCORE_MULTIPLIER_VK_GROUP_APP = "score_multiplier_vk_group_app";
            //score
            String SCORE_ACTION_FAVORITE = "score_action_favorite";
            String SCORE_ACTION_READ = "score_action_read";
            String SCORE_ACTION_INTERSTITIAL_SHOWN = "score_action_interstitial_shown";
            String SCORE_ACTION_VK_GROUP = "score_action_vk_group";
            String SCORE_ACTION_OUR_APP = "score_action_our_app";
            String SCORE_ACTION_REWARDED_VIDEO = "score_action_rewarded_video";
            String SCORE_ACTION_AUTH = "score_action_auth";
            String SCORE_ACTION_NONE = "score_action_none";
            String LEVELS_JSON = "levels_json";
            //downloads
            String DOWNLOAD_ALL_ENABLED_FOR_FREE = "download_all_enabled_for_free";
            String DOWNLOAD_FREE_ARTICLES_LIMIT = "download_free_articles_limit";
            String DOWNLOAD_SCORE_PER_ARTICLE = "download_score_per_article";

            String GALLERY_BANNER_DISABLED = "gallery_banner_disabled";
            String MAIN_BANNER_DISABLED = "main_banner_disabled";
            String ARTICLE_BANNER_DISABLED = "article_banner_disabled";

            String NO_ADS_SUBS_ENABLED = "no_ads_subs_enabled";

            String APP_LANG_VERSIONS = "app_lang_versions";
        }

        int REQUEST_INVITE = 1024;

        interface Analitics {

            interface EventType {
                String REWARD_GAINED = "REWARD_GAINED";
                String REWARD_REQUESTED = "REWARD_REQUESTED";
            }

            interface StartScreen {
                String SNACK_BAR = "SNACK_BAR";
                String MENU = "MENU";
                String DRAWER_HEADER_LOGINED = "DRAWER_HEADER_LOGINED";
                String FONT = "FONT";
                String AUTO_SYNC_SNACKBAR = "AUTO_SYNC_SNACKBAR";
                String AUTO_SYNC_FROM_SETTINGS = "AUTO_SYNC_FROM_SETTINGS";
                String DOWNLOAD_DIALOG = "DOWNLOAD_DIALOG";
            }

            String INVITED_FIVE_FRIENDS = "INVITED_FIVE_FRIENDS";
            String APP_CRACKED = "APP_CRACKED";
        }
    }
}