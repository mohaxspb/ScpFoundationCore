package ru.dante.scpfoundation.api;

import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
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
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.downloads.ScpParseException;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import rx.Observable;
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
            final MyPreferenceManager preferencesManager,
            final Gson gson,
            final ConstantValues constantValues
    ) {
        super(okHttpClient, vpsRetrofit, scpRetrofit, scpReaderRetrofit, preferencesManager, gson, constantValues);
    }

    @Override
    public Observable<String> getRandomUrl() {
        return bindWithUtils(Observable.unsafeCreate(subscriber -> {
            final Request.Builder request = new Request.Builder();
            request.url(mConstantValues.getRandomPageUrl());
            request.get();

            try {
                final OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(new HttpLoggingInterceptor(
                                message -> Timber.d(message)).setLevel(BuildConfig.FLAVOR.equals("dev")
                                                                       ? HttpLoggingInterceptor.Level.BODY
                                                                       : HttpLoggingInterceptor.Level.NONE)
                        )
                        .build();
                final Response response = client.newCall(request.build()).execute();

                final ResponseBody requestResult = response.body();
                if (requestResult != null) {
                    final String html = requestResult.string();
                    final Document doc = Jsoup.parse(html);

                    final Element aTag = doc.getElementById("page-content")
                            .getElementsByTag("iframe").first();
                    final String randomURL = aTag.attr("src").replace("http://snippets.wdfiles.com/local--code/code:iframe-redirect#", "");
                    Timber.d("randomURL = %s", randomURL);
                    subscriber.onNext(randomURL);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new ScpParseException(MyApplicationImpl.getAppInstance().getString(R.string.error_parse)));
                }
            } catch (final IOException e) {
                Timber.e(e);
                subscriber.onError(e);
            }
        }));
    }

    @Override
    public Observable<Integer> getRecentArticlesPageCountObservable() {
        return bindWithUtils(Observable.<Integer>unsafeCreate(subscriber -> {
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
        }));
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
    protected List<Article> parseForRatedArticles(final Document doc) throws ScpParseException {
        final Element pageContent = doc.getElementById("page-content");
        if (pageContent == null) {
            throw new ScpParseException(MyApplicationImpl.getAppInstance().getString(R.string.error_parse));
        }
        final Element listPagesBox = pageContent.getElementsByClass("panel-body").last();
        if (listPagesBox == null) {
            throw new ScpParseException(MyApplicationImpl.getAppInstance().getString(R.string.error_parse));
        }

        final Elements articlesDivs = listPagesBox.getElementsByClass("list-pages-item");
        final List<Article> articles = new ArrayList<>();
        for (final Element element : articlesDivs) {
            final Element aTag = element.getElementsByTag("a").first();
            final String url = mConstantValues.getBaseApiUrl() + aTag.attr("href");
            final String title = aTag.text();

            final Element pTag = element.getElementsByTag("p").first();
            String ratingString = pTag.text().substring(pTag.text().indexOf("avaliação ") + "avaliação ".length());
            ratingString = ratingString.substring(0, ratingString.indexOf("."));
            final int rating = Integer.parseInt(ratingString);

            final Article article = new Article();
            article.url = url;
            article.rating = rating;
            article.title = title;
            articles.add(article);
        }

        return articles;
    }

    @Override
    protected List<Article> parseForObjectArticles(Document doc) throws ScpParseException {
        Element pageContent = doc.getElementById("page-content");
        if (pageContent == null) {
            throw new ScpParseException(BaseApplication.getAppInstance().getString(ru.kuchanov.scpcore.R.string.error_parse));
        }

        //parse
        pageContent = doc.getElementsByClass("content-panel standalone series").first();

        final Element listPagesBox = pageContent.getElementsByClass("list-pages-box").first();
        if (listPagesBox != null) {
            listPagesBox.remove();
        }
        final Element collapsibleBlock = pageContent.getElementsByClass("collapsible-block").first();
        if (collapsibleBlock != null) {
            collapsibleBlock.remove();
        }
        final Element table = pageContent.getElementsByTag("table").first();
        if (table != null) {
            table.remove();
        }
        final Element h2 = doc.getElementById("toc0");
        if (h2 != null) {
            h2.remove();
        }
        final Elements aWithNameAttr2 = doc.getElementsByTag("a");
        if (aWithNameAttr2 != null) {
            for (final Element element : aWithNameAttr2) {
                if (element.hasAttr("name")) {
                    element.remove();
                }
            }
        }

        //now we will remove all html code before tag h2,with id toc1
        String allHtml = pageContent.html();
        int indexOfh2WithIdToc1 = allHtml.indexOf("<h1 id=\"toc2\">");
        if (indexOfh2WithIdToc1 == -1) {
            indexOfh2WithIdToc1 = allHtml.indexOf("<h1 id=\"toc3\">");
        }
        int indexOfhr = allHtml.indexOf("<hr>");
        //for other objects filials there is no HR tag at the end...

        if (indexOfhr < indexOfh2WithIdToc1) {
            indexOfhr = allHtml.indexOf("<p style=\"text-align: center;\">= = = =</p>");
        }
        if (indexOfhr < indexOfh2WithIdToc1) {
            indexOfhr = allHtml.length();
        }
        allHtml = allHtml.substring(indexOfh2WithIdToc1, indexOfhr);

        doc = Jsoup.parse(allHtml);

        final Elements h2withIdToc1 = doc.getElementsByTag("h1");
        if (h2withIdToc1 != null) {
            h2withIdToc1.remove();
        }

        final Elements pTags = doc.getElementsByTag("p");
        if (pTags != null) {
            pTags.remove();
        }

        final Elements allh2Tags = doc.getElementsByTag("h2");
        for (final Element h2Tag : allh2Tags) {
            final Element brTag = new Element(Tag.valueOf("br"), "");
            h2Tag.replaceWith(brTag);
        }

        final String allArticles = doc.getElementsByTag("body").first().html();
        final String[] arrayOfArticles = allArticles.split("<br>");
        final List<Article> articles = new ArrayList<>();
        for (final String arrayItem : arrayOfArticles) {
            Timber.d("arrayItem: %s", arrayItem);
            doc = Jsoup.parse(arrayItem);
            //type of object
//            final String imageURL = doc.getElementsByTag("img").first().attr("src");
            @Article.ObjectType final String type = Article.ObjectType.NONE;

            Timber.d("url: %s", doc.getElementsByTag("a").first().attr("href"));
            String url = doc.getElementsByTag("a").first().attr("href");
            if(!url.startsWith(mConstantValues.getBaseApiUrl())){
                url = mConstantValues.getBaseApiUrl() + url;
            }
            final String title = doc.text();

            final Article article = new Article();

            article.url = url;
            article.title = title;
            article.type = type;
            articles.add(article);
        }

        return articles;
    }

    @Override
    public Observable<List<ArticleTag>> getTagsFromSite() {
        return bindWithUtils(Observable.<List<ArticleTag>>unsafeCreate(subscriber -> {
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
                subscriber.onNext(tags);
                subscriber.onCompleted();
            } catch (final Exception e) {
                Timber.e(e, "error while get arts list");
                subscriber.onError(e);
            }
        }));
    }

    @Override
    public Observable<List<Article>> getArticlesByTags(final List<ArticleTag> tags) {
        final List<String> tagsTitles = ArticleTag.getStringsFromTags(tags);
        //fix index of bounds error
        if (tagsTitles.isEmpty()) {
            return Observable.just(Collections.emptyList());
        }
        final String tagTitle = tagsTitles.get(0);
        return bindWithUtils(Observable.<List<Article>>unsafeCreate(subscriber -> {
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
                subscriber.onNext(articles);
                subscriber.onCompleted();
            } catch (final Exception e) {
                Timber.e(e, "error while get arts list");
                subscriber.onError(e);
            }
        }));
    }

    @Override
    protected String getScpServerWiki() {
        return "scp-wiki";
    }
}