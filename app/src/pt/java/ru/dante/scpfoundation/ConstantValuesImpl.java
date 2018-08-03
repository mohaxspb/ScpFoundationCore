package ru.dante.scpfoundation;

import ru.kuchanov.scpcore.ConstantValues;

/**
 * Created by mohax on 15.07.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ConstantValuesImpl implements ConstantValues {

    @Override
    public String getBaseApiUrl() {
        return Urls.BASE_API_URL;
    }

    @Override
    public String getMostRated() {
        return Urls.RATE;
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
        return null;
    }

    @Override
    public String getIncidents() {
        return null;
    }

    @Override
    public String getInterviews() {
        return null;
    }

    @Override
    public String getJokes() {
        return Urls.JOKES;
    }

    @Override
    public String getArchive() {
        return null;
    }

    @Override
    public String getOthers() {
        return null;
    }

    @Override
    public String getLeaks() {
        return null;
    }

    @Override
    public String getAbout() {
        return Urls.ABOUT_SCP;
    }

    @Override
    public String getNews() {
        return null;
    }

    @Override
    public String getStories() {
        return null;
    }

    @Override
    public String[] getAllLinksArray() {
        return Urls.ALL_LINKS_ARRAY;
    }

    @Override
    public String getSearchSiteUrl() {
        return Api.SEARCH_URL;
    }

    @Override
    public String getRandomPageUrl() {
        return Api.RANDOM_PAGE_SCRIPT_URL;
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

    @Override
    public String getAppLang() {
        return "pt";
    }

    @Override
    public String getObjectsFr() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public String getObjectsJp() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public String getObjectsEs() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public String getObjectsPl() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public String getObjectsDe() {
        throw new IllegalStateException("not implemented");
    }

    interface Api {
        /**
         * first arg is searchQuery with SPACEs replaced by "%20"
         * second - num of page
         */
        String SEARCH_URL = "/search:site/a/p/q/%1$s/p/%2$s";
        String RANDOM_PAGE_SCRIPT_URL = Urls.BASE_API_URL + "/random:random-scp";
        //not used
        int NUM_OF_ARTICLES_ON_RECENT_PAGE = 30;
        //not used
        int NUM_OF_ARTICLES_ON_RATED_PAGE = 20;
        int NUM_OF_ARTICLES_ON_SEARCH_PAGE = 10;
    }

    public interface Urls {
        String BASE_API_URL = "http://scp-pt-br.wikidot.com";

        String MAIN = BASE_API_URL + "/";

        String ABOUT_SCP = BASE_API_URL + "/sobre";
        //not used
        String NEWS = BASE_API_URL + "/news";
        //not used
        String RATE = BASE_API_URL + "/najwyzej-ocenione";
        //not used
        String NEW_ARTICLES = BASE_API_URL + "/ostatnio-stworzone";

        String OBJECTS_1 = BASE_API_URL + "/scp-series";
        String OBJECTS_2 = BASE_API_URL + "/scp-series-2";
        String OBJECTS_3 = BASE_API_URL + "/scp-series-3";
        String OBJECTS_4 = BASE_API_URL + "/scp-series-4";

        String OBJECTS_PL = BASE_API_URL + "/series-1-pt";

        String SEARCH = "SEARCH";

        String JOKES = BASE_API_URL + "/piadas";

        //not used
        String ARCHIVE = BASE_API_URL + "/archived-scps";
        //not used
        String SCP_EX = BASE_API_URL + "/scp-ex";
        //not used
        String LOCATIONS = BASE_API_URL + "/niewyjasnione";
        //not used
        String EVENTS = BASE_API_URL + "/log-of-extranormal-events";
        //not used
        String ANOMALS = BASE_API_URL + "/anomalne";
        //not used
        String INCEDENTS = BASE_API_URL + "/incident-reports-eye-witness-interviews-and-personal-logs";

        String[] ALL_LINKS_ARRAY = {MAIN, RATE, NEW_ARTICLES,
                EVENTS, INCEDENTS, LOCATIONS, ANOMALS, JOKES, ARCHIVE, SCP_EX,
                OBJECTS_1, OBJECTS_2, OBJECTS_3, OBJECTS_4,
                NEWS, SEARCH};
    }
}