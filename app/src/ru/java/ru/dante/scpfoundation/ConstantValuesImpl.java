package ru.dante.scpfoundation;

import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.Constants;

/**
 * Created by mohax on 15.07.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ConstantValuesImpl implements ConstantValues {

    @Override
    public String getBaseApiUrl() {
        return Constants.Urls.BASE_API_URL;
    }

    @Override
    public String getMostRated() {
        return Constants.Urls.RATE;
    }

    @Override
    public String getNewArticles() {
        return Constants.Urls.NEW_ARTICLES;
    }

    @Override
    public String getObjects1() {
        return Constants.Urls.OBJECTS_1;
    }

    @Override
    public String getObjects2() {
        return Constants.Urls.OBJECTS_2;
    }

    @Override
    public String getObjects3() {
        return Constants.Urls.OBJECTS_3;
    }

    @Override
    public String getObjects4() {
        return Constants.Urls.OBJECTS_4;
    }

    @Override
    public String getObjectsRu() {
        return Constants.Urls.OBJECTS_RU;
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
        return Constants.Urls.ABOUT_SCP;
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
        return Constants.Urls.ALL_LINKS_ARRAY;
    }

    @Override
    public String getSearchSiteUrl() {
        return Constants.Api.SEARCH_URL;
    }

    @Override
    public String getRandomPageUrl() {
        return Constants.Api.RANDOM_PAGE_SCRIPT_URL;
    }

    @Override
    public int getNumOfArticlesOnRecentPage() {
        return Constants.Api.NUM_OF_ARTICLES_ON_RECENT_PAGE;
    }

    @Override
    public int getNumOfArticlesOnRatedPage() {
        return Constants.Api.NUM_OF_ARTICLES_ON_RATED_PAGE;
    }

    @Override
    public int getNumOfArticlesOnSearchPage() {
        return Constants.Api.NUM_OF_ARTICLES_ON_SEARCH_PAGE;
    }

    @Override
    public String getAppLang() {
        return "ru";
    }

    public String getObjectsFrUrl(){
        return "http://scpfoundation.ru/scp-list-fr";
    }
}