package ru.kuchanov.scpcore.db;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.facebook.login.LoginManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import com.vk.sdk.VKSdk;

import org.jetbrains.annotations.NotNull;

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
import ru.kuchanov.scpcore.db.model.LeaderboardUser;
import ru.kuchanov.scpcore.db.model.User;
import ru.kuchanov.scpcore.db.model.VkImage;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import rx.Observable;
import timber.log.Timber;

public class DbProvider implements DbProviderModel<Article> {

    private final Realm mRealm;

    private final MyPreferenceManager mMyPreferenceManager;

    private final ConstantValues mConstantValues;

    DbProvider(final MyPreferenceManager myPreferenceManager, final ConstantValues constantValues) {
        super();
        mRealm = Realm.getDefaultInstance();
        mMyPreferenceManager = myPreferenceManager;
        mConstantValues = constantValues;
    }

    @Override
    public void close() {
        Timber.d("close");
        mRealm.close();
    }

    @Override
    public int getScore() {
        final User user = getUserSync();
        return user == null ? 0 : user.score;
    }

    public Observable<RealmResults<Article>> getArticlesByIds(@NonNull final List<String> urls) {
        if (urls.isEmpty()) {
            throw new IllegalArgumentException("Can't query by empty data list");
        }
        return mRealm.where(Article.class)
                .in(Article.FIELD_URL, urls.toArray(new String[0]))
                .findAllAsync()
                .asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid);
    }

    public Observable<RealmResults<Article>> getArticlesSortedAsync(final String field, final Sort order) {
        return mRealm.where(Article.class)
                .notEqualTo(field, Article.ORDER_NONE)
                .findAllSortedAsync(field, order)
                .asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid);
    }

    public Observable<RealmResults<Article>> getOfflineArticlesSortedAsync(final String field, final Sort order) {
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

    public Observable<RealmResults<Article>> getReadArticlesSortedAsync(final String field, final Sort order) {
        return mRealm.where(Article.class)
                .notEqualTo(Article.FIELD_IS_IN_READEN, false)
                .findAllSortedAsync(field, order)
                .asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid);
    }

    public Observable<RealmResults<LeaderboardUser>> getLeaderboardUsers() {
        return mRealm.where(LeaderboardUser.class)
                .findAllSortedAsync(LeaderboardUser.FIELD_SCORE, Sort.DESCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid);
    }



    @NotNull
    public List<LeaderboardUser> getLeaderboardUsersUnmanaged() {
        List<LeaderboardUser> users = mRealm.where(LeaderboardUser.class)
                .findAllSorted(LeaderboardUser.FIELD_SCORE, Sort.DESCENDING);
        return users.isEmpty() ? Collections.emptyList() : mRealm.copyFromRealm(users);
    }

    public Observable<Integer> saveLeaderboardUsers(final List<LeaderboardUser> data) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    //remove all users
                    realm.delete(LeaderboardUser.class);
                    //insert all
                    realm.insertOrUpdate(data);
                },
                () -> {
                    subscriber.onNext(data.size());
                    subscriber.onCompleted();
                    mRealm.close();
                },
                e -> {
                    subscriber.onError(e);
                    mRealm.close();
                }
        ));
    }

    public Observable<Pair<Integer, Integer>> saveRecentArticlesList(final List<Article> apiData, final int offset) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    //remove all aps from nominees if we update list
                    if (offset == 0) {
                        final List<Article> articles = realm.where(Article.class)
                                .notEqualTo(Article.FIELD_IS_IN_RECENT, Article.ORDER_NONE)
                                .findAll();
                        for (final Article application : articles) {
                            application.isInRecent = Article.ORDER_NONE;
                        }
                    }
                    //check if we have app in db and update
                    for (int i = 0; i < apiData.size(); i++) {
                        final Article applicationToWrite = apiData.get(i);
                        final Article applicationInDb = realm.where(Article.class)
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
                }
        ));
    }

    public Observable<Pair<Integer, Integer>> saveRatedArticlesList(final List<Article> data, final int offset) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    //remove all aps from nominees if we update list
                    if (offset == 0) {
                        final List<Article> articleList = realm.where(Article.class)
                                .notEqualTo(Article.FIELD_IS_IN_MOST_RATED, Article.ORDER_NONE)
                                .findAll();
                        for (final Article application : articleList) {
                            application.isInMostRated = Article.ORDER_NONE;
                        }
                    }
                    //check if we have app in db and update
                    for (int i = 0; i < data.size(); i++) {
                        final Article applicationToWrite = data.get(i);
                        final Article applicationInDb = realm.where(Article.class)
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
                }
        ));
    }

    @Override
    public Observable<Pair<Integer, Integer>> saveObjectsArticlesList(final List<Article> data, final String inDbField) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    //remove all articles from this list while update it
                    final List<Article> articleList = realm.where(Article.class)
                            .notEqualTo(inDbField, Article.ORDER_NONE)
                            .findAll();
                    for (final Article article : articleList) {
                        switch (inDbField) {
                            case Article.FIELD_IS_IN_OBJECTS_1:
                                article.isInObjects1 = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OBJECTS_2:
                                article.isInObjects2 = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OBJECTS_3:
                                article.isInObjects3 = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OBJECTS_4:
                                article.isInObjects4 = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OBJECTS_RU:
                                article.isInObjectsRu = Article.ORDER_NONE;
                                break;
                            //other filials
                            case Article.FIELD_IS_IN_OBJECTS_FR:
                                article.isInObjectsFr = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OBJECTS_JP:
                                article.isInObjectsJp = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OBJECTS_ES:
                                article.isInObjectsEs = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OBJECTS_PL:
                                article.isInObjectsPl = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OBJECTS_DE:
                                article.isInObjectsDe = Article.ORDER_NONE;
                                break;
                            //////
                            case Article.FIELD_IS_IN_EXPERIMETS:
                                article.isInExperiments = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_INCIDENTS:
                                article.isInIncidents = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_INTERVIEWS:
                                article.isInInterviews = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_OTHER:
                                article.isInOther = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_ARCHIVE:
                                article.isInArchive = Article.ORDER_NONE;
                                break;
                            case Article.FIELD_IS_IN_JOKES:
                                article.isInJokes = Article.ORDER_NONE;
                                break;
                            default:
                                Timber.e("unexpected inDbField id");
                                break;
                        }
                    }
                    //check if we have app in db and update
                    for (int i = 0; i < data.size(); i++) {
                        final Article article = data.get(i);
                        final Article articleInDb = realm.where(Article.class)
                                .equalTo(Article.FIELD_URL, article.url)
                                .findFirst();
                        if (articleInDb != null) {
                            switch (inDbField) {
                                case Article.FIELD_IS_IN_OBJECTS_1:
                                    articleInDb.isInObjects1 = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_2:
                                    articleInDb.isInObjects2 = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_3:
                                    articleInDb.isInObjects3 = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_4:
                                    articleInDb.isInObjects4 = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_RU:
                                    articleInDb.isInObjectsRu = i;
                                    break;
                                //other filials
                                case Article.FIELD_IS_IN_OBJECTS_FR:
                                    articleInDb.isInObjectsFr = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_JP:
                                    articleInDb.isInObjectsJp = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_ES:
                                    articleInDb.isInObjectsEs = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_PL:
                                    articleInDb.isInObjectsPl = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_DE:
                                    articleInDb.isInObjectsDe = i;
                                    break;
                                //////////
                                case Article.FIELD_IS_IN_EXPERIMETS:
                                    articleInDb.isInExperiments = i;
                                    break;
                                case Article.FIELD_IS_IN_INCIDENTS:
                                    articleInDb.isInIncidents = i;
                                    break;
                                case Article.FIELD_IS_IN_INTERVIEWS:
                                    articleInDb.isInInterviews = i;
                                    break;
                                case Article.FIELD_IS_IN_OTHER:
                                    articleInDb.isInOther = i;
                                    break;
                                case Article.FIELD_IS_IN_ARCHIVE:
                                    articleInDb.isInArchive = i;
                                    break;
                                case Article.FIELD_IS_IN_JOKES:
                                    articleInDb.isInJokes = i;
                                    break;
                                default:
                                    Timber.e("unexpected inDbField id");
                                    break;
                            }
                            articleInDb.title = article.title;

                            articleInDb.type = article.type;
                        } else {
                            switch (inDbField) {
                                case Article.FIELD_IS_IN_OBJECTS_1:
                                    article.isInObjects1 = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_2:
                                    article.isInObjects2 = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_3:
                                    article.isInObjects3 = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_4:
                                    article.isInObjects4 = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_RU:
                                    article.isInObjectsRu = i;
                                    break;
                                //other filials
                                case Article.FIELD_IS_IN_OBJECTS_FR:
                                    article.isInObjectsFr = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_JP:
                                    article.isInObjectsJp = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_ES:
                                    article.isInObjectsEs = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_PL:
                                    article.isInObjectsPl = i;
                                    break;
                                case Article.FIELD_IS_IN_OBJECTS_DE:
                                    article.isInObjectsDe = i;
                                    break;
                                //////////
                                case Article.FIELD_IS_IN_EXPERIMETS:
                                    article.isInExperiments = i;
                                    break;
                                case Article.FIELD_IS_IN_INCIDENTS:
                                    article.isInIncidents = i;
                                    break;
                                case Article.FIELD_IS_IN_INTERVIEWS:
                                    article.isInInterviews = i;
                                    break;
                                case Article.FIELD_IS_IN_OTHER:
                                    article.isInOther = i;
                                    break;
                                case Article.FIELD_IS_IN_ARCHIVE:
                                    article.isInArchive = i;
                                    break;
                                case Article.FIELD_IS_IN_JOKES:
                                    article.isInJokes = i;
                                    break;
                                default:
                                    Timber.e("unexpected inDbField id");
                                    break;
                            }
                            realm.insertOrUpdate(article);
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
                }
        ));
    }

    /**
     * @param articleUrl used as ID
     * @return Observable that emits <b>unmanaged</b>, valid and loaded Article and emits changes to it or null if there is no one in DB with this url
     */
    public Observable<Article> getUnmanagedArticleAsync(final String articleUrl) {
        return mRealm.where(Article.class)
                .equalTo(Article.FIELD_URL, articleUrl)
                .findAllAsync()
                .<List<Article>>asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid)
                .flatMap(arts -> arts.isEmpty() ? Observable.just(null) : Observable.just(mRealm.copyFromRealm(arts.first())));
    }

    public Observable<Article> getUnmanagedArticleAsyncOnes(final String articleUrl) {
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

    @Override
    public Article getUnmanagedArticleSync(final String url) {
        final Article articleFromDb = mRealm.where(Article.class).equalTo(Article.FIELD_URL, url).findFirst();
        return articleFromDb == null ? null : mRealm.copyFromRealm(articleFromDb);
    }

    public Article getArticleSync(final String url) {
        return mRealm.where(Article.class).equalTo(Article.FIELD_URL, url).findFirst();
    }

    public Observable<List<Article>> saveMultipleArticlesWithoutTextSync(final List<Article> data) {
        mRealm.executeTransaction(realm -> {
//            realm.insertOrUpdate(articles);
            //check if we have app in db and update
            for (int i = 0; i < data.size(); i++) {
                final Article articleToWrite = data.get(i);
                final Article articleInDb = realm.where(Article.class)
                        .equalTo(Article.FIELD_URL, articleToWrite.url)
                        .findFirst();
                if (articleInDb != null) {
//                    applicationInDb.isInMostRated = offset + i;
//                    applicationInDb.rating = applicationToWrite.rating;
                } else {
//                    applicationToWrite.isInMostRated = offset + i;
                    realm.insertOrUpdate(articleToWrite);
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
    public Observable<Article> saveArticle(final Article article) {
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
                }
        ));
    }

    /**
     * @param article obj to save
     * @return Observable that emits unmanaged saved article on successful insert or throws error
     */
    public Observable<Article> saveArticleSync(final Article article) {
        mRealm.executeTransaction(realm -> saveArticleToRealm(article, realm));
        mRealm.close();
        return Observable.just(article);
    }

    @Override
    public void saveArticleSync(final Article article, final boolean closeRealm) {
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
    public Observable<List<Article>> saveMultipleArticlesSync(final List<Article> articles) {
        mRealm.executeTransaction(realm -> {
            for (final Article article : articles) {
                saveArticleToRealm(article, realm);
            }
        });
        mRealm.close();
        return Observable.just(articles);
    }

    private void saveArticleToRealm(final Article article, final Realm realm) {
        //if not have subscription
        //check if we have limit in downloads
        //if so - delete one article to save this
        if (!mMyPreferenceManager.isHasSubscription()) {
            final FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
            if (!config.getBoolean(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_ALL_ENABLED_FOR_FREE)) {
                final long numOfArtsInDb = realm.where(Article.class)
                        .notEqualTo(Article.FIELD_TEXT, (String) null)
                        //remove articles from main activity
                        .notEqualTo(Article.FIELD_URL, mConstantValues.getAbout())
                        .notEqualTo(Article.FIELD_URL, mConstantValues.getNews())
                        .notEqualTo(Article.FIELD_URL, mConstantValues.getStories())
                        .count();
                Timber.d("numOfArtsInDb: %s", numOfArtsInDb);
//                long limit = config.getLong(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_FREE_ARTICLES_LIMIT);
                final FirebaseRemoteConfig remConf = FirebaseRemoteConfig.getInstance();
                int limit = (int) remConf.getLong(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_FREE_ARTICLES_LIMIT);
                final int numOfScorePerArt = (int) remConf.getLong(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_SCORE_PER_ARTICLE);

                final User user = realm.where(User.class).findFirst();
                if (user != null) {
                    limit += user.score / numOfScorePerArt;
                }

                Timber.d("limit: %s", limit);
                if (numOfArtsInDb + 1 > limit) {
                    final int numOfArtsToDelete = (int) (numOfArtsInDb + 1 - limit);
                    Timber.d("numOfArtsToDelete: %s", numOfArtsToDelete);
                    for (int i = 0; i < numOfArtsToDelete; i++) {
                        final RealmResults<Article> articlesToDelete = realm.where(Article.class)
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

        final long timeStamp = System.currentTimeMillis();
        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
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

    public Observable<Article> toggleFavorite(final String url) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    //check if we have app in db and update
                    final Article articleInDb = realm.where(Article.class)
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
                }
        ));
    }

    /**
     * @param url used as Article ID
     * @return observable, that emits updated article or error if no article found
     */
    public Observable<String> toggleReaden(final String url) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    //check if we have app in db and update
                    final Article article = realm.where(Article.class)
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
                }
        ));
    }

    public Observable<String> deleteArticlesText(final String url) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    //check if we have app in db and update
                    final Article articleInDb = realm.where(Article.class)
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
                }
        ));
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

    /**
     * @return Observable, that emits unmanaged user
     */
    public Observable<User> getUserSyncUnmanaged() {
        return mRealm.where(User.class)
                .findAll()
                .asObservable()
                .flatMap(users -> Observable.just(users.isEmpty() ? null : mRealm.copyFromRealm(users.first())));
    }

    @Nullable
    public User getUserUnmanaged() {
        final User user = mRealm.where(User.class).findFirst();
        return mRealm.where(User.class).findFirst() == null ? null : mRealm.copyFromRealm(user);
    }

    public User getUserSync() {
        return mRealm.where(User.class).findFirst();
    }

    public Observable<User> saveUser(final User user) {
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
                }
        ));
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
                }
        ));
    }

    public Observable<Void> logout() {
        //run loop through enum with providers and logout from each of them
        for (final Constants.Firebase.SocialProvider provider : Constants.Firebase.SocialProvider.values()) {
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

    public Observable<Void> saveImages(final List<VkImage> vkImages) {
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
                }
        ));
    }

    public Observable<List<VkImage>> getGalleryImages() {
        return mRealm.where(VkImage.class)
                .findAllAsync()
                .asObservable()
                .filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid)
                .flatMap(realmResults -> Observable.just(mRealm.copyFromRealm(realmResults)));
    }

    public Observable<List<ArticleInFirebase>> saveArticlesFromFirebase(final List<ArticleInFirebase> inFirebaseList) {
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    Collections.sort(inFirebaseList, (articleInFirebase, t1) ->
                            articleInFirebase.updated < t1.updated ? -1 : articleInFirebase.updated > t1.updated ? 1 : 0);
                    long counter = 0;
                    for (final ArticleInFirebase article : inFirebaseList) {
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
                }
                )
        );
    }

    public Observable<Article> setArticleSynced(final Article article, final boolean synced) {
        Timber.d("setArticleSynced url: %s, newState: %s", article.url, synced);
        final boolean managed = article.isManaged();
        final String url = article.url;
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    final Article articleInDb = realm.where(Article.class).equalTo(Article.FIELD_URL, url).findFirst();
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
                }
                )
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
    public Observable<Integer> setArticlesSynced(final List<Article> articles, final boolean synced) {
        Timber.d("setArticlesSynced size: %s, new state: %s", articles.size(), synced);
        final List<String> urls = new ArrayList<>();
        for (final Article article : articles) {
            urls.add(article.url);
        }
        final int articlesToSyncSize = articles.size();
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    for (final String url : urls) {
                        final Article articleInDb = realm.where(Article.class).equalTo(Article.FIELD_URL, url).findFirst();
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
                }
                )
        );
    }

    public Observable<Integer> updateUserScore(final int totalScore) {
        Timber.d("updateUserScore: %s", totalScore);
        return Observable.unsafeCreate(subscriber -> mRealm.executeTransactionAsync(
                realm -> {
                    //check if we have app in db and update
                    final User user = realm.where(User.class).findFirst();
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

    public Observable<List<ArticleTag>> saveArticleTags(final List<ArticleTag> data) {
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
}