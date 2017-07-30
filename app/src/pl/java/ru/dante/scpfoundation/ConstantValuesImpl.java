package ru.dante.scpfoundation;

import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.Constants;

/**
 * Created by mohax on 15.07.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ConstantValuesImpl implements ConstantValues {

    @Override
    public ApiValues getApiValues() {
        return new ApiValuesImpl();
    }

    @Override
    public UrlsValues getUrlsValues() {
        return new UrlsValuesImpl();
    }

    public class UrlsValuesImpl implements UrlsValues {

        @Override
        public String getBaseApiUrl() {
            return Urls.BASE_API_URL;
        }

        @Override
        public String getMain() {
            return Urls.MAIN;
        }

        @Override
        public String getMostRated() {
            return Constants.Urls.RATE;
        }

        @Override
        public String getNewArticles() {
            return Urls.NEW_ARTICLES;
        }

        @Override
        public String getObjects1() {
            return Urls.OBJECTS_1;
        }

        @Override
        public String getObjects2() {
            return Urls.OBJECTS_2;
        }

        @Override
        public String getObjects3() {
            return Urls.OBJECTS_3;
        }

        @Override
        public String getObjects4() {
            return Urls.OBJECTS_4;
        }

        @Override
        public String getObjectsRu() {
            return Urls.OBJECTS_PL;
        }

        @Override
        public String getExperiments() {
            return Constants.Urls.EXPERIMENTS;
        }

        @Override
        public String getIncidents() {
            return Constants.Urls.INCEDENTS;
        }

        @Override
        public String getInterviews() {
            return Constants.Urls.INTERVIEWS;
        }

        @Override
        public String getJokes() {
            return Constants.Urls.JOKES;
        }

        @Override
        public String getArchive() {
            return Constants.Urls.ARCHIVE;
        }

        @Override
        public String getOthers() {
            return Constants.Urls.OTHERS;
        }

        @Override
        public String getLeaks() {
            return Constants.Urls.LEAKS;
        }

        @Override
        public String getAbout() {
            return Urls.ABOUT_SCP;
        }

        @Override
        public String getNews() {
            return Constants.Urls.NEWS;
        }

        @Override
        public String getStories() {
            return Constants.Urls.STORIES;
        }

        @Override
        public String[] getAllLinksArray() {
            return Urls.ALL_LINKS_ARRAY;
        }
    }

    public class ApiValuesImpl implements ApiValues {

        @Override
        public String getMostRecentUrl() {
            return Api.MOST_RECENT_URL;
        }

        @Override
        public String getMostRatedUrl() {
            return Constants.Api.MOST_RATED_URL;
        }

        @Override
        public String getSearchSiteUrl() {
            return Api.SEARCH_URL;
        }

        @Override
        public String getRandomPageUrl() {
            return Constants.Api.RANDOM_PAGE_SCRIPT_URL;
        }

        @Override
        public int getNumOfArticlesOnRecentPage() {
            return Api.NUM_OF_ARTICLES_ON_RECENT_PAGE;
        }

        @Override
        public int getNumOfArticlesOnRatedPage() {
            return Api.NUM_OF_ARTICLES_ON_RATED_PAGE;
        }

        @Override
        public int getNumOfArticlesOnSearchPage() {
            return Api.NUM_OF_ARTICLES_ON_SEARCH_PAGE;
        }
    }

    interface Api {
        String MOST_RECENT_URL = "/ostatnio-stworzone/p/";
        String MOST_RATED_URL = "/top-rated-pages/p/";
        /**
         * first arg is searchQuery with SPACEs replaced by "%20"
         * second - num of page
         */
        String SEARCH_URL = "/search:site/a/p/q/%1$s/p/%2$s";
        String RANDOM_PAGE_SCRIPT_URL = Urls.BASE_API_URL + "/random:random-scp";
        int NUM_OF_ARTICLES_ON_RECENT_PAGE = 30;
        int NUM_OF_ARTICLES_ON_RATED_PAGE = 100;
        int NUM_OF_ARTICLES_ON_SEARCH_PAGE = 10;
    }

    interface Urls {
        String BASE_API_URL = "http://scp-pl.wikidot.com";

        String MAIN = BASE_API_URL + "/";

        String ABOUT_SCP = BASE_API_URL + "/o-fundacji-scp";
        String NEWS = BASE_API_URL + "/news";
        String RATE = BASE_API_URL + "/top-rated-pages";
        String NEW_ARTICLES = BASE_API_URL + "/ostatnio-stworzone";

        String OBJECTS_1 = BASE_API_URL + "/lista-eng";
        String OBJECTS_2 = BASE_API_URL + "/lista-eng-2";
        String OBJECTS_3 = BASE_API_URL + "/lista-eng-3";
        String OBJECTS_4 = BASE_API_URL + "/lista-eng-4";

        String OBJECTS_PL = BASE_API_URL + "/lista-pl";

        String SEARCH = "SEARCH";
        String FAVORITES = "FAVORITES";
        String OFFLINE = "OFFLINE";
        String MATERIALS_ALL = "MATERIALS_ALL";
        //TODO catch this link from system
        String TAGS_SEARCH_ON_SITE = BASE_API_URL + "/tags";
        String TAGS_SEARCH = "TAGS_SEARCH";

        String ARCHIVE = BASE_API_URL + "/archived-scps";
        String JOKES = BASE_API_URL + "/joke-scps";
        String SCP_EX = BASE_API_URL + "/scp-ex";
        String LOCATIONS = BASE_API_URL + "/log-of-anomalous-locations";
        String EVENTS = BASE_API_URL + "/log-of-extranormal-events";
        String ANOMALS = BASE_API_URL + "/log-of-anomalous-items";
        String INCEDENTS = BASE_API_URL + "/incident-reports-eye-witness-interviews-and-personal-logs";

        String[] ALL_LINKS_ARRAY = {MAIN, RATE, NEW_ARTICLES,
                EVENTS, INCEDENTS, LOCATIONS, ANOMALS, JOKES, ARCHIVE, SCP_EX,
                OBJECTS_1, OBJECTS_2, OBJECTS_3, OBJECTS_4,
                NEWS, SEARCH};
    }
}