package ru.dante.scpfoundation.api;

import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import ru.dante.scpfoundation.MyApplicationImpl;
import ru.dante.scpfoundation.R;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.BuildConfig;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.api.service.EnScpSiteApi;
import ru.kuchanov.scpcore.api.service.ScpReaderAuthApi;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.downloads.ScpParseException;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import rx.Observable;
import rx.Single;
import timber.log.Timber;

/**
 * Created by mohax on 13.07.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ApiClientImpl extends ApiClient {

    public ApiClientImpl(
            final OkHttpClient okHttpClient,
            final Retrofit vpsRetrofit,
            final Retrofit scpRetrofit,
            final Retrofit scpReaderRetrofit,
            final ScpReaderAuthApi scpReaderAuthApi,
            final EnScpSiteApi enScpSiteApi,
            final MyPreferenceManager preferencesManager,
            final Gson gson,
            final ConstantValues constantValues
    ) {
        super(
                okHttpClient,
                vpsRetrofit,
                scpRetrofit,
                scpReaderRetrofit,
                scpReaderAuthApi,
                enScpSiteApi,
                preferencesManager,
                gson,
                constantValues
        );
    }

    @Override
    public Single<String> getRandomUrl() {
        Timber.d("getRandomUrl");
        return Single.create(subscriber -> {
            final Request.Builder request = new Request.Builder();
            request.url(mConstantValues.getRandomPageUrl());
            request.get();

            try {
                final OkHttpClient client = new OkHttpClient.Builder()
                        .followRedirects(false)
                        .addInterceptor(new HttpLoggingInterceptor(message -> Timber.d(message)).setLevel(
                                BuildConfig.FLAVOR_mode.equals("dev")
                                ? HttpLoggingInterceptor.Level.BODY
                                : HttpLoggingInterceptor.Level.NONE
                        ))
                        .build();
                final Response response = client.newCall(request.build()).execute();

                final ResponseBody requestResult = response.body();
                if (requestResult != null) {
                    String html = requestResult.string();
                    final String patternToFindUrl = "<iframe src=\"http://snippets.wdfiles.com/local--code/code:iframe-redirect#";
                    html = html.substring(html.indexOf(patternToFindUrl) + patternToFindUrl.length());
                    html = html.substring(0, html.indexOf("\""));
                    final String randomURL = html;
                    Timber.d("randomUrl = %s", randomURL);
                    subscriber.onSuccess(randomURL);
                } else {
                    subscriber.onError(new ScpParseException(MyApplicationImpl.getAppInstance().getString(R.string.error_parse)));
                }
            } catch (final IOException e) {
                Timber.e(e);
                subscriber.onError(e);
            }
        });
    }

    @Override
    public Single<Integer> getRecentArticlesPageCountObservable() {
        return Single.create(subscriber -> {
            final Request request = new Request.Builder()
                    .url(mConstantValues.getNewArticles() + "/p/1")
                    .build();

            final String responseBody;
            try {
                final Response response = mOkHttpClient.newCall(request).execute();
                final ResponseBody body = response.body();
                if (body != null) {
                    responseBody = body.string();
                } else {
                    subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(ru.kuchanov.scpcore.R.string.error_parse)));
                    return;
                }
            } catch (final IOException e) {
                subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(R.string.error_connection)));
                return;
            }
            try {
                final Document doc = Jsoup.parse(responseBody);

                //get num of pages
                final Element spanWithNumber = doc.getElementsByClass("pager-no").first();
                final String text = spanWithNumber.text();
                final Integer numOfPages = Integer.valueOf(text.substring(text.lastIndexOf(" ") + 1));

                subscriber.onSuccess(numOfPages);
            } catch (final Exception e) {
                Timber.e(e, "error while get arts list");
                subscriber.onError(e);
            }
        });
    }

    @Override
    protected List<Article> parseForRecentArticles(final Document doc) throws ScpParseException {
        final Element contentTypeDescription = doc.getElementsByClass("content-type-description").first();
        final Element pageContent = contentTypeDescription.getElementsByTag("table").first();
        if (pageContent == null) {
            throw new ScpParseException(MyApplicationImpl.getAppInstance().getString(R.string.error_parse));
        }

        final List<Article> articles = new ArrayList<>();
        final Elements listOfElements = pageContent.getElementsByTag("tr");
        for (int i = 1/*start from 1 as first row is tables header*/; i < listOfElements.size(); i++) {
            final Elements listOfTd = listOfElements.get(i).getElementsByTag("td");
            final Element firstTd = listOfTd.first();
            final Element tagA = firstTd.getElementsByTag("a").first();

            final String title = tagA.text();
            final String url = mConstantValues.getBaseApiUrl() + tagA.attr("href");
            //4 Jun 2017, 22:25
            //createdDate
            final Element createdDateNode = listOfTd.get(1);
            final String createdDate = createdDateNode.text().trim();

            final Article article = new Article();
            article.title = title;
            article.url = url.trim();
            article.createdDate = createdDate;
            articles.add(article);
        }

        return articles;
    }

    @Override
    protected List<Article> parseForRatedArticles(Document doc) throws ScpParseException {
        final Element pageContent = doc.getElementById("page-content");
        if (pageContent == null) {
            throw new ScpParseException(MyApplicationImpl.getAppInstance().getString(R.string.error_parse));
        }
        final Element listPagesBox = pageContent.getElementsByClass("list-pages-box").first();
        if (listPagesBox == null) {
            throw new ScpParseException(MyApplicationImpl.getAppInstance().getString(R.string.error_parse));
        }

        final String allArticles = listPagesBox.getElementsByTag("p").first().html();
        final String[] arrayOfArticles = allArticles.split("<br>");
        final List<Article> articles = new ArrayList<>();
        for (final String arrayItem : arrayOfArticles) {
            doc = Jsoup.parse(arrayItem);
            final Element aTag = doc.getElementsByTag("a").first();
            final String url = mConstantValues.getBaseApiUrl() + aTag.attr("href");
            final String title = aTag.text();

            String rating = arrayItem.substring(arrayItem.indexOf("Bewertung: ") + "Bewertung: ".length());
            rating = rating.substring(0, rating.indexOf(", "));

            final Article article = new Article();
            article.url = url;
            article.rating = Integer.parseInt(rating);
            article.title = title;
            articles.add(article);
        }

        return articles;
    }

    @Override
    protected List<Article> parseForObjectArticles(final Document doc) throws ScpParseException {
        final Element pageContent = doc.getElementById("page-content");
        if (pageContent == null) {
            throw new ScpParseException(MyApplicationImpl.getAppInstance().getString(R.string.error_parse));
        }
        final Elements listPagesBox = pageContent.getElementsByTag("h1");
        listPagesBox.remove();
        final Element collapsibleBlock = pageContent.getElementsByTag("ul").first();
        collapsibleBlock.remove();
//        Element table = pageContent.getElementsByClass("content-toc").first();
//        table.remove();
        final Elements allUls = pageContent.getElementsByClass("content-panel").first().getElementsByTag("ul");

        final List<Article> articles = new ArrayList<>();

        for (final Element ul : allUls) {
            final Elements allLi = ul.children();
            for (final Element li : allLi) {
                //do not add empty articles
                if (li.getElementsByTag("a").first().hasClass("newpage")) {
                    continue;
                }
                final Article article = new Article();
                article.url = mConstantValues.getBaseApiUrl() + li.getElementsByTag("a").first().attr("href");
                article.title = li.text();
                articles.add(article);
            }
        }

        return articles;
    }

    @Override
    public Single<List<ArticleTag>> getTagsFromSite() {
        return Single.create(subscriber -> {
            final Request request = new Request.Builder()
                    .url(mConstantValues.getBaseApiUrl() + "/system:page-tags/")
                    .build();

            final String responseBody;
            try {
                final Response response = mOkHttpClient.newCall(request).execute();
                final ResponseBody body = response.body();
                if (body != null) {
                    responseBody = body.string();
                } else {
                    subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(R.string.error_parse)));
                    return;
                }
            } catch (final IOException e) {
                subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(R.string.error_connection)));
                return;
            }
            try {
                final Document doc = Jsoup.parse(responseBody);
                final Element pageContent = doc.getElementById("page-content");
                if (pageContent == null) {
                    subscriber.onError(new ScpParseException(BaseApplication.getAppInstance().getString(R.string.error_parse)));
                    return;
                }

                final List<ArticleTag> tags = new ArrayList<>();

                final Element allTags = doc.getElementsByClass("pages-tag-cloud-box").first();
                for (final Element tagNode : allTags.getElementsByClass("tag")) {
                    final ArticleTag tag = new ArticleTag();
                    tag.title = tagNode.text();
                    tags.add(tag);
                }
                //parse end
                subscriber.onSuccess(tags);
            } catch (final Exception e) {
                Timber.e(e, "error while get arts list");
                subscriber.onError(e);
            }
        });
    }

    @Override
    public Single<List<Article>> getArticlesByTags(final List<ArticleTag> tags) {
//        Timber.d("getArticlesByTags: %s", tags);
//        String tagName = tags.get(0).title;
//        Timber.d("tagName: %s", tagName);
        final List<String> tagsTitles = ArticleTag.getStringsFromTags(tags);
        //fix index of bounds error
        if (tagsTitles.isEmpty()) {
            return Single.just(Collections.emptyList());
        }
//        Timber.d("tagsTitles: %s", tagsTitles);
        final String tagTitle = tagsTitles.get(0);
//        Timber.d("tagTitle: %s", tagTitle);
        return Single.create(subscriber -> {
            final Request request = new Request.Builder()
                    .url(mConstantValues.getBaseApiUrl() + "/system:page-tags/tag/" + tagTitle)
                    .build();

            final String responseBody;
            try {
                final Response response = mOkHttpClient.newCall(request).execute();
                final ResponseBody body = response.body();
                if (body != null) {
                    responseBody = body.string();
                } else {
                    subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(R.string.error_parse)));
                    return;
                }
            } catch (final IOException e) {
                subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(R.string.error_connection)));
                return;
            }
            try {
                final Document doc = Jsoup.parse(responseBody);
                final Element pageContent = doc.getElementById("page-content");
                if (pageContent == null) {
                    subscriber.onError(new ScpParseException(BaseApplication.getAppInstance().getString(R.string.error_parse)));
                    return;
                }

                final List<Article> articles = new ArrayList<>();

                final Element allTags = doc.getElementById("tagged-pages-list");
                for (final Element tagNode : allTags.getElementsByTag("a")) {
                    final Article tag = new Article();
                    tag.title = tagNode.text();
                    tag.url = mConstantValues.getBaseApiUrl() + tagNode.attr("href");
                    articles.add(tag);
                }
                //parse end
                subscriber.onSuccess(articles);
            } catch (final Exception e) {
                Timber.e(e, "error while get arts list");
                subscriber.onError(e);
            }
        });
    }

    @Override
    protected String getScpServerWiki() {
        return "scp-wiki";
    }
}