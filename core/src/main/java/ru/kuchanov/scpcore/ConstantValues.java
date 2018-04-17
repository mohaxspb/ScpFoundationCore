package ru.kuchanov.scpcore;

/**
 * Created by mohax on 15.07.2017.
 * <p>
 * for ScpFoundationRu
 */
public interface ConstantValues {

    String getBaseApiUrl();

    String getMostRated();

    String getNewArticles();

    String getObjects1();

    String getObjects2();

    String getObjects3();

    String getObjects4();

    String getObjectsRu();

    String getExperiments();

    String getIncidents();

    String getInterviews();

    String getJokes();

    String getArchive();

    String getOthers();

    String getLeaks();

    String getAbout();

    String getNews();

    String getStories();

    String getSearchSiteUrl();

    String getRandomPageUrl();

    //other filials objects
    String getObjectsFr();

    String getObjectsJp();

    String getObjectsEs();

    String getObjectsPl();

    String getObjectsDe();
    //end other filials objects

    String[] getAllLinksArray();

    int getNumOfArticlesOnRecentPage();

    int getNumOfArticlesOnRatedPage();

    int getNumOfArticlesOnSearchPage();

    String getAppLang();
}