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
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scp.downloads.ScpParseException;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.BuildConfig;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
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
            OkHttpClient okHttpClient,
            Retrofit vpsRetrofit,
            Retrofit scpRetrofit,
            MyPreferenceManager preferencesManager,
            Gson gson,
            ConstantValues constantValues
    ) {
        super(okHttpClient, vpsRetrofit, scpRetrofit, preferencesManager, gson, constantValues);
    }

    public Observable<String> getRandomUrl() {
        Timber.d("getRandomUrl");
        return bindWithUtils(Observable.unsafeCreate(subscriber -> {
            Request.Builder request = new Request.Builder();
            request.url(mConstantValues.getRandomPageUrl());
            request.get();

            try {
                OkHttpClient client = new OkHttpClient.Builder()
                        .followRedirects(false)
                        .addInterceptor(new HttpLoggingInterceptor(message -> Timber.d(message)).setLevel(BuildConfig.FLAVOR.equals("dev")
                                ? HttpLoggingInterceptor.Level.BODY
                                : HttpLoggingInterceptor.Level.NONE))
                        .build();
                Response response = client.newCall(request.build()).execute();

                ResponseBody requestResult = response.body();
                if (requestResult != null) {
                    String html = requestResult.string();
                    String patternToFindUrl = "<iframe src=\"http://snippets.wdfiles.com/local--code/code:iframe-redirect#";
                    html = html.substring(html.indexOf(patternToFindUrl) + patternToFindUrl.length());
                    html = html.substring(0, html.indexOf("\""));
                    String randomURL = html;
                    Timber.d("randomUrl = " + randomURL);
                    subscriber.onNext(randomURL);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new ScpParseException(MyApplicationImpl.getAppInstance().getString(R.string.error_parse)));
                }
            } catch (IOException e) {
                Timber.e(e);
                subscriber.onError(e);
            }
        }));
    }

    @Override
    public Observable<Integer> getRecentArticlesPageCountObservable() {
        return bindWithUtils(Observable.<Integer>unsafeCreate(subscriber -> {
            Request request = new Request.Builder()
                    .url(mConstantValues.getNewArticles() + "/p/1")
                    .build();

            String responseBody = null;
            try {
                Response response = mOkHttpClient.newCall(request).execute();
                ResponseBody body = response.body();
                if (body != null) {
                    responseBody = body.string();
                } else {
                    subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(ru.kuchanov.scpcore.R.string.error_parse)));
                    return;
                }
            } catch (IOException e) {
                subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(R.string.error_connection)));
                return;
            }
            try {
                Document doc = Jsoup.parse(responseBody);

                //get num of pages
                Element spanWithNumber = doc.getElementsByClass("pager-no").first();
                String text = spanWithNumber.text();
                Integer numOfPages = Integer.valueOf(text.substring(text.lastIndexOf(" ") + 1));

                subscriber.onNext(numOfPages);
                subscriber.onCompleted();
            } catch (Exception e) {
                Timber.e(e, "error while get arts list");
                subscriber.onError(e);
            }
        }));
    }

    @Override
    protected List<Article> parseForRecentArticles(Document doc) throws ScpParseException {
        Element contentTypeDescription = doc.getElementsByClass("content-type-description").first();
        Element pageContent = contentTypeDescription.getElementsByTag("table").first();
        if (pageContent == null) {
            throw new ScpParseException(MyApplicationImpl.getAppInstance().getString(R.string.error_parse));
        }

        List<Article> articles = new ArrayList<>();
        Elements listOfElements = pageContent.getElementsByTag("tr");
        for (int i = 1/*start from 1 as first row is tables header*/; i < listOfElements.size(); i++) {
            Elements listOfTd = listOfElements.get(i).getElementsByTag("td");
            Element firstTd = listOfTd.first();
            Element tagA = firstTd.getElementsByTag("a").first();

            String title = tagA.text();
            String url = mConstantValues.getBaseApiUrl() + tagA.attr("href");
            //4 Jun 2017, 22:25
            //createdDate
            Element createdDateNode = listOfTd.get(1);
            String createdDate = createdDateNode.text().trim();

            Article article = new Article();
            article.title = title;
            article.url = url.trim();
            article.createdDate = createdDate;
            articles.add(article);
        }

        return articles;
    }

    @Override
    protected List<Article> parseForRatedArticles(Document doc) throws ScpParseException {
        Element pageContent = doc.getElementById("page-content");
        if (pageContent == null) {
            throw new ScpParseException(MyApplicationImpl.getAppInstance().getString(R.string.error_parse));
        }
        Element listPagesBox = pageContent.getElementsByClass("list-pages-box").first();
        if (listPagesBox == null) {
            throw new ScpParseException(MyApplicationImpl.getAppInstance().getString(R.string.error_parse));
        }

        String allArticles = listPagesBox.getElementsByTag("p").first().html();
        String[] arrayOfArticles = allArticles.split("<br>");
        List<Article> articles = new ArrayList<>();
        for (String arrayItem : arrayOfArticles) {
            doc = Jsoup.parse(arrayItem);
            Element aTag = doc.getElementsByTag("a").first();
            String url = mConstantValues.getBaseApiUrl() + aTag.attr("href");
            String title = aTag.text();

            String rating = arrayItem.substring(arrayItem.indexOf("Voto: ") + "Voto: ".length());
            rating = rating.substring(0, rating.indexOf(", "));

            Article article = new Article();
            article.url = url;
            article.rating = Integer.parseInt(rating);
            article.title = title;
            articles.add(article);
        }

        return articles;
    }

    @Override
    protected List<Article> parseForObjectArticles(Document doc) throws ScpParseException {
        Element pageContent = doc.getElementById("page-content");
        if (pageContent == null) {
            throw new ScpParseException(MyApplicationImpl.getAppInstance().getString(R.string.error_parse));
        }
        Elements listPagesBox = pageContent.getElementsByTag("h1");
        listPagesBox.remove();
        Element collapsibleBlock = pageContent.getElementsByTag("ul").first();
        collapsibleBlock.remove();
//        Element table = pageContent.getElementsByClass("content-toc").first();
//        table.remove();
        Elements allUls = pageContent.getElementsByClass("content-panel").first().getElementsByTag("ul");

        List<Article> articles = new ArrayList<>();

        for (Element ul : allUls) {
            Elements allLi = ul.children();
            for (Element li : allLi) {
                //do not add empty articles
                if (li.getElementsByTag("a").first().hasClass("newpage")) {
                    continue;
                }
                Article article = new Article();
                article.url = mConstantValues.getBaseApiUrl() + li.getElementsByTag("a").first().attr("href");
                article.title = li.text();
                articles.add(article);
            }
        }

        return articles;
    }

    @Override
    public Observable<List<ArticleTag>> getTagsFromSite() {
        return bindWithUtils(Observable.<List<ArticleTag>>unsafeCreate(subscriber -> {
            Request request = new Request.Builder()
                    .url(mConstantValues.getBaseApiUrl() + "/system:page-tags/")
                    .build();

            String responseBody = null;
            try {
                Response response = mOkHttpClient.newCall(request).execute();
                ResponseBody body = response.body();
                if (body != null) {
                    responseBody = body.string();
                } else {
                    subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(R.string.error_parse)));
                    return;
                }
            } catch (IOException e) {
                subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(R.string.error_connection)));
                return;
            }
            try {
                Document doc = Jsoup.parse(responseBody);
                Element pageContent = doc.getElementById("page-content");
                if (pageContent == null) {
                    subscriber.onError(new ScpParseException(BaseApplication.getAppInstance().getString(R.string.error_parse)));
                    return;
                }

                List<ArticleTag> tags = new ArrayList<>();

                Element allTags = doc.getElementsByClass("pages-tag-cloud-box").first();
                for (Element tagNode : allTags.getElementsByClass("tag")) {
                    ArticleTag tag = new ArticleTag();
                    tag.title = tagNode.text();
                    tags.add(tag);
                }
                //parse end
                subscriber.onNext(tags);
                subscriber.onCompleted();
            } catch (Exception e) {
                Timber.e(e, "error while get arts list");
                subscriber.onError(e);
            }
        }));
    }

    @Override
    public Observable<List<Article>> getArticlesByTags(List<ArticleTag> tags) {
//        Timber.d("getArticlesByTags: %s", tags);
//        String tagName = tags.get(0).title;
//        Timber.d("tagName: %s", tagName);
        List<String> tagsTitles = ArticleTag.getStringsFromTags(tags);
        //fix index of bounds error
        if (tagsTitles.isEmpty()) {
            return Observable.just(Collections.emptyList());
        }
//        Timber.d("tagsTitles: %s", tagsTitles);
        String tagTitle = tagsTitles.get(0);
//        Timber.d("tagTitle: %s", tagTitle);
        return bindWithUtils(Observable.<List<Article>>unsafeCreate(subscriber -> {
            Request request = new Request.Builder()
                    .url(mConstantValues.getBaseApiUrl() + "/system:page-tags/tag/" + tagTitle)
                    .build();

            String responseBody = null;
            try {
                Response response = mOkHttpClient.newCall(request).execute();
                ResponseBody body = response.body();
                if (body != null) {
                    responseBody = body.string();
                } else {
                    subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(R.string.error_parse)));
                    return;
                }
            } catch (IOException e) {
                subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(R.string.error_connection)));
                return;
            }
            try {
                Document doc = Jsoup.parse(responseBody);
                Element pageContent = doc.getElementById("page-content");
                if (pageContent == null) {
                    subscriber.onError(new ScpParseException(BaseApplication.getAppInstance().getString(R.string.error_parse)));
                    return;
                }

                List<Article> articles = new ArrayList<>();

                Element allTags = doc.getElementById("tagged-pages-list");
                for (Element tagNode : allTags.getElementsByTag("a")) {
                    Article tag = new Article();
                    tag.title = tagNode.text();
                    tag.url = mConstantValues.getBaseApiUrl() + tagNode.attr("href");
                    articles.add(tag);
                }
                //parse end
                subscriber.onNext(articles);
                subscriber.onCompleted();
            } catch (Exception e) {
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