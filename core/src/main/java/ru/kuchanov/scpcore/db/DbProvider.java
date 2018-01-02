package ru.kuchanov.scpcore.db;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.vk.sdk.VKSdk;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scp.downloads.DbProviderModel;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.api.model.firebase.ArticleInFirebase;
import ru.kuchanov.scpcore.db.error.ScpNoArticleForIdError;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.db.model.User;
import ru.kuchanov.scpcore.db.model.VkImage;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import rx.Observable;
import timber.log.Timber;

public class DbProvider implements DbProviderModel<Article> {

    private Realm mRealm;
    private MyPreferenceManager mMyPreferenceManager;
    private ConstantValues mConstantValues;

    DbProvider(MyPreferenceManager myPreferenceManager, ConstantValues constantValues) {
        mRealm = Realm.getDefaultInstance();
        mMyPreferenceManager = myPreferenceManager;
        mConstantValues = constantValues;
    }

    public void close() {
        Timber.d("close");
        mRealm.close();
    }

    @Override
    public int getScore() {
        User user = getUserSync();
        return user == null ? 0 : user.score;
    }

    public Observable<RealmResults<Article>> getArticlesByIds(@NonNull List<String> urls) {
        if(urls.isEmpty()){
            throw new IllegalArgumentException("Can't query by empty data list");
        }
        return mRealm.where(Article.class)
                .in(Article.FIELD_URL, urls.toArray(new String[0]))
                .findAllAsync()
                .asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid);
    }

    public Observable<RealmResults<Article>> getArticlesSortedAsync(String field, Sort order) {
        return mRealm.where(Article.class)
                .notEqualTo(field, Article.ORDER_NONE)
                .findAllSortedAsync(field, order)
                .asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid);
    }

    public Observable<RealmResults<Article>> getOfflineArticlesSortedAsync(String field, Sort order) {
        return mRealm.where(Article.class)
                .notEqualTo(Article.FIELD_TEXT, (String) null)
                //remove articles from main activity
                .notEqualTo(Article.FIELD_URL, mConstantValues.getAbout())
                .notEqualTo(Article.FIELD_URL, mConstantValues.getNews())
                .notEqualTo(Article.FIELD_URL, mConstantValues.getStories())
                .findAllSortedAsync(field, order)
                .asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid);
    }

    public Observable<RealmResults<Article>> getReadArticlesSortedAsync(String field, Sort order) {
        return mRealm.where(Article.class)
                .notEqualTo(Article.FIELD_IS_IN_READEN, false)
                .findAllSortedAsync(field, order)
                .asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid);
    }

    public Observable<Pair<Integer, Integer>> saveRecentArticlesList(List<Article> apiData, int offset) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    //remove all aps from nominees if we update list
                    if (offset == 0) {
                        List<Article> articles = realm.where(Article.class)
                                .notEqualTo(Article.FIELD_IS_IN_RECENT, Article.ORDER_NONE)
                                .findAll();
                        for (Article application : articles) {
                            application.isInRecent = Article.ORDER_NONE;
                        }
                    }
                    //check if we have app in db and update
                    for (int i = 0; i < apiData.size(); i++) {
                        Article applicationToWrite = apiData.get(i);
                        Article applicationInDb = realm.where(Article.class)
                                .equalTo(Article.FIELD_URL, applicationToWrite.url)
                                .findFirst();
                        if (applicationInDb != null) {
                            applicationInDb.isInRecent = offset + i;

                            if (applicationToWrite.rating != 0) {
                                applicationInDb.rating = applicationToWrite.rating;
                            }
                            applicationInDb.authorName = applicationToWrite.authorName;
                            applicationInDb.authorUrl = applicationToWrite.authorUrl;

                            applicationInDb.createdDate = applicationToWrite.createdDate;
                            applicationInDb.updatedDate = applicationToWrite.updatedDate;
                        } else {
                            applicationToWrite.isInRecent = offset + i;
                            realm.insertOrUpdate(applicationToWrite);
                        }
                    }
                },
                () -> {
                    subscriber.onNext(new Pair<>(apiData.size(), offset));
                    subscriber.onCompleted();
                    mRealm.close();
                },
                e -> {
                    subscriber.onError(e);
                    mRealm.close();
                }));
    }

    public Observable<Pair<Integer, Integer>> saveRatedArticlesList(List<Article> data, int offset) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    //remove all aps from nominees if we update list
                    if (offset == 0) {
                        List<Article> articleList = realm.where(Article.class)
                                .notEqualTo(Article.FIELD_IS_IN_MOST_RATED, Article.ORDER_NONE)
                                .findAll();
                        for (Article application : articleList) {
                            application.isInMostRated = Article.ORDER_NONE;
                        }
                    }
                    //check if we have app in db and update
                    for (int i = 0; i < data.size(); i++) {
                        Article applicationToWrite = data.get(i);
                        Article applicationInDb = realm.where(Article.class)
                                .equalTo(Article.FIELD_URL, applicationToWrite.url)
                                .findFirst();
                        if (applicationInDb != null) {
                            applicationInDb.isInMostRated = offset + i;

                            applicationInDb.rating = applicationToWrite.rating;
                        } else {
                            applicationToWrite.isInMostRated = offset + i;
                            realm.insertOrUpdate(applicationToWrite);
                        }
                    }
                },
                () -> {
                    subscriber.onNext(new Pair<>(data.size(), offset));
                    subscriber.onCompleted();
                    mRealm.close();
                },
                e -> {
                    subscriber.onError(e);
                    mRealm.close();
                }));
    }

    @Override
    public Observable<Pair<Integer, Integer>> saveObjectsArticlesList(List<Article> data, String inDbField) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    //remove all aps from this list while update it
                    List<Article> articleList =
                            realm.where(Article.class)
                                    .notEqualTo(inDbField, Article.ORDER_NONE)
                                    .findAll();
                    for (Article application : articleList) {
                        switch (inDbField) {
                            case Article.FIELD_IS_IN_OBJECTS_1:
                                application.isInObjects1 = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OBJECTS_2:
                                application.isInObjects2 = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OBJECTS_3:
                                application.isInObjects3 = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OBJECTS_4:
                                application.isInObjects4 = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OBJECTS_RU:
                                application.isInObjectsRu = Article.ORDER_NONE;
                                break;
                                //other filials
                            case Article.FIELD_IS_IN_OBJECTS_FR:
                                application.isInObjectsFr = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OBJECTS_JP:
                                application.isInObjectsJp = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OBJECTS_ES:
                                application.isInObjectsEs = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OBJECTS_PL:
                                application.isInObjectsPl = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OBJECTS_DE:
                                application.isInObjectsDe = Article.ORDER_NONE;
                                break;
                                //////
                            case Article.FIELD_IS_IN_EXPERIMETS:
                                application.isInExperiments = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_INCIDENTS:
                                application.isInIncidents = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_INTERVIEWS:
                                application.isInInterviews = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OTHER:
                                application.isInOther = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_ARCHIVE:
                                application.isInArchive = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_JOKES:
                                application.isInJokes = Article.ORDER_NONE;
                                break;
                            default:
                                Timber.e("unexpected inDbField id");
                                break;
                        }
                    }
                    //check if we have app in db and update
                    for (int i = 0; i < data.size(); i++) {
                        Article applicationToWrite = data.get(i);
                        Article applicationInDb = realm.where(Article.class)
                                .equalTo(Article.FIELD_URL, applicationToWrite.url)
                                .findFirst();
                        if (applicationInDb != null) {
                            switch (inDbField) {
                                case Article.FIELD_IS_IN_OBJECTS_1:
                                    applicationInDb.isInObjects1 = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_2:
                                    applicationInDb.isInObjects2 = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_3:
                                    applicationInDb.isInObjects3 = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_4:
                                    applicationInDb.isInObjects4 = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_RU:
                                    applicationInDb.isInObjectsRu = i;
                                    break;
                                    //other filials
                                case Article.FIELD_IS_IN_OBJECTS_FR:
                                    applicationInDb.isInObjectsFr = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_JP:
                                    applicationInDb.isInObjectsJp = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_ES:
                                    applicationInDb.isInObjectsEs = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_PL:
                                    applicationInDb.isInObjectsPl = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_DE:
                                    applicationInDb.isInObjectsDe = i;
                                    break;
                                    //////////
                                case Article.FIELD_IS_IN_EXPERIMETS:
                                    applicationInDb.isInExperiments = i;
                                    break;
                                case Article.FIELD_IS_IN_INCIDENTS:
                                    applicationInDb.isInIncidents = i;
                                    break;
                                case Article.FIELD_IS_IN_INTERVIEWS:
                                    applicationInDb.isInInterviews = i;
                                    break;
                                case Article.FIELD_IS_IN_OTHER:
                                    applicationInDb.isInOther = i;
                                    break;
                                case Article.FIELD_IS_IN_ARCHIVE:
                                    applicationInDb.isInArchive = i;
                                    break;
                                case Article.FIELD_IS_IN_JOKES:
                                    applicationInDb.isInJokes = i;
                                    break;
                                default:
                                    Timber.e("unexpected inDbField id");
                                    break;
                            }
                            applicationInDb.title = applicationToWrite.title;

                            applicationInDb.type = applicationToWrite.type;
                        } else {
                            switch (inDbField) {
                                case Article.FIELD_IS_IN_OBJECTS_1:
                                    applicationToWrite.isInObjects1 = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_2:
                                    applicationToWrite.isInObjects2 = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_3:
                                    applicationToWrite.isInObjects3 = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_4:
                                    applicationToWrite.isInObjects4 = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_RU:
                                    applicationToWrite.isInObjectsRu = i;
                                    break;
                                //other filials
                                case Article.FIELD_IS_IN_OBJECTS_FR:
                                    applicationToWrite.isInObjectsFr = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_JP:
                                    applicationToWrite.isInObjectsJp = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_ES:
                                    applicationToWrite.isInObjectsEs = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_PL:
                                    applicationToWrite.isInObjectsPl = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_DE:
                                    applicationToWrite.isInObjectsDe = i;
                                    break;
                                //////////
                                case Article.FIELD_IS_IN_EXPERIMETS:
                                    applicationToWrite.isInExperiments = i;
                                    break;
                                case Article.FIELD_IS_IN_INCIDENTS:
                                    applicationToWrite.isInIncidents = i;
                                    break;
                                case Article.FIELD_IS_IN_INTERVIEWS:
                                    applicationToWrite.isInInterviews = i;
                                    break;
                                case Article.FIELD_IS_IN_OTHER:
                                    applicationToWrite.isInOther = i;
                                    break;
                                case Article.FIELD_IS_IN_ARCHIVE:
                                    applicationToWrite.isInArchive = i;
                                    break;
                                case Article.FIELD_IS_IN_JOKES:
                                    applicationToWrite.isInJokes = i;
                                    break;
                                default:
                                    Timber.e("unexpected inDbField id");
                                    break;
                            }
                            realm.insertOrUpdate(applicationToWrite);
                        }
                    }
                },
                () -> {
                    subscriber.onNext(new Pair<>(data.size(), 0));
                    subscriber.onCompleted();
                    mRealm.close();
                },
                e -> {
                    subscriber.onError(e);
                    mRealm.close();
                }));
    }

    /**
     * @param articleUrl used as ID
     * @return Observable that emits <b>unmanaged</b>, valid and loaded Article
     * and emits changes to it
     * or null if there is no one in DB with this url
     */
    public Observable<Article> getUnmanagedArticleAsync(String articleUrl) {
        return mRealm.where(Article.class)
                .equalTo(Article.FIELD_URL, articleUrl)
                .findAllAsync()
                .<List<Article>>asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid)
                .flatMap(arts -> arts.isEmpty() ? Observable.just(null) : Observable.just(mRealm.copyFromRealm(arts.first())));
    }

    public Observable<Article> getUnmanagedArticleAsyncOnes(String articleUrl) {
        return mRealm.where(Article.class)
                .equalTo(Article.FIELD_URL, articleUrl)
                .findAllAsync()
                .<List<Article>>asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid)
                .first()
                .flatMap(arts -> arts.isEmpty() ? Observable.just(null) : Observable.just(mRealm.copyFromRealm(arts.first())))
                .doOnNext(article -> close());
    }

    public Article getUnmanagedArticleSync(String url) {
        Article articleFromDb = mRealm.where(Article.class).equalTo(Article.FIELD_URL, url).findFirst();
        return articleFromDb == null ? null : mRealm.copyFromRealm(articleFromDb);
    }

    public Article getArticleSync(String url) {
        return mRealm.where(Article.class).equalTo(Article.FIELD_URL, url).findFirst();
    }

    public Observable<List<Article>> saveMultipleArticlesWithoutTextSync(List<Article> data) {
        mRealm.executeTransaction(realm -> {
//            realm.insertOrUpdate(articles);
            //check if we have app in db and update
            for (int i = 0; i < data.size(); i++) {
                Article applicationToWrite = data.get(i);
                Article applicationInDb = realm.where(Article.class)
                        .equalTo(Article.FIELD_URL, applicationToWrite.url)
                        .findFirst();
                if (applicationInDb != null) {
//                    applicationInDb.isInMostRated = offset + i;
//                    applicationInDb.rating = applicationToWrite.rating;
                } else {
//                    applicationToWrite.isInMostRated = offset + i;
                    realm.insertOrUpdate(applicationToWrite);
                }
            }
        });
        mRealm.close();
        return Observable.just(data);
    }

    /**
     * @param article obj to save
     * @return Observable that emits unmanaged saved article on successful insert or throws error
     */
    public Observable<Article> saveArticle(Article article) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> saveArticleToRealm(article, realm),
                () -> {
                    subscriber.onNext(article);
                    subscriber.onCompleted();
                    mRealm.close();
                },
                e -> {
                    subscriber.onError(e);
                    mRealm.close();
                }));
    }

    /**
     * @param article obj to save
     * @return Observable that emits unmanaged saved article on successful insert or throws error
     */
    public Observable<Article> saveArticleSync(Article article) {
        mRealm.executeTransaction(realm -> saveArticleToRealm(article, realm));
        mRealm.close();
        return Observable.just(article);
    }

    public void saveArticleSync(Article article, boolean closeRealm) {
        mRealm.executeTransaction(realm -> saveArticleToRealm(article, realm));
        if (closeRealm) {
            mRealm.close();
        }
    }

    /**
     * Saves articles with text. Do not use it for saving articles lists without text
     *
     * @param articles obj to save
     * @return Observable that emits unmanaged saved article on successful insert or throws error
     */
    public Observable<List<Article>> saveMultipleArticlesSync(List<Article> articles) {
        mRealm.executeTransaction(realm -> {
            for (Article article : articles) {
                saveArticleToRealm(article, realm);
            }
        });
        mRealm.close();
        return Observable.just(articles);
    }

    private void saveArticleToRealm(Article article, Realm realm) {
        //if not have subscription
        //check if we have limit in downloads
        //if so - delete one article to save this
        if (!mMyPreferenceManager.isHasSubscription()) {
            FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
            if (!config.getBoolean(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_ALL_ENABLED_FOR_FREE)) {
                long numOfArtsInDb = realm.where(Article.class)
                        .notEqualTo(Article.FIELD_TEXT, (String) null)
                        //remove articles from main activity
                        .notEqualTo(Article.FIELD_URL, mConstantValues.getAbout())
                        .notEqualTo(Article.FIELD_URL, mConstantValues.getNews())
                        .notEqualTo(Article.FIELD_URL, mConstantValues.getStories())
                        .count();
                Timber.d("numOfArtsInDb: %s", numOfArtsInDb);
//                long limit = config.getLong(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_FREE_ARTICLES_LIMIT);
                FirebaseRemoteConfig remConf = FirebaseRemoteConfig.getInstance();
                int limit = (int) remConf.getLong(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_FREE_ARTICLES_LIMIT);
                int numOfScorePerArt = (int) remConf.getLong(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_SCORE_PER_ARTICLE);

                User user = realm.where(User.class).findFirst();
                if (user != null) {
                    limit += user.score / numOfScorePerArt;
                }

                Timber.d("limit: %s", limit);
                if (numOfArtsInDb + 1 > limit) {
                    int numOfArtsToDelete = (int) (numOfArtsInDb + 1 - limit);
                    Timber.d("numOfArtsToDelete: %s", numOfArtsToDelete);
                    for (int i = 0; i < numOfArtsToDelete; i++) {
                        RealmResults<Article> articlesToDelete = realm.where(Article.class)
                                .notEqualTo(Article.FIELD_TEXT, (String) null)
                                .notEqualTo(Article.FIELD_URL, article.url)
                                //remove articles from main activity
                                .notEqualTo(Article.FIELD_URL, mConstantValues.getAbout())
                                .notEqualTo(Article.FIELD_URL, mConstantValues.getNews())
                                .notEqualTo(Article.FIELD_URL, mConstantValues.getStories())
                                .findAllSorted(Article.FIELD_LOCAL_UPDATE_TIME_STAMP, Sort.ASCENDING);
                        if (!articlesToDelete.isEmpty()) {
                            Timber.d("delete text for: %s", articlesToDelete.first().title);
                            articlesToDelete.first().text = null;
                        }
                    }
                }
            }
        }

        long timeStamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
        Timber.d("insert/update: %s/%s", article.title, sdf.format(timeStamp));

        //check if we have app in db and update
        Article articleInDb = realm.where(Article.class)
                .equalTo(Article.FIELD_URL, article.url)
                .findFirst();
        if (articleInDb != null) {
            articleInDb = realm.copyFromRealm(articleInDb);
            articleInDb.text = article.text;
            //check length as in en site titles in article screen is less that it in articles list
            if (TextUtils.isEmpty(articleInDb.title) || articleInDb.title.length() < article.title.length()) {
                articleInDb.title = article.title;
            }
            if (article.rating != 0) {
                articleInDb.rating = article.rating;
            }
            //textParts
            articleInDb.textParts = article.textParts;
            articleInDb.textPartsTypes = article.textPartsTypes;
            //images
            articleInDb.imagesUrls = article.imagesUrls;
            //update localUpdateTimeStamp to be able to sort arts by this value
            articleInDb.localUpdateTimeStamp = timeStamp;

//            if (article.tags != null && !article.tags.isEmpty()) {
            articleInDb.tags.clear();
            articleInDb.tags = article.tags;
//            }

            //update it in DB such way, as we add unmanaged items
            realm.insertOrUpdate(articleInDb);
        } else {
            //update localUpdateTimeStamp to be able to sort arts by this value
            article.localUpdateTimeStamp = timeStamp;
            realm.insertOrUpdate(article);
        }
    }

    public Observable<Article> toggleFavorite(String url) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    //check if we have app in db and update
                    Article articleInDb = realm.where(Article.class)
                            .equalTo(Article.FIELD_URL, url)
                            .findFirst();
                    if (articleInDb != null) {
                        if (articleInDb.isInFavorite == Article.ORDER_NONE) {
                            articleInDb.isInFavorite = (long) realm.where(Article.class)
//                                        .notEqualTo(Article.FIELD_IS_IN_FAVORITE, Article.ORDER_NONE)
                                    .max(Article.FIELD_IS_IN_FAVORITE) + 1;
                        } else {
                            articleInDb.isInFavorite = Article.ORDER_NONE;
                        }

                        subscriber.onNext(realm.copyFromRealm(articleInDb));
                        subscriber.onCompleted();
                    } else {
                        Timber.e("No article to add to favorites for ID: %s", url);
                        subscriber.onError(new ScpNoArticleForIdError(url));
                    }
                },
                () -> {
                    subscriber.onCompleted();
                    mRealm.close();
                },
                e -> {
                    subscriber.onError(e);
                    mRealm.close();
                }));
    }

    /**
     * @param url used as Article ID
     * @return observable, that emits updated article
     * or error if no article found
     */
    public Observable<String> toggleReaden(String url) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    //check if we have app in db and update
                    Article article = realm.where(Article.class)
                            .equalTo(Article.FIELD_URL, url)
                            .findFirst();
                    if (article != null) {
//                        Timber.d("article: %s", article.isInReaden);
                        article.isInReaden = !article.isInReaden;
//                        Timber.d("article: %s", article.isInReaden);
//                        Article updatedArticle;
//                        updatedArticle = realm.copyFromRealm(article);
//                        Timber.d("updatedArticle: %s", updatedArticle.isInReaden);
//                        subscriber.onNext(updatedArticle);
//                        subscriber.onCompleted();
                    } else {
                        Timber.e("No article to add to favorites for ID: %s", url);
                        subscriber.onError(new ScpNoArticleForIdError(url));
                    }
                },
                () -> {
                    subscriber.onNext(url);
                    subscriber.onCompleted();
                    mRealm.close();
                },
                e -> {
                    subscriber.onError(e);
                    mRealm.close();
                }));
    }

    public Observable<String> deleteArticlesText(String url) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    //check if we have app in db and update
                    Article articleInDb = realm.where(Article.class)
                            .equalTo(Article.FIELD_URL, url)
                            .findFirst();
                    if (articleInDb != null) {
                        articleInDb.text = null;
                        articleInDb.textParts = null;
                        articleInDb.textPartsTypes = null;

                        subscriber.onNext(url);
                        subscriber.onCompleted();
                    } else {
                        Timber.e("No article to add to favorites for ID: %s", url);
                        subscriber.onError(new ScpNoArticleForIdError(url));
                    }
                },
                () -> {
                    subscriber.onCompleted();
                    mRealm.close();
                },
                e -> {
                    subscriber.onError(e);
                    mRealm.close();
                }));
    }

    /**
     * @return Observable, that emits unmanaged user
     */
    public Observable<User> getUserAsync() {
        return mRealm.where(User.class)
                .findAllAsync()
                .asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid)
                .flatMap(users -> Observable.just(users.isEmpty() ? null : mRealm.copyFromRealm(users.first())));
    }

    public User getUserSync() {
        return mRealm.where(User.class).findFirst();
    }

    public Observable<User> saveUser(User user) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> realm.insertOrUpdate(user),
                () -> {
                    subscriber.onNext(user);
                    subscriber.onCompleted();
                    mRealm.close();
                },
                e -> {
                    subscriber.onError(e);
                    mRealm.close();
                }));
    }

    private Observable<Void> deleteUserData() {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    realm.delete(User.class);
                    //do not delete it now... As we want to check if it will be better later via Firebase AB testing
//                    List<Article> favs = realm.where(Article.class)
//                            .notEqualTo(Article.FIELD_IS_IN_FAVORITE, Article.ORDER_NONE)
//                            .findAll();
//                    for (Article article : favs) {
//                        article.isInFavorite = Article.ORDER_NONE;
//                    }
//                    List<Article> read = realm.where(Article.class)
//                            .equalTo(Article.FIELD_IS_IN_READEN, true)
//                            .findAll();
//                    for (Article article : read) {
//                        article.isInReaden = false;
//                    }
                },
                () -> {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                    mRealm.close();
                },
                e -> {
                    subscriber.onError(e);
                    mRealm.close();
                }));
    }

    public Observable<Void> logout() {
        //run loop through enum with providers and logout from each of them
        for (Constants.Firebase.SocialProvider provider : Constants.Firebase.SocialProvider.values()) {
            switch (provider) {
                case VK:
                    VKSdk.logout();
                    break;
                case GOOGLE:
                    //do nothing...
                    break;
                case FACEBOOK:
                    LoginManager.getInstance().logOut();
                    break;
                default:
                    throw new IllegalArgumentException("unexpected provider");
            }
        }
        FirebaseAuth.getInstance().signOut();
        return deleteUserData();
    }

    public Observable<Void> saveImages(List<VkImage> vkImages) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    //clear
                    realm.delete(VkImage.class);
                    realm.insertOrUpdate(vkImages);
                },
                () -> {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                    mRealm.close();
                },
                e -> {
                    subscriber.onError(e);
                    mRealm.close();
                }));
    }

    public Observable<List<VkImage>> getGalleryImages() {
        return mRealm.where(VkImage.class)
                .findAllAsync()
                .asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid)
                .flatMap(realmResults -> Observable.just(mRealm.copyFromRealm(realmResults)));
    }

    public Observable<List<ArticleInFirebase>> saveArticlesFromFirebase(List<ArticleInFirebase> inFirebaseList) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    Collections.sort(inFirebaseList, (articleInFirebase, t1) ->
                            articleInFirebase.updated < t1.updated ? -1 : articleInFirebase.updated > t1.updated ? 1 : 0);
                    long counter = 0;
                    for (ArticleInFirebase article : inFirebaseList) {
                        Article realmArticle = realm.where(Article.class).equalTo(Article.FIELD_URL, article.url).findFirst();
                        if (realmArticle == null) {
                            realmArticle = new Article();
                            realmArticle.url = article.url;
                            realmArticle.title = article.title;
                            if (article.isFavorite) {
                                realmArticle.isInFavorite = counter;
                                counter++;
                            } else {
                                realmArticle.isInFavorite = Article.ORDER_NONE;
                            }
                            realmArticle.isInReaden = article.isRead;

                            realmArticle.synced = Article.SYNCED_OK;

                            realm.insert(realmArticle);
                        } else {
                            if (article.isFavorite) {
                                realmArticle.isInFavorite = counter;
                                counter++;
                            } else {
                                realmArticle.isInFavorite = Article.ORDER_NONE;
                            }
                            realmArticle.isInReaden = article.isRead;

                            realmArticle.synced = Article.SYNCED_OK;
                        }
                    }
                },
                () -> {
                    mRealm.close();
                    subscriber.onNext(inFirebaseList);
                    subscriber.onCompleted();
                },
                e -> {
                    mRealm.close();
                    subscriber.onError(e);
                })
        );
    }

    public Observable<Article> setArticleSynced(Article article, boolean synced) {
        Timber.d("setArticleSynced url: %s, newState: %s", article.url, synced);
        boolean managed = article.isManaged();
        String url = article.url;
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    Article articleInDb = realm.where(Article.class).equalTo(Article.FIELD_URL, url).findFirst();
                    if (articleInDb != null) {
                        articleInDb.synced = synced ? Article.SYNCED_OK : Article.SYNCED_NEED;
                    } else {
                        subscriber.onError(new ScpNoArticleForIdError(article.url));
                    }
                },
                () -> {
                    if (!managed) {
                        article.synced = synced ? Article.SYNCED_OK : Article.SYNCED_NEED;
                    }
                    subscriber.onNext(article);
                    subscriber.onCompleted();
                    mRealm.close();
                },
                e -> {
                    mRealm.close();
                    subscriber.onError(e);
                })
        );
    }

    public Observable<RealmResults<Article>> getUnsyncedArticlesManaged() {
        return mRealm.where(Article.class)
                .equalTo(Article.FIELD_SYNCED, Article.SYNCED_NEED)
                .findAllAsync()
                .asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid)
                .first();
    }

    /**
     * @return observable that emits num of updated articles
     */
    public Observable<Integer> setArticlesSynced(List<Article> articles, boolean synced) {
        Timber.d("setArticlesSynced size: %s, new state: %s", articles.size(), synced);
        List<String> urls = new ArrayList<>();
        for (Article article : articles) {
            urls.add(article.url);
        }
        int articlesToSyncSize = articles.size();
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    for (String url : urls) {
                        Article articleInDb = realm.where(Article.class).equalTo(Article.FIELD_URL, url).findFirst();
                        if (articleInDb != null) {
                            articleInDb.synced = synced ? Article.SYNCED_OK : Article.SYNCED_NEED;
                        }
                    }
                },
                () -> {
                    subscriber.onNext(articlesToSyncSize);
                    subscriber.onCompleted();
                    mRealm.close();
                },
                e -> {
                    subscriber.onError(e);
                    mRealm.close();
                })
        );
    }

    public Observable<Integer> updateUserScore(int totalScore) {
        Timber.d("updateUserScore: %s", totalScore);
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    //check if we have app in db and update
                    User user = realm.where(User.class).findFirst();
                    if (user != null) {
                        user.score = totalScore;
                    } else {
                        subscriber.onError(new IllegalStateException("No user to increment score"));
                    }
                },
                () -> {
                    subscriber.onNext(totalScore);
                    subscriber.onCompleted();
                    mRealm.close();
                },
                e -> {
                    subscriber.onError(e);
                    mRealm.close();
                })
        );
    }

    public Observable<RealmResults<ArticleTag>> getArticleTagsAsync() {
        return mRealm.where(ArticleTag.class)
                .findAllAsync()
                .asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid);
    }

    public Observable<List<ArticleTag>> saveArticleTags(List<ArticleTag> data) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> realm.insertOrUpdate(data),
                () -> {
                    mRealm.close();
                    subscriber.onNext(data);
                    subscriber.onCompleted();
                },
                e -> {
                    mRealm.close();
                    subscriber.onError(e);
                })
        );
    }

    public Observable<List<Article>> insertRestoredArticlesSync(List<Article> articles) {
        return Observable.unsafeCreate(subscriber -> {
            mRealm.executeTransaction(
                    realm -> {
                        for (Article article : articles) {
                            Article articleInDb = realm.where(Article.class)
                                    .equalTo(Article.FIELD_URL, article.url)
                                    .findFirst();
                            if (articleInDb != null) {
                                articleInDb.isInFavorite = (long) realm.where(Article.class)
                                        .max(Article.FIELD_IS_IN_FAVORITE) + 1;
                                if (article.isInReaden) {
                                    articleInDb.isInReaden = true;
                                }
                                articleInDb.synced = Article.SYNCED_NEED;
                            } else {
                                article.synced = Article.SYNCED_NEED;
                                realm.insertOrUpdate(article);
                            }
                        }
                    });
            mRealm.close();
            subscriber.onNext(articles);
            subscriber.onCompleted();
        });
    }
}