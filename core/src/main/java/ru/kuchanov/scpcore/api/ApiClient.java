package ru.kuchanov.scpcore.api;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.Profile;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.realm.RealmList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import ru.kuchanov.scp.downloads.ApiClientModel;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scp.downloads.ScpParseException;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.BuildConfig;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.error.ScpException;
import ru.kuchanov.scpcore.api.error.ScpNoSearchResultsException;
import ru.kuchanov.scpcore.api.model.ArticleFromSearchTagsOnSite;
import ru.kuchanov.scpcore.api.model.firebase.ArticleInFirebase;
import ru.kuchanov.scpcore.api.model.firebase.FirebaseObjectUser;
import ru.kuchanov.scpcore.api.model.response.LeaderBoardResponse;
import ru.kuchanov.scpcore.api.model.response.PurchaseValidateResponse;
import ru.kuchanov.scpcore.api.model.response.VkGalleryResponse;
import ru.kuchanov.scpcore.api.model.response.VkGroupJoinResponse;
import ru.kuchanov.scpcore.api.service.ScpServer;
import ru.kuchanov.scpcore.api.service.VpsServer;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.db.model.RealmString;
import ru.kuchanov.scpcore.db.model.SocialProviderModel;
import ru.kuchanov.scpcore.db.model.User;
import ru.kuchanov.scpcore.db.model.VkImage;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.model.PlayMarketApplication;
import ru.kuchanov.scpcore.monetization.model.VkGroupToJoin;
import ru.kuchanov.scpcore.util.DimensionUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by y.kuchanov on 22.12.16.
 * <p>
 * for scp_ru
 */
public class ApiClient implements ApiClientModel<Article> {

    @SuppressWarnings("unused")
    protected MyPreferenceManager mPreferencesManager;
    protected OkHttpClient mOkHttpClient;
    protected Gson mGson;

    private VpsServer mVpsServer;
    private ScpServer mScpServer;

    protected ConstantValues mConstantValues;

    public ApiClient(
            OkHttpClient okHttpClient,
            Retrofit vpsRetrofit,
            Retrofit scpRetrofit,
            MyPreferenceManager preferencesManager,
            Gson gson,
            ConstantValues constantValues
    ) {
        mPreferencesManager = preferencesManager;
        mOkHttpClient = okHttpClient;
        mGson = gson;
        mVpsServer = vpsRetrofit.create(VpsServer.class);
        mScpServer = scpRetrofit.create(ScpServer.class);
        mConstantValues = constantValues;
    }

    protected <T> Observable<T> bindWithUtils(Observable<T> observable) {
        return observable
//                .doOnError(throwable -> {
//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                })
//                .delay(2, TimeUnit.SECONDS)
                ;
    }

    public Observable<String> getRandomUrl() {
        return bindWithUtils(Observable.unsafeCreate(subscriber -> {
            Request.Builder request = new Request.Builder();
            request.url(mConstantValues.getRandomPageUrl());
            request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            request.addHeader("Accept-Encoding", "gzip, deflate, br");
            request.addHeader("Accept-Language", "en-US,en;q=0.8,de-DE;q=0.5,de;q=0.3");
            request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
            request.get();

            try {
                Response response = mOkHttpClient.newCall(request.build()).execute();

                Request requestResult = response.request();
                Timber.d("requestResult:" + requestResult);
                Timber.d("requestResult.url().url():" + requestResult.url().url());

                String randomURL = requestResult.url().url().toString();
                Timber.d("randomUrl = " + randomURL);

                subscriber.onNext(randomURL);
                subscriber.onCompleted();
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
                    subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(R.string.error_parse)));
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

    public Observable<List<Article>> getRecentArticlesForOffset(int offset) {
        int page = offset / mConstantValues.getNumOfArticlesOnRecentPage() + 1/*as pages are not zero based*/;
        return getRecentArticlesForPage(page);
    }

    public Observable<List<Article>> getRecentArticlesForPage(int page) {
        return bindWithUtils(Observable.<List<Article>>unsafeCreate(subscriber -> {
            Request request = new Request.Builder()
                    .url(mConstantValues.getNewArticles() + "/p/" + page)
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

                List<Article> articles = parseForRecentArticles(doc);

                subscriber.onNext(articles);
                subscriber.onCompleted();
            } catch (Exception | ScpParseException e) {
                Timber.e(e, "error while get arts list");
                subscriber.onError(e);
            }
        }));
    }

    protected List<Article> parseForRecentArticles(Document doc) throws ScpParseException {
        Element pageContent = doc.getElementsByClass("wiki-content-table").first();
        if (pageContent == null) {
            throw new ScpParseException(BaseApplication.getAppInstance().getString(R.string.error_parse));
        }

        List<Article> articles = new ArrayList<>();
        Elements listOfElements = pageContent.getElementsByTag("tr");
        for (int i = 1/*start from 1 as first row is tables geader*/; i < listOfElements.size(); i++) {
            Element tableRow = listOfElements.get(i);
            Elements listOfTd = tableRow.getElementsByTag("td");
            //title and url
            Element firstTd = listOfTd.first();
            Element tagA = firstTd.getElementsByTag("a").first();
            String title = tagA.text();
            String url = mConstantValues.getBaseApiUrl() + tagA.attr("href");
            //rating
            Element ratingNode = listOfTd.get(1);
            int rating = Integer.parseInt(ratingNode.text());
            //author
            Element spanWithAuthor = listOfTd.get(2)
                    .getElementsByAttributeValueContaining("class", "printuser").first();
            String authorName = spanWithAuthor.text();
            Element authorUrlNode = spanWithAuthor.getElementsByTag("a").first();
            String authorUrl = authorUrlNode != null ? authorUrlNode.attr("href") : null;

            //createdDate
            Element createdDateNode = listOfTd.get(3);
            String createdDate = createdDateNode.text().trim();
            //updatedDate
            Element updatedDateNode = listOfTd.get(4);
            String updatedDate = updatedDateNode.text().trim();

            Article article = new Article();
            article.title = title;
            article.url = url.trim();
            article.rating = rating;
            article.authorName = authorName;
            article.authorUrl = authorUrl;
            article.createdDate = createdDate;
            article.updatedDate = updatedDate;
            articles.add(article);
        }

        return articles;
    }

    public Observable<List<Article>> getRatedArticles(int offset) {
        return bindWithUtils(Observable.<List<Article>>unsafeCreate(subscriber -> {
            int page = offset / mConstantValues.getNumOfArticlesOnRatedPage() + 1/*as pages are not zero based*/;

            Request request = new Request.Builder()
                    .url(mConstantValues.getMostRated() + "/p/" + page)
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

                List<Article> articles = parseForRatedArticles(doc);

                subscriber.onNext(articles);
                subscriber.onCompleted();
            } catch (Exception | ScpParseException e) {
                Timber.e(e, "error while get arts list");
                subscriber.onError(e);
            }
        }));
    }

    protected List<Article> parseForRatedArticles(Document doc) throws ScpParseException {
        Element pageContent = doc.getElementById("page-content");
        if (pageContent == null) {
            throw new ScpParseException(BaseApplication.getAppInstance().getString(R.string.error_parse));
        }
        Element listPagesBox = pageContent.getElementsByClass("list-pages-box").first();
        if (listPagesBox == null) {
            throw new ScpParseException(BaseApplication.getAppInstance().getString(R.string.error_parse));
        }

        List<Article> articles = new ArrayList<>();
        List<Element> listOfElements = listPagesBox.getElementsByClass("list-pages-item");
        for (Element element : listOfElements) {
//                    Timber.d("element: %s", element);
            Element tagP = element.getElementsByTag("p").first();
            Element tagA = tagP.getElementsByTag("a").first();
            String title = tagP.text().substring(0, tagP.text().indexOf(", рейтинг"));
            String url = mConstantValues.getBaseApiUrl() + tagA.attr("href");
            //remove a tag to leave only text with rating
            tagA.remove();
            tagP.text(tagP.text().replace(", рейтинг ", ""));
            tagP.text(tagP.text().substring(0, tagP.text().length() - 1));
            int rating = Integer.parseInt(tagP.text());

            Article article = new Article();
            article.title = title;
            article.url = url;
            article.rating = rating;
            articles.add(article);
        }

        return articles;
    }

    public Observable<List<Article>> getSearchArticles(int offset, String searchQuery) {
        return bindWithUtils(Observable.<List<Article>>unsafeCreate(subscriber -> {
            int page = offset / mConstantValues.getNumOfArticlesOnSearchPage() + 1/*as pages are not zero based*/;

            Request request = new Request.Builder()
                    .url(mConstantValues.getBaseApiUrl() + String.format(Locale.ENGLISH, mConstantValues.getSearchSiteUrl(), searchQuery, page))
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

                Element searchResults = pageContent.getElementsByClass("search-results").first();
                Elements items = searchResults.children();
                if (items.size() == 0) {
                    subscriber.onError(new ScpNoSearchResultsException(
                            BaseApplication.getAppInstance().getString(R.string.error_no_search_results)));
                } else {
                    for (Element item : items) {
                        Element titleA = item.getElementsByClass("title").first().getElementsByTag("a").first();
                        String title = titleA.html();
                        String url = titleA.attr("href");
                        Element previewDiv = item.getElementsByClass("preview").first();
                        String preview = previewDiv.html();

                        Article article = new Article();

                        article.title = title;
                        article.url = url;
                        article.preview = preview;

                        articles.add(article);
                    }
                    subscriber.onNext(articles);
                    subscriber.onCompleted();
                }
            } catch (Exception e) {
                Timber.e(e, "error while get arts list");
                subscriber.onError(e);
            }
        }));
    }

    public Observable<List<Article>> getObjectsArticles(String sObjectsLink) {
        return bindWithUtils(Observable.<List<Article>>unsafeCreate(subscriber -> {
            Request request = new Request.Builder()
                    .url(sObjectsLink)
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

                List<Article> articles = parseForObjectArticles(doc);

                subscriber.onNext(articles);
                subscriber.onCompleted();
            } catch (Exception | ScpParseException e) {
                Timber.e(e, "error while get arts list");
                subscriber.onError(e);
            }
        }));
    }

    protected List<Article> parseForObjectArticles(Document doc) throws ScpParseException {
        Element pageContent = doc.getElementById("page-content");
        if (pageContent == null) {
            throw new ScpParseException(BaseApplication.getAppInstance().getString(R.string.error_parse));
        }

        List<Article> articles = new ArrayList<>();
        //parse
        Element listPagesBox = pageContent.getElementsByClass("list-pages-box").first();
        listPagesBox.remove();
        Element collapsibleBlock = pageContent.getElementsByClass("collapsible-block").first();
        collapsibleBlock.remove();
        Element table = pageContent.getElementsByTag("table").first();
        table.remove();
        Element h2 = doc.getElementById("toc0");
        h2.remove();

        //now we will remove all html code before tag h2,with id toc1
        String allHtml = pageContent.html();
        int indexOfh2WithIdToc1 = allHtml.indexOf("<h2 id=\"toc1\">");
        int indexOfhr = allHtml.indexOf("<hr>");
        allHtml = allHtml.substring(indexOfh2WithIdToc1, indexOfhr);

        doc = Jsoup.parse(allHtml);

        Element h2withIdToc1 = doc.getElementById("toc1");
        h2withIdToc1.remove();

        Elements allh2Tags = doc.getElementsByTag("h2");
        for (Element h2Tag : allh2Tags) {
            Element brTag = new Element(Tag.valueOf("br"), "");
            h2Tag.replaceWith(brTag);
        }

        String allArticles = doc.getElementsByTag("body").first().html();
        String[] arrayOfArticles = allArticles.split("<br>");
        for (String arrayItem : arrayOfArticles) {
            doc = Jsoup.parse(arrayItem);
            //type of object
            String imageURL = doc.getElementsByTag("img").first().attr("src");
            @Article.ObjectType
            String type = getObjectTypeByImageUrl(imageURL);

            String url = mConstantValues.getBaseApiUrl() + doc.getElementsByTag("a").first().attr("href");
            String title = doc.text();

            Article article = new Article();

            article.url = url;
            article.title = title;
            article.type = type;
            articles.add(article);
        }

        return articles;
    }

    @Nullable
    public Article getArticleFromApi(String url) throws Exception, ScpParseException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        String responseBody;
        try {
            Response response = mOkHttpClient.newCall(request).execute();
            ResponseBody body = response.body();
            if (body != null) {
                responseBody = body.string();
            } else {
                throw new IOException(BaseApplication.getAppInstance().getString(R.string.error_parse));
            }
        } catch (IOException e) {
            throw new IOException(BaseApplication.getAppInstance().getString(R.string.error_connection));
        }

        try {
            Document doc = Jsoup.parse(responseBody);
            Element pageContent = getArticlePageContentTag(doc);
            if (pageContent == null) {
                throw new ScpParseException(BaseApplication.getAppInstance().getString(R.string.error_parse));
            }
            Element p404 = pageContent.getElementById("404-message");
            if (p404 != null) {
                Article article = new Article();
                article.url = url;
                article.text = p404.outerHtml();
                article.title = "404";

                return article;
            }
            //замена ссылок в сносках
            Elements footnoterefs = pageContent.getElementsByClass("footnoteref");
            for (Element snoska : footnoterefs) {
                Element aTag = snoska.getElementsByTag("a").first();
                String digits = "";
                for (char c : aTag.id().toCharArray()) {
                    if (TextUtils.isDigitsOnly(String.valueOf(c))) {
                        digits += String.valueOf(c);
                    }
                }
                aTag.attr("href", "scp://" + digits);
            }
            Elements footnoterefsFooter = pageContent.getElementsByClass("footnote-footer");
            for (Element snoska : footnoterefsFooter) {
                Element aTag = snoska.getElementsByTag("a").first();
                snoska.prependText(aTag.text());
                aTag.remove();
//                    aTag.replaceWith(new Element(Tag.valueOf("pizda"), aTag.text()));
            }

            //замена ссылок в библиографии
            Elements bibliographi = pageContent.getElementsByClass("bibcite");
            for (Element snoska : bibliographi) {
                Element aTag = snoska.getElementsByTag("a").first();
                String onclickAttr = aTag.attr("onclick");

                String id = onclickAttr.substring(onclickAttr.indexOf("bibitem-"), onclickAttr.lastIndexOf("'"));
                aTag.attr("href", id);
            }
            //remove rating bar
            int rating = 0;
            Element rateDiv = pageContent.getElementsByClass("page-rate-widget-box").first();
            if (rateDiv != null) {
                Element spanWithRating = rateDiv.getElementsByClass("rate-points").first();
                if (spanWithRating != null) {
                    Element ratingSpan = spanWithRating.getElementsByClass("number").first();
//                    Timber.d("ratingSpan: %s", ratingSpan);
                    if (ratingSpan != null) {
                        try {
                            rating = Integer.parseInt(ratingSpan.text().substring(1, ratingSpan.text().length()));
//                            Timber.d("rating: %s", rating);
                        } catch (Exception e) {
                            Timber.e(e);
                        }
                    }
                }

                Element span1 = rateDiv.getElementsByClass("rateup").first();
                span1.remove();
                Element span2 = rateDiv.getElementsByClass("ratedown").first();
                span2.remove();
                Element span3 = rateDiv.getElementsByClass("cancel").first();
                span3.remove();

                Elements heritageDiv = rateDiv.parent().getElementsByClass("heritage-emblem");
                if (heritageDiv != null && !heritageDiv.isEmpty()) {
                    heritageDiv.first().remove();
                }
            }
            //remove something more
            Element svernut = pageContent.getElementById("toc-action-bar");
            if (svernut != null) {
                svernut.remove();
            }
            Elements script = pageContent.getElementsByTag("script");
            for (Element element : script) {
                element.remove();
            }
            //replace all spans with strike-through with <s>
            Elements spansWithStrike = pageContent.select("span[style=text-decoration: line-through;]");
            for (Element element : spansWithStrike) {
//                    Timber.d("element: %s", element);
                element.tagName("s");
                for (Attribute attribute : element.attributes()) {
                    element.removeAttr(attribute.getKey());
                }
//                    Timber.d("element refactored: %s", element);
            }
            //get title
            Element titleEl = doc.getElementById("page-title");
            String title = "";
            if (titleEl != null) {
                title = titleEl.text();
            }
            Element upperDivWithLink = doc.getElementById("breadcrumbs");
            if (upperDivWithLink != null) {
                pageContent.prependChild(upperDivWithLink);
            }
            ParseHtmlUtils.parseImgsTags(pageContent);

            //put all text which is not in any tag in div tag
            for (Element element : pageContent.children()) {
                Node nextSibling = element.nextSibling();
//                    Timber.d("child: ___%s___", nextSibling);
//                    Timber.d("nextSibling.nodeName(): %s", nextSibling.nodeName());
                if (nextSibling != null && !nextSibling.toString().equals(" ") && nextSibling.nodeName().equals("#text")) {
                    element.after(new Element("div").appendChild(nextSibling));
                }

                //also fix scp-3000, where image and spoiler are in div tag, fucking shit! Web monkeys, ARGH!!!
                if (!element.children().isEmpty() && element.children().size() == 2
                        && element.child(0).tagName().equals("img") && element.child(1).className().equals("collapsible-block")) {
                    element.before(element.childNode(0));
                    element.after(element.childNode(1));
                    element.remove();
                }
            }

            //replace styles with underline and strike
            Elements spans = pageContent.getElementsByTag("span");
            for (Element element : spans) {
                //<span style="text-decoration: underline;">PLEASE</span>
                if (element.hasAttr("style") && element.attr("style").equals("text-decoration: underline;")) {
//                    Timber.d("fix underline span: %s", element.outerHtml());
                    Element uTag = new Element(Tag.valueOf("u"), "").text(element.text());
                    element.replaceWith(uTag);
//                    Timber.d("fixED underline span: %s", uTag.outerHtml());
                }
                //<span style="text-decoration: line-through;">условия содержания.</span>
                if (element.hasAttr("style") && element.attr("style").equals("text-decoration: line-through;")) {
//                    Timber.d("fix strike span");
                    Element sTag = new Element(Tag.valueOf("s"), "");
                    element.replaceWith(sTag);
                }
            }

            //search for relative urls to add domain
            for (Element a : pageContent.getElementsByTag("a")) {
                //replace all links to not translated articles
                if (a.className().equals("newpage")) {
                    a.attr("href", Constants.Api.NOT_TRANSLATED_ARTICLE_UTIL_URL
                            + Constants.Api.NOT_TRANSLATED_ARTICLE_URL_DELIMITER
                            + a.attr("href")
                    );
                } else if (a.attr("href").startsWith("/")) {
                    a.attr("href", mConstantValues.getBaseApiUrl() + a.attr("href"));
                }
            }

            //extract tags
            RealmList<ArticleTag> articleTags = new RealmList<>();
            Element tagsContainer = doc.getElementsByClass("page-tags").first();
//                Timber.d("tagsContainer: %s", tagsContainer);
            if (tagsContainer != null) {
                for (Element a : tagsContainer./*getElementsByTag("span").first().*/getElementsByTag("a")) {
                    articleTags.add(new ArticleTag(a.text()));
//                        Timber.d("tag: %s", articleTags.get(articleTags.size() - 1));
                }
            }

            //search for images and add it to separate field to be able to show it in arts lists
            RealmList<RealmString> imgsUrls = null;
            Elements imgsOfArticle = pageContent.getElementsByTag("img");
            if (!imgsOfArticle.isEmpty()) {
                imgsUrls = new RealmList<>();
                for (Element img : imgsOfArticle) {
                    imgsUrls.add(new RealmString(img.attr("src")));
                }
            }

            //type parsing TODO fucking unformatted info!

            //this we store as article text
            String rawText = pageContent.toString();
//            Timber.d("rawText: %s", rawText);

            //articles textParts
            RealmList<RealmString> textParts = new RealmList<>();
            RealmList<RealmString> textPartsTypes = new RealmList<>();

            List<String> rawTextParts = ParseHtmlUtils.getArticlesTextParts(rawText);
            for (String value : rawTextParts) {
                textParts.add(new RealmString(value));
            }
            for (@ParseHtmlUtils.TextType String value : ParseHtmlUtils.getListOfTextTypes(rawTextParts)) {
                textPartsTypes.add(new RealmString(value));
            }

            //finally fill article info
            Article article = new Article();

            article.url = url;
            article.text = rawText;
            article.title = title;
            //textParts
            article.textParts = textParts;
            //log
//                if (article.textParts != null) {
//                    for (RealmString realmString : article.textParts) {
//                        Timber.d("part: %s", realmString.val);
//                    }
//                } else {
//                    Timber.d("article.textParts is NULL!");
//                }
            article.textPartsTypes = textPartsTypes;
            //images
            article.imagesUrls = imgsUrls;
            //tags
            article.tags = articleTags;
            //rating
            if (rating != 0) {
                article.rating = rating;
            }

            return article;
        } catch (Exception e) {
            Timber.e(e);
            throw e;
        }
    }

    /**
     * We need this as in FR site all article content wraped in another div... ***!!!11
     *
     * @return Element with article content
     */
    protected Element getArticlePageContentTag(Document doc) {
        return doc.getElementById("page-content");
    }

    public Observable<Article> getArticle(String url) {
        Timber.d("start download article: %s", url);
        return bindWithUtils(Observable.<Article>unsafeCreate(subscriber -> {
            try {
                Article article = getArticleFromApi(url);
                subscriber.onNext(article);
                subscriber.onCompleted();
            } catch (Exception | ScpParseException e) {
                subscriber.onError(e);
            }
        }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(article -> {
                    //download all images
                    if (article.imagesUrls != null) {
                        for (RealmString realmString : article.imagesUrls) {
//                            Timber.d("load image by Glide: %s", realmString.val);
                            Glide.with(BaseApplication.getAppInstance())
                                    .load(realmString.val)
                                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                    .listener(new RequestListener<String, GlideDrawable>() {
                                        @Override
                                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                            Timber.e("error while preload image by Glide");
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                                            Timber.d("onResourceReady: %s/%s", resource.getIntrinsicWidth(), resource.getIntrinsicHeight());
                                            return false;
                                        }
                                    })
                                    .preload();
                        }
                    }

                    return article;
                })
                .onErrorResumeNext(throwable -> Observable.error(new ScpException(throwable, url)));
    }

    public Observable<List<Article>> getMaterialsArticles(String objectsLink) {
        return bindWithUtils(Observable.<List<Article>>unsafeCreate(subscriber -> {
            Request request = new Request.Builder()
                    .url(objectsLink)
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
                //parse
                List<Element> listOfElements = pageContent.getElementsByTag("ul");
                for (int i = 0; i < listOfElements.size(); i++) {
                    ArrayList<Element> listOfLi = listOfElements.get(i).getElementsByTag("li");
                    for (int u = 0; u < listOfLi.size(); u++) {
                        String url = listOfLi.get(u).getElementsByTag("a").first().attr("href");
                        if (!url.startsWith("http")) {
                            url = mConstantValues.getBaseApiUrl() + url;
                        }
                        String text = listOfLi.get(u).text();
                        Article article = new Article();
                        article.title = text;
                        article.url = url;
                        articles.add(article);
                    }
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

    public Observable<List<Article>> getMaterialsArchiveArticles() {
        return bindWithUtils(Observable.<List<Article>>unsafeCreate(subscriber -> {
            Request request = new Request.Builder()
                    .url(mConstantValues.getArchive())
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

                //now we will remove all html code before tag h2,with id toc1
                String allHtml = pageContent.html();
                int indexOfh2WithIdToc1 = allHtml.indexOf("<h2 id=\"toc1\">");
                int indexOfh2WithIdToc5 = allHtml.indexOf("<h2 id=\"toc5\">");
                allHtml = allHtml.substring(indexOfh2WithIdToc1, indexOfh2WithIdToc5);

                doc = Jsoup.parse(allHtml);

                Element h2withIdToc1 = doc.getElementById("toc1");
                h2withIdToc1.remove();

                Elements allh2Tags = doc.getElementsByTag("h2");
                for (Element h2Tag : allh2Tags) {
                    Element brTag = new Element(Tag.valueOf("br"), "");
                    h2Tag.replaceWith(brTag);
                }
                Elements allP = doc.getElementsByTag("p");
                allP.remove();
                Elements allUl = doc.getElementsByTag("ul");
                allUl.remove();

                List<Article> articles = new ArrayList<>();

                String allArticles = doc.getElementsByTag("body").first().html();
                String[] arrayOfArticles = allArticles.split("<br>");
                for (String arrayItem : arrayOfArticles) {
                    doc = Jsoup.parse(arrayItem);
                    String imageURL = doc.getElementsByTag("img").first().attr("src");
                    String url = mConstantValues.getBaseApiUrl() + doc.getElementsByTag("a").first().attr("href");
                    String title = doc.text();

                    @Article.ObjectType
                    String type = getObjectTypeByImageUrl(imageURL);

                    Article article = new Article();
                    article.url = url;
                    article.type = type;
                    article.title = title;
                    articles.add(article);
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

    public Observable<List<Article>> getMaterialsJokesArticles() {
        return bindWithUtils(Observable.<List<Article>>unsafeCreate(subscriber -> {
            Request request = new Request.Builder()
                    .url(mConstantValues.getJokes())
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

                //now we will remove all html code before tag h2,with id toc1
                String allHtml = pageContent.html();
                int indexOfh2WithIdToc1 = allHtml.indexOf("<h2 id=\"toc1\">");
                allHtml = allHtml.substring(indexOfh2WithIdToc1);

                doc = Jsoup.parse(allHtml);

                Element h2withIdToc1 = doc.getElementById("toc1");
                h2withIdToc1.remove();

                Elements allh2Tags = doc.getElementsByTag("h2");
                for (Element h2Tag : allh2Tags) {
                    Element brTag = new Element(Tag.valueOf("br"), "");
                    h2Tag.replaceWith(brTag);
                }

                List<Article> articles = new ArrayList<>();

                String allArticles = doc.getElementsByTag("body").first().html();
                String[] arrayOfArticles = allArticles.split("<br>");
                for (String arrayItem : arrayOfArticles) {
//                    Timber.d("arrayItem: %s", arrayItem);
                    doc = Jsoup.parse(arrayItem);
                    String imageURL = null;
                    Elements img = doc.getElementsByTag("img");
                    if (img != null && !img.isEmpty()) {
                        imageURL = img.first().attr("src");
                    }
                    String url = mConstantValues.getBaseApiUrl() + doc.getElementsByTag("a").first().attr("href");
                    String title = doc.text();

                    @Article.ObjectType
                    String type = imageURL != null ? getObjectTypeByImageUrl(imageURL) : Article.ObjectType.NONE;

                    Article article = new Article();
                    article.url = url;
                    article.type = type;
                    article.title = title;
                    articles.add(article);
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

    public Observable<Boolean> joinVkGroup(String groupId) {
        Timber.d("joinVkGroup with groupId: %s", groupId);
        return bindWithUtils(Observable.<Boolean>unsafeCreate(subscriber -> {
                    VKParameters parameters = VKParameters.from(
                            VKApiConst.GROUP_ID, groupId,
                            VKApiConst.ACCESS_TOKEN, VKAccessToken.currentToken(),
                            VKApiConst.VERSION, BuildConfig.VK_API_VERSION
                    );

                    VKRequest vkRequest = VKApi.groups().join(parameters);
                    vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            Timber.d("onComplete: %s", response.responseString);
                            VkGroupJoinResponse vkGroupJoinResponse = mGson
                                    .fromJson(response.responseString, VkGroupJoinResponse.class);
                            Timber.d("vkGroupJoinResponse: %s", vkGroupJoinResponse);
                            subscriber.onNext(vkGroupJoinResponse.response == 1);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onError(VKError error) {
                            Timber.d("onError: %s", error);
                            subscriber.onError(new Throwable(error.toString()));
                        }
                    });
                })
        );
    }

    @Article.ObjectType
    private String getObjectTypeByImageUrl(String imageURL) {
        @Article.ObjectType
        String type;

        switch (imageURL) {
            case "http://scp-ru.wdfiles.com/local--files/scp-list-4/na.png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-3/na.png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-2/na(1).png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-ru/na(1).png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list/na.png":
            case "http://scp-ru.wdfiles.com/local--files/archive/na.png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-j/na(1).png":
                type = Article.ObjectType.NEUTRAL_OR_NOT_ADDED;
                break;
            case "http://scp-ru.wdfiles.com/local--files/scp-list-4/safe.png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-3/safe.png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-2/safe(1).png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-ru/safe(1).png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list/safe.png":
            case "http://scp-ru.wdfiles.com/local--files/archive/safe.png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-j/safe(1).png":
                type = Article.ObjectType.SAFE;
                break;
            case "http://scp-ru.wdfiles.com/local--files/scp-list-4/euclid.png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-3/euclid.png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-2/euclid(1).png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-ru/euclid(1).png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list/euclid.png":
            case "http://scp-ru.wdfiles.com/local--files/archive/euclid.png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-j/euclid(1).png":
                type = Article.ObjectType.EUCLID;
                break;
            case "http://scp-ru.wdfiles.com/local--files/scp-list-4/keter.png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-3/keter.png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-2/keter(1).png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-ru/keter(1).png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list/keter.png":
            case "http://scp-ru.wdfiles.com/local--files/archive/keter.png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-j/keter(1).png":
                type = Article.ObjectType.KETER;
                break;
            case "http://scp-ru.wdfiles.com/local--files/scp-list-4/thaumiel.png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-3/thaumiel.png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-2/thaumiel(1).png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-ru/thaumiel(1).png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list/thaumiel.png":
            case "http://scp-ru.wdfiles.com/local--files/archive/thaumiel.png":
            case "http://scp-ru.wdfiles.com/local--files/scp-list-j/thaumiel(1).png":
                type = Article.ObjectType.THAUMIEL;
                break;
            default:
                type = Article.ObjectType.NONE;
                break;
        }
        return type;
    }

    public Observable<List<VkImage>> getGallery() {
        Timber.d("getGallery");
        return bindWithUtils(Observable.unsafeCreate(subscriber -> {
                    VKParameters parameters = VKParameters.from(
                            VKApiConst.OWNER_ID, Constants.Api.GALLERY_VK_GROUP_ID,
                            VKApiConst.ALBUM_ID, Constants.Api.GALLERY_VK_ALBUM_ID,
                            VKApiConst.VERSION, BuildConfig.VK_API_VERSION
                    );

                    VKRequest vkRequest = new VKRequest("photos.get", parameters);
                    vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            Timber.d("onComplete");
//                            Timber.d("onComplete: %s", response.responseString);
                            VkGalleryResponse attachments = mGson
                                    .fromJson(response.responseString, VkGalleryResponse.class);
//                            Timber.d("attachments: %s", attachments);
                            List<VkImage> images = convertAttachmentsToImage(attachments.response.items);
                            subscriber.onNext(images);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onError(VKError error) {
                            Timber.d("onError: %s", error);
                            subscriber.onError(new Throwable(error.toString()));
                        }
                    });
                })
        );
    }

    private List<VkImage> convertAttachmentsToImage(List<VKApiPhoto> attachments) {
        List<VkImage> images = new ArrayList<>();
        for (VKAttachments.VKApiAttachment attachment : attachments) {
            if (attachment.getId() == 456239049) {
                continue;
            }
            VKApiPhoto vkApiPhoto = (VKApiPhoto) attachment;

            VkImage image = new VkImage();
            image.id = vkApiPhoto.id;
            image.ownerId = vkApiPhoto.owner_id;
            image.date = vkApiPhoto.date;
            //size
            image.width = vkApiPhoto.width;
            image.height = vkApiPhoto.height;
            //urls
            image.photo75 = vkApiPhoto.photo_75;
            image.photo130 = vkApiPhoto.photo_130;
            image.photo604 = vkApiPhoto.photo_604;
            image.photo807 = vkApiPhoto.photo_807;
            image.photo1280 = vkApiPhoto.photo_1280;
            image.photo2560 = vkApiPhoto.photo_2560;

            image.allUrls = new RealmList<>();

            if (image.photo75 != null) {
                image.allUrls.add(new RealmString(image.photo75));
            }
            if (image.photo130 != null) {
                image.allUrls.add(new RealmString(image.photo130));
            }
            if (image.photo604 != null) {
                image.allUrls.add(new RealmString(image.photo604));
            }
            if (image.photo807 != null) {
                image.allUrls.add(new RealmString(image.photo807));
            }
            if (image.photo1280 != null) {
                image.allUrls.add(new RealmString(image.photo1280));
            }
            if (image.photo2560 != null) {
                image.allUrls.add(new RealmString(image.photo2560));
            }

            image.description = vkApiPhoto.text;

            images.add(image);
        }
        return images;
    }

    private Observable<VKApiUser> getUserDataFromVk() {
        return Observable.unsafeCreate(subscriber -> VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "photo_200")).executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                //noinspection unchecked
                VKApiUser vkApiUser = ((VKList<VKApiUser>) response.parsedModel).get(0);
                Timber.d("User name %s %s", vkApiUser.first_name, vkApiUser.last_name);

                subscriber.onNext(vkApiUser);
                subscriber.onCompleted();
            }

            @Override
            public void onError(VKError error) {
                subscriber.onError(error.httpError);
            }
        }));
    }

    private Observable<FirebaseUser> authWithCustomToken(String token) {
        return Observable.unsafeCreate(subscriber ->
                FirebaseAuth.getInstance().signInWithCustomToken(token).addOnCompleteListener(task -> {
                    Timber.d("signInWithCustomToken:onComplete: %s", task.isSuccessful());

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        subscriber.onError(task.getException());
                    } else {
                        subscriber.onNext(task.getResult().getUser());
                        subscriber.onCompleted();
                    }
                }));
    }

    //todo use it via retrofit and update server to return JSON with request result
    public Observable<FirebaseUser> getAuthInFirebaseWithSocialProviderObservable(Constants.Firebase.SocialProvider provider, String id) {
        Observable<FirebaseUser> authToFirebaseObservable;
        switch (provider) {
            case VK:
                authToFirebaseObservable = Observable.<String>unsafeCreate(subscriber -> {
                    String url = BuildConfig.TOOLS_API_URL + "scp-ru-1/MyServlet";
                    String params = "?provider=vk&token=" +
                            id +
                            "&email=" + VKAccessToken.currentToken().email +
                            "&id=" + VKAccessToken.currentToken().userId;
                    Request request = new Request.Builder()
                            .url(url + params)
                            .build();
                    mOkHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String responseBody;
                            ResponseBody body = response.body();
                            if (body != null) {
                                responseBody = body.string();
                            } else {
                                subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(R.string.error_parse)));
                                return;
                            }
                            subscriber.onNext(responseBody);
                            subscriber.onCompleted();
                        }
                    });
                })
                        .flatMap(response -> TextUtils.isEmpty(response) ?
                                Observable.error(new IllegalArgumentException("empty token")) :
                                Observable.just(response))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(this::authWithCustomToken);
                break;
            case GOOGLE:
                authToFirebaseObservable = Observable.unsafeCreate(subscriber -> {
                    AuthCredential credential = GoogleAuthProvider.getCredential(id, null);
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Timber.d("signInWithCredential:success");
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            subscriber.onNext(user);
                            subscriber.onCompleted();
                        } else {
                            // If sign in fails, display a message to the user.
//                            Timber.e(task.getException(), "signInWithCredential:failure");
                            subscriber.onError(task.getException());
                        }
                    });
                });
                break;
            case FACEBOOK:
                authToFirebaseObservable = Observable.unsafeCreate(subscriber -> {
                    AuthCredential credential = FacebookAuthProvider.getCredential(id);
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Timber.d("signInWithCredential:success");
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    subscriber.onNext(user);
                                    subscriber.onCompleted();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Timber.e(task.getException(), "signInWithCredential:failure");
                                    subscriber.onError(task.getException());
                                }
                            });
                });
                break;
            default:
                throw new IllegalArgumentException("unexpected provider");
        }
        return authToFirebaseObservable;
    }

    public Observable<FirebaseObjectUser> getUserObjectFromFirebaseObservable() {
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                firebaseDatabase.getReference()
                        .child(Constants.Firebase.Refs.USERS)
                        .child(firebaseUser.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                FirebaseObjectUser userFromFireBase = dataSnapshot.getValue(FirebaseObjectUser.class);
                                subscriber.onNext(userFromFireBase);
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Timber.e(databaseError.toException(), "onCancelled");
                                subscriber.onError(databaseError.toException());
                            }
                        });
            } else {
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        });
    }

    public Observable<Void> updateFirebaseUsersEmailObservable() {
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                if (VKAccessToken.currentToken() != null) {
                    user.updateEmail(VKAccessToken.currentToken().email).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Timber.d("User profile updated email.");
                            subscriber.onNext(null);
                            subscriber.onCompleted();
                        } else {
                            Timber.e("error while update user email");
                            subscriber.onError(task.getException());
                        }
                    });
                } else {
                    Timber.d("not logined in vk, so cant get email and update firebase user with it");
                    subscriber.onError(new IllegalArgumentException("vk token is null, so can't get email to update firebase user"));
                }
            } else {
                Timber.e("firebase user is null while try to update!");
                subscriber.onError(new IllegalStateException("Firebase user is null while try to update its profile"));
            }
        });
    }

    public Observable<Void> updateFirebaseUsersNameAndAvatarObservable(String name, String avatar) {
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .setPhotoUri(Uri.parse(avatar))
                        .build();

                user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Timber.d("User profile updated name and photo.");
                        subscriber.onNext(null);
                        subscriber.onCompleted();
                    } else {
                        Timber.e("error while update user name and photo");
                        subscriber.onError(task.getException());
                    }
                });
            } else {
                Timber.e("firebase user is null while try to update!");
                subscriber.onError(new IllegalStateException("Firebase user is null while try to update its profile"));
            }
        });
    }

    public Observable<Pair<String, String>> nameAndAvatarFromProviderObservable(Constants.Firebase.SocialProvider provider) {
        Observable<Pair<String, String>> nameAvatarObservable;
        switch (provider) {
            case VK:
                nameAvatarObservable = getUserDataFromVk().flatMap(vkApiUser -> {
                    String displayName = vkApiUser.first_name + " " + vkApiUser.last_name;
                    String avatarUrl = vkApiUser.photo_200;
                    return Observable.just(new Pair<>(displayName, avatarUrl));
                });
                break;
            case FACEBOOK:
                Timber.d("attempt to get name and avatar from facebook");
                nameAvatarObservable = getUserDataFromFacebook();
                break;
            default:
                throw new RuntimeException("unexpected provider");
        }
        return nameAvatarObservable;
    }

    private Observable<Pair<String, String>> getUserDataFromFacebook() {
        return Observable.unsafeCreate(subscriber -> {
            Profile profile = Profile.getCurrentProfile();
            if (profile != null) {
                int size = DimensionUtils.dpToPx(56);
                subscriber.onNext(new Pair<>(profile.getName(), profile.getProfilePictureUri(size, size).toString()));
                subscriber.onCompleted();
            } else {
                subscriber.onError(new NullPointerException("profile is null"));
            }
        });
    }

    public Observable<Void> updateFirebaseUsersSocialProvidersObservable(List<SocialProviderModel> socialProviderModels) {
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                FirebaseDatabase.getInstance()
                        .getReference(Constants.Firebase.Refs.USERS)
                        .child(firebaseUser.getUid())
                        .child(Constants.Firebase.Refs.SOCIAL_PROVIDER)
                        .setValue(socialProviderModels, (databaseError, databaseReference) -> {
                            if (databaseError == null) {
                                //success
                                Timber.d("user created");
                                subscriber.onNext(null);
                                subscriber.onCompleted();
                            } else {
                                subscriber.onError(databaseError.toException());
                            }
                        });
            } else {
                Timber.e("firebase user is null while try to update!");
                subscriber.onError(new IllegalStateException("Firebase user is null while try to update its profile"));
            }
        });
    }

    /**
     * @param user local DB {@link User} object to write to firebase DB
     */
    public Observable<FirebaseObjectUser> writeUserToFirebaseObservable(FirebaseObjectUser user) {
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                FirebaseDatabase.getInstance()
                        .getReference(Constants.Firebase.Refs.USERS)
                        .child(firebaseUser.getUid())
                        .setValue(user, (databaseError, databaseReference) -> {
                            if (databaseError == null) {
                                //success
                                Timber.d("user created");
                                subscriber.onNext(user);
                                subscriber.onCompleted();
                            } else {
                                subscriber.onError(databaseError.toException());
                            }
                        });
            } else {
                subscriber.onError(new IllegalStateException("firebase user is null"));
            }
        });
    }

    /**
     * @param scoreToAdd score to add to user
     * @return Observable, that emits user total score
     */
    public Observable<Integer> incrementScoreInFirebaseObservable(int scoreToAdd) {
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                //add, not rewrite
                FirebaseDatabase.getInstance()
                        .getReference(Constants.Firebase.Refs.USERS)
                        .child(firebaseUser.getUid())
                        .child(Constants.Firebase.Refs.SCORE)
                        .runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                Integer p = mutableData.getValue(Integer.class);
                                if (p == null) {
                                    return Transaction.success(mutableData);
                                }

                                p = p + scoreToAdd;

                                // Set value and report transaction success
                                mutableData.setValue(p);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                if (databaseError == null) {
                                    Timber.d("onComplete: %s", dataSnapshot.getValue());
                                    subscriber.onNext(dataSnapshot.getValue(Integer.class));
                                    subscriber.onCompleted();
                                } else {
                                    Timber.e(databaseError.toException(), "onComplete with error: %s", databaseError.toString());
                                    subscriber.onError(databaseError.toException());
                                }
                            }
                        });
            } else {
                subscriber.onError(new IllegalStateException("firebase user is null"));
            }
        });
    }

    public Observable<Boolean> setUserRewardedForAuthInFirebaseObservable() {
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                //add, not rewrite
                FirebaseDatabase.getInstance()
                        .getReference(Constants.Firebase.Refs.USERS)
                        .child(firebaseUser.getUid())
                        .child(Constants.Firebase.Refs.SIGN_IN_REWARD_GAINED)
                        .setValue(true, (databaseError, databaseReference) -> {
                            if (databaseError == null) {
                                Timber.d("onComplete");
                                subscriber.onNext(true);
                                subscriber.onCompleted();
                            } else {
                                Timber.e(databaseError.toException(), "onComplete with error: %s", databaseError.toString());
                                subscriber.onError(databaseError.toException());
                            }
                        });
            } else {
                subscriber.onError(new IllegalStateException("firebase user is null"));
            }
        });
    }

    public Observable<Article> writeArticleToFirebase(Article article) {
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                subscriber.onError(new IllegalArgumentException("firebase user is null"));
                return;
            }
            String url = article.url.replace(mConstantValues.getBaseApiUrl(), "");

            //as firebase can't have dots or # in ref path we must replace it...
            url = url.replaceAll("#", "____");
            Timber.d("id: %s", url);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference()
                    .child(Constants.Firebase.Refs.USERS)
                    .child(firebaseUser.getUid())
                    .child(Constants.Firebase.Refs.ARTICLES)
                    .child(url);
            ArticleInFirebase articleInFirebase = new ArticleInFirebase(
                    article.isInFavorite != Article.ORDER_NONE,
                    article.isInReaden,
                    article.title,
                    article.url,
                    System.currentTimeMillis()
            );

            reference.setValue(articleInFirebase, (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    subscriber.onNext(article);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(databaseError.toException());
                }
            });
        });
    }

    public Observable<ArticleInFirebase> getArticleFromFirebase(Article article) {
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                subscriber.onError(new IllegalArgumentException("firebase user is null"));
                return;
            }
            String url = article.url.replace(mConstantValues.getBaseApiUrl(), "");

            //as firebase can't have dots or # in ref path we must replace it...
            url = url.replaceAll("#", "____");
            Timber.d("id: %s", url);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference()
                    .child(Constants.Firebase.Refs.USERS)
                    .child(firebaseUser.getUid())
                    .child(Constants.Firebase.Refs.ARTICLES)
                    .child(url);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    subscriber.onNext(dataSnapshot.getValue(ArticleInFirebase.class));
                    subscriber.onCompleted();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(databaseError.toException());
                }
            });
        });
    }

    public Observable<Integer> getUserScoreFromFirebase() {
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                subscriber.onError(new IllegalArgumentException("firebase user is null"));
                return;
            }
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference()
                    .child(Constants.Firebase.Refs.USERS)
                    .child(firebaseUser.getUid())
                    .child(Constants.Firebase.Refs.SCORE);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    subscriber.onNext(dataSnapshot.getValue(Integer.class));
                    subscriber.onCompleted();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(databaseError.toException());
                }
            });
        });
    }

    public Observable<Boolean> isUserRewardedForAuth() {
        Timber.d("isUserRewardedForAuth");
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                subscriber.onError(new IllegalArgumentException("firebase user is null"));
                return;
            }
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference()
                    .child(Constants.Firebase.Refs.USERS)
                    .child(firebaseUser.getUid())
                    .child(Constants.Firebase.Refs.SIGN_IN_REWARD_GAINED);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Boolean data = dataSnapshot.getValue(Boolean.class);
                    Timber.d("dataSnapshot.getValue(): %s", data);
                    subscriber.onNext(data != null && data);
                    subscriber.onCompleted();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(databaseError.toException());
                }
            });
        });
    }

    public Observable<Boolean> isUserJoinedVkGroup(String id) {
        Timber.d("isUserJoinedVkGroup id: %s", id);
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                subscriber.onError(new IllegalArgumentException("firebase user is null"));
                return;
            }
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference()
                    .child(Constants.Firebase.Refs.USERS)
                    .child(firebaseUser.getUid())
                    .child(Constants.Firebase.Refs.VK_GROUPS)
                    .child(id);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    VkGroupToJoin data = dataSnapshot.getValue(VkGroupToJoin.class);
                    Timber.d("dataSnapshot.getValue(): %s", data);
                    subscriber.onNext(data != null);
                    subscriber.onCompleted();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(databaseError.toException());
                }
            });
        });
    }

    public Observable<Void> addJoinedVkGroup(String id) {
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                subscriber.onError(new IllegalArgumentException("firebase user is null"));
                return;
            }
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference()
                    .child(Constants.Firebase.Refs.USERS)
                    .child(firebaseUser.getUid())
                    .child(Constants.Firebase.Refs.VK_GROUPS)
                    .child(id);
            reference.setValue(new VkGroupToJoin(id), (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(databaseError.toException());
                }
            });
        });
    }

    public Observable<Boolean> isUserInstallApp(String packageNameWithDots) {
        //as firebase can't have dots in ref path we must replace it...
        final String id = packageNameWithDots.replaceAll("\\.", "____");
        Timber.d("id: %s", id);
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                subscriber.onError(new IllegalArgumentException("firebase user is null"));
                return;
            }
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference()
                    .child(Constants.Firebase.Refs.USERS)
                    .child(firebaseUser.getUid())
                    .child(Constants.Firebase.Refs.APPS)
                    .child(id);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    PlayMarketApplication data = dataSnapshot.getValue(PlayMarketApplication.class);
                    Timber.d("dataSnapshot.getValue(): %s", data);
                    subscriber.onNext(data != null);
                    subscriber.onCompleted();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(databaseError.toException());
                }
            });
        });
    }

    public Observable<Void> addInstalledApp(String packageNameWithDots) {
        //as firebase can't have dots in ref path we must replace it...
        final String id = packageNameWithDots.replaceAll("\\.", "____");
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                subscriber.onError(new IllegalArgumentException("firebase user is null"));
                return;
            }
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference()
                    .child(Constants.Firebase.Refs.USERS)
                    .child(firebaseUser.getUid())
                    .child(Constants.Firebase.Refs.APPS)
                    .child(id);
            reference.setValue(new PlayMarketApplication(id), (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(databaseError.toException());
                }
            });
        });
    }

    public Observable<Boolean> isUserGainedSkoreFromInapp(String sku) {
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                subscriber.onError(new IllegalArgumentException("firebase user is null"));
                return;
            }
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference()
                    .child(Constants.Firebase.Refs.USERS)
                    .child(firebaseUser.getUid())
                    .child(Constants.Firebase.Refs.INAPP)
                    .child(sku);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    PlayMarketApplication data = dataSnapshot.getValue(PlayMarketApplication.class);
                    Timber.d("dataSnapshot.getValue(): %s", data);
                    subscriber.onNext(data != null);
                    subscriber.onCompleted();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(databaseError.toException());
                }
            });
        });
    }

    public Observable<Void> addRewardedInapp(String sku) {
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                subscriber.onError(new IllegalArgumentException("firebase user is null"));
                return;
            }
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference()
                    .child(Constants.Firebase.Refs.USERS)
                    .child(firebaseUser.getUid())
                    .child(Constants.Firebase.Refs.INAPP)
                    .child(sku);
            reference.push().setValue(true, (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(databaseError.toException());
                }
            });
        });
    }

    public Observable<Void> setCrackedInFirebase() {
        return Observable.unsafeCreate(subscriber -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                subscriber.onError(new IllegalArgumentException("firebase user is null"));
                return;
            }
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference()
                    .child(Constants.Firebase.Refs.USERS)
                    .child(firebaseUser.getUid())
                    .child(Constants.Firebase.Refs.CRACKED);
            reference.setValue(true, (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(databaseError.toException());
                }
            });
        });
    }

    public Observable<LeaderBoardResponse> getLeaderboard() {
        return bindWithUtils(mVpsServer.getLeaderboard(mConstantValues.getAppLang()));
    }

    public Observable<List<Article>> getArticlesByTags(List<ArticleTag> tags) {
        return bindWithUtils(mScpServer.getArticlesByTags(getScpServerWiki(), ArticleTag.getStringsFromTags(tags)))
                .map(ArticleFromSearchTagsOnSite::getArticlesFromSiteArticles)
                .map(articles -> {
                    for (Article article : articles) {
                        if (!article.url.startsWith("http://")) {
                            String start = mConstantValues.getBaseApiUrl();
                            if (!article.url.startsWith("/")) {
                                start += "/";
                            }
                            article.url = start + article.url;
                        }
                    }
                    return articles;
                });
    }

    public Observable<List<ArticleTag>> getTagsFromSite() {
        return bindWithUtils(mScpServer.getTagsList(getScpServerWiki())
                .map(strings -> {
                    List<ArticleTag> tags = new ArrayList<>();
                    for (String divWithTagData : strings) {
                        tags.add(new ArticleTag(divWithTagData));
                    }
                    return tags;
                }));
    }

    public Observable<PurchaseValidateResponse> validatePurchase(
            boolean isSubscription,
            String packageName,
            String sku,
            String purchaseToken
    ) {
        return bindWithUtils(mVpsServer.validatePurchase(isSubscription, packageName, sku, purchaseToken));
    }

    public PurchaseValidateResponse validatePurchaseSync(
            boolean isSubscription,
            String packageName,
            String sku,
            String purchaseToken
    ) throws IOException {
        return mVpsServer.validatePurchaseSync(isSubscription, packageName, sku, purchaseToken).execute().body();
    }

    public Observable<Boolean> inviteReceived(String inviteId, boolean newOne) {
        return mVpsServer.onInviteReceived(
                VpsServer.InviteAction.RECEIVED,
                inviteId,
                mConstantValues.getAppLang(),
                newOne
        ).map(onInviteReceivedResponse -> onInviteReceivedResponse.status);
    }

    public Observable<Boolean> inviteSent(String inviteId, String fcmToken) {
        return mVpsServer.onInviteSent(
                VpsServer.InviteAction.SENT,
                inviteId,
                mConstantValues.getAppLang(),
                fcmToken
        ).map(onInviteReceivedResponse -> onInviteReceivedResponse.status);
    }

    protected String getScpServerWiki() {
        return "scp-ru";
    }

    public ConstantValues getConstantValues() {
        return mConstantValues;
    }
}