package ru.dante.scpfoundation.api;

import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import ru.dante.scpfoundation.MyApplicationImpl;
import ru.dante.scpfoundation.R;
import ru.kuchanov.scpcore.BaseApplication;
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

import static ru.kuchanov.scpcore.api.service.EnScpSiteApiKt.EN_SITE_TAG_SORT;

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
    public Observable<String> getRandomUrl() {
        Timber.d("getRandomUrl");
        return Observable.unsafeCreate(subscriber -> {
            final Request.Builder request = new Request.Builder();
            request.url(mConstantValues.getRandomPageUrl());
            request.get();

            try {
                final OkHttpClient client = new OkHttpClient.Builder()
                        .followRedirects(true)
                        .addInterceptor(new HttpLoggingInterceptor(message -> Timber.d(message)).setLevel(HttpLoggingInterceptor.Level.BODY))
                        .build();
                final Response response = client.newCall(request.build()).execute();

                final ResponseBody requestResult = response.body();
                if (requestResult != null) {
                    String html = requestResult.string();
                    html = html.substring(html.indexOf("<iframe src=\"http://snippets.wdfiles.com/local--code/code:iframe-redirect#") +
                            "<iframe src=\"http://snippets.wdfiles.com/local--code/code:iframe-redirect#".length());
                    html = html.substring(0, html.indexOf("\""));
                    final String randomURL = html;
                    Timber.d("randomUrl = %s", randomURL);
                    subscriber.onNext(randomURL);
                    subscriber.onCompleted();
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
    public Observable<Integer> getRecentArticlesPageCountObservable() {
        return Observable.unsafeCreate(subscriber -> {
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

                subscriber.onNext(numOfPages);
                subscriber.onCompleted();
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

            String rating = arrayItem.substring(arrayItem.indexOf("rating: ") + "rating: ".length());
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
        final Element table = pageContent.getElementsByClass("content-toc").first();
        table.remove();
        final Elements allUls = pageContent.getElementsByClass("content-panel").first().getElementsByTag("ul");

        final List<Article> articles = new ArrayList<>();

        for (final Element ul : allUls) {
            for (final Element li : ul.children()) {
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

    //todo parse tags
    @Override
    public Single<List<Article>> getArticlesByTags(final List<ArticleTag> tags) {
//        return mEnScpSiteApi.getArticlesByTags(ArticleTag.getCommaSeparatedStringFromTags(tags), EN_SITE_TAG_SORT)
//                .map(ArticleFromSearchTagsOnSite::getArticlesFromSiteArticles)
//                .map(articles -> {
//                    for (final Article article : articles) {
//                        if (!article.url.startsWith("http://")) {
//                            String start = mConstantValues.getBaseApiUrl();
//                            if (!article.url.startsWith("/")) {
//                                start += "/";
//                            }
//                            article.url = start + article.url;
//                        }
//                    }
//                    return articles;
//                });

//        @FormUrlEncoded
//        @POST("tools/tagGet.php")
//        fun getArticlesByTags(
//                @Field("tags") tags: String,
//                @Field("sort") sort: String
//    ): Single<String>

//        mOkHttpClient.
String tagsQuery = ArticleTag.getCommaSeparatedStringFromTags(tags);
        return Single.create(subscriber -> {
            final Request.Builder request = new Request.Builder();
            request.url("http://home.helenbot.com/tools/tagGet.php");
            final RequestBody requestBody = new FormBody.Builder()
                    .add("tags", tagsQuery)
                    .add("sort", /*EN_SITE_TAG_SORT*/"rating desc")
                    .build();
            request.post(requestBody);

            try {
                final Response response = mOkHttpClient.newCall(request.build()).execute();

                final ResponseBody requestResult = response.body();
                if (requestResult != null) {
                    String html = requestResult.string();
//                    html = html.substring(html.indexOf("<iframe src=\"http://snippets.wdfiles.com/local--code/code:iframe-redirect#") +
//                            "<iframe src=\"http://snippets.wdfiles.com/local--code/code:iframe-redirect#".length());
//                    html = html.substring(0, html.indexOf("\""));
//                    final String randomURL = html;
//                    Timber.d("randomUrl = %s", randomURL);

                    Timber.d("html: %s", html);
                    Document doc = Jsoup.parse(html);
                    Elements articlesTags = doc.getElementById("taglist").children();
                    List<Article> articles = new ArrayList<>();
                    for (Element element : articlesTags) {
                        Article article = new Article();
                        Element aTag = element.getElementsByTag("a").first();
                        article.url = aTag.attr("href");
                        article.title = aTag.text();
                        aTag.remove();
                        String authorAndRating = element.text();
                        String author = authorAndRating.replace(" Created by: ", "");
                        author = author.substring(0, author.indexOf(" Rating: "));
                        Timber.d("authorAndRating: %s", authorAndRating);
                        String ratingString = authorAndRating.substring(authorAndRating.indexOf(" Rating: "));
                        int rating = Integer.parseInt(ratingString);

                        article.authorName = author;
                        article.rating = rating;
                        articles.add(article);
                    }
                    subscriber.onSuccess(articles);
                } else {
                    subscriber.onError(new ScpParseException(MyApplicationImpl.getAppInstance().getString(R.string.error_parse)));
                }
            } catch (final IOException e) {
                Timber.e(e);
                subscriber.onError(e);
            }
        });

//        return mEnScpSiteApi.getArticlesByTags(ArticleTag.getCommaSeparatedStringFromTags(tags), EN_SITE_TAG_SORT)
//                .map(html -> {
//                    Document doc = Jsoup.parse(html);
//                    Elements articlesTags = doc.getElementById("taglist").children();
//                    List<Article> articles = new ArrayList<>();
//                    for (Element element : articlesTags) {
//                        Article article = new Article();
//                        Element aTag = element.getElementsByTag("a").first();
//                        article.url = aTag.attr("href");
//                        article.title = aTag.text();
//                        aTag.remove();
//                        String authorAndRating = element.text();
//                        String author = authorAndRating.replace(" Created by: ", "");
//                        author = author.substring(0, author.indexOf(" Rating: "));
//                        String ratingString = authorAndRating.substring(authorAndRating.indexOf(" Rating: "));
//                        int rating = Integer.parseInt(ratingString);
//
//                        article.authorName = author;
//                        article.rating = rating;
//                        articles.add(article);
//                    }
//                    return articles;
//                });
    }

    @Override
    public Single<List<ArticleTag>> getTagsFromSite() {
        return Single.create(subscriber -> {
            final Request request = new Request.Builder()
                    .url("http://home.helenbot.com/tags.php")
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
                final Element selectWithTags = doc.getElementById("taglist");
                final Elements options = selectWithTags.children();
                final List<ArticleTag> articleTags = new ArrayList<>();
                for (final Element option : options) {
                    articleTags.add(new ArticleTag(option.text()));
                }

                subscriber.onSuccess(articleTags);
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