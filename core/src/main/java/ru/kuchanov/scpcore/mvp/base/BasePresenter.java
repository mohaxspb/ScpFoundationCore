package ru.kuchanov.scpcore.mvp.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;
import com.vk.sdk.VKSdk;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProvider;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.User;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.model.ApplicationsResponse;
import ru.kuchanov.scpcore.monetization.model.VkGroupsToJoinResponse;
import ru.kuchanov.scpcore.mvp.contract.LoginActions;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
public abstract class BasePresenter<V extends BaseMvp.View>
        extends MvpNullObjectBasePresenter<V>
        implements BaseMvp.Presenter<V> {

    MyPreferenceManager mMyPreferencesManager;
    protected DbProviderFactory mDbProviderFactory;
    protected ApiClient mApiClient;

    private User mUser;

    protected boolean getUserInConstructor() {
        return true;
    }

    public BasePresenter(MyPreferenceManager myPreferencesManager, DbProviderFactory dbProviderFactory, ApiClient apiClient) {
        mMyPreferencesManager = myPreferencesManager;
        mDbProviderFactory = dbProviderFactory;
        mApiClient = apiClient;

        if (getUserInConstructor()) {
            getUserFromDb();
        }
    }

    @Override
    public void attachView(@NonNull V view) {
        super.attachView(view);
        if (!getUserInConstructor()) {
            getUserFromDb();
        }
    }

    @Override
    public void onCreate() {
        Timber.d("onCreate");
    }

    @Override
    public void getUserFromDb() {
        Timber.d("getUserFromDb");
        mDbProviderFactory.getDbProvider().getUserAsync().subscribe(
                user -> {
                    mUser = user;
                    if (getView() instanceof LoginActions.View) {
                        ((LoginActions.View) getView()).updateUser(mUser);
                    }
                    onUserChanged(mUser);
                },
                e -> Timber.e(e, "error while get user from DB")
        );
    }

    @Override
    public void onUserChanged(User user) {
        //empty implementation
    }

    @Override
    public User getUser() {
        return mUser;
    }

    @Override
    public void updateArticleInFirebase(Article article, boolean showResultMessage) {
        Timber.d("updateArticleInFirebase: %s", article.url);

        if (!mMyPreferencesManager.isHasSubscription()) {
            long curNumOfAttempts = mMyPreferencesManager.getNumOfAttemptsToAutoSync();
            long maxNumOfAttempts = FirebaseRemoteConfig.getInstance()
                    .getLong(Constants.Firebase.RemoteConfigKeys.NUM_OF_SYNC_ATTEMPTS_BEFORE_CALL_TO_ACTION);

            Timber.d("does not have subscription, so no auto sync: %s/%s", curNumOfAttempts, maxNumOfAttempts);

            if (curNumOfAttempts >= maxNumOfAttempts) {
                //show call to action
                mMyPreferencesManager.setNumOfAttemptsToAutoSync(0);
                getView().showSnackBarWithAction(Constants.Firebase.CallToActionReason.ENABLE_AUTO_SYNC);
            } else {
                mMyPreferencesManager.setNumOfAttemptsToAutoSync(curNumOfAttempts + 1);
            }
            return;
        }
        //ignore articles which not starts with base domain
        if (!article.url.startsWith(mApiClient.getConstantValues().getBaseApiUrl())) {
            mDbProviderFactory.getDbProvider().setArticleSynced(article, true).subscribe(
                    article1 -> Timber.d("article1 synced"),
                    Timber::e
            );
            return;
        }

        @ScoreAction
        String action = article.isInReaden ? ScoreAction.READ :
                article.isInFavorite != Article.ORDER_NONE ? ScoreAction.FAVORITE : ScoreAction.NONE;

        int totalScoreToAdd = getTotalScoreToAddFromAction(action, mMyPreferencesManager);

        //update score for articles, that is not in firebase, than write/update them
        mApiClient
                .getArticleFromFirebase(article)
                .flatMap(articleInFirebase -> articleInFirebase == null ?
                        mApiClient.incrementScoreInFirebaseObservable(totalScoreToAdd)
                                //score will be added to firebase user object
                                .flatMap(newTotalScore -> Observable.just(article))
                        : Observable.just(article))
                .flatMap(article1 -> mApiClient.writeArticleToFirebase(article1))
                .flatMap(article1 -> mDbProviderFactory.getDbProvider().setArticleSynced(article1, true))
                .subscribe(
                        article1 -> {
                            Timber.d("sync article onComplete: %s", article1.url);
                            //show only for favorites
                            if (showResultMessage) {
                                getView().showMessage(R.string.sync_fav_success);
                            }
                        },
                        e -> {
                            Timber.e(e);
                            if (showResultMessage) {
                                getView().showError(new Throwable(BaseApplication.getAppInstance().getString(R.string.error_while_sync)));
                            }
                        }
                );
    }

    @Override
    public void syncData(boolean showResultMessage) {
        Timber.d("syncData showResultMessage: %s", showResultMessage);
        //get unsynced articles from DB
        //write them to firebase
        //mark them as synced
        DbProvider dbProvider = mDbProviderFactory.getDbProvider();
        dbProvider.getUnsyncedArticlesManaged()
                .doOnNext(articles -> Timber.d("articles: %s", articles))
                .flatMap(articles -> articles.isEmpty() ? Observable.just(new Pair<>(0, 0)) :
                        //need to calculate and add score for firstly synced articles
                        //caclulate how many new articles we add and return it to calculate hoц much score we should add
                        //I think that this can be done via calculate initial childs of ARTICLE ref minus result childs of ARTICLE ref
                        Observable.from(articles)
                                .flatMap(article -> {
                                    //ignore articles which not starts with base domain
                                    if (!article.url.startsWith(mApiClient.getConstantValues().getBaseApiUrl())) {
                                        Timber.e("Article from no main domain MUST IGNORE");
                                        return mDbProviderFactory.getDbProvider().setArticleSynced(article, true)
                                                .flatMap(art -> Observable.just(new Pair<>(article, 0)));
                                    }

                                    @ScoreAction
                                    String action = article.isInReaden ? ScoreAction.READ :
                                            article.isInFavorite != Article.ORDER_NONE ? ScoreAction.FAVORITE : ScoreAction.NONE;

                                    int totalScoreToAdd = getTotalScoreToAddFromAction(action, mMyPreferencesManager);

                                    return mApiClient
                                            .getArticleFromFirebase(article)
                                            .flatMap(articleInFirebase -> articleInFirebase == null ?
                                                    mApiClient.incrementScoreInFirebaseObservable(totalScoreToAdd)
                                                            .flatMap(firebaseUserScore -> mDbProviderFactory.getDbProvider().updateUserScore(firebaseUserScore))
                                                            .flatMap(integer -> Observable.just((new Pair<>(article, totalScoreToAdd))))
                                                    : Observable.just(new Pair<>(article, 0)))
                                            .flatMap(articleAndScore -> mApiClient.writeArticleToFirebase(articleAndScore.first).flatMap(article1 -> Observable.just(articleAndScore)))
                                            .flatMap(articleAndScore -> mDbProviderFactory.getDbProvider().setArticleSynced(articleAndScore.first, true).flatMap(article1 -> Observable.just(articleAndScore)))
                                            //try not to break whole operation if error ocures
                                            .doOnError(Timber::e)
                                            .onErrorResumeNext(error -> mDbProviderFactory.getDbProvider().setArticleSynced(article, true).flatMap(article1 -> Observable.just(new Pair<>(article, 0))));
                                })
                                .toList()
                                .flatMap(articleAndScores -> {
                                    int totalAddedScore = 0;
                                    for (Pair<Article, Integer> articleAndScore : articleAndScores) {
                                        totalAddedScore += articleAndScore.second;
                                    }
                                    return Observable.just(new Pair<>(articleAndScores.size(), totalAddedScore));
                                })
                )
                //also increment user score from unsynced score
                .flatMap(articlesCountAndAddedScore -> {
                    Timber.d("num of updated articles/added score: %s/%s", articlesCountAndAddedScore.first, articlesCountAndAddedScore.second);
                    int unsyncedScore = mMyPreferencesManager.getNumOfUnsyncedScore();
                    if (unsyncedScore == 0) {
                        //getScore from firebase and update it in Realm
                        //as there can be situation, where we have nothing to sync except of score
                        //added from another device
                        return mApiClient.getUserScoreFromFirebase()
                                .flatMap(firebaseUserScore -> mDbProviderFactory.getDbProvider().updateUserScore(firebaseUserScore))
                                .flatMap(totalScore -> Observable.just(new Pair<>(articlesCountAndAddedScore.first, articlesCountAndAddedScore.second)));
                    } else {
                        return mApiClient.incrementScoreInFirebaseObservable(unsyncedScore)
                                .flatMap(newTotalScore -> mDbProviderFactory.getDbProvider().updateUserScore(newTotalScore))
                                .flatMap(newTotalScore -> Observable.just(new Pair<>(articlesCountAndAddedScore.first, articlesCountAndAddedScore.second + unsyncedScore)));
                    }
                })
                //add unsynced score for vkGroups
                .flatMap(artsAndScoreAdded -> {
                    VkGroupsToJoinResponse unsyncedScore = mMyPreferencesManager.getUnsyncedVkGroupsJson();
                    if (unsyncedScore == null) {
                        //no need to update something
                        return Observable.just(artsAndScoreAdded);
                    } else {
                        //get unjoined vk groups that we must sync
                        @ScoreAction
                        String action = ScoreAction.VK_GROUP;
                        int actionScore = getTotalScoreToAddFromAction(action, mMyPreferencesManager);
                        return Observable.from(unsyncedScore.items)
                                .map(vkGroupToJoin -> vkGroupToJoin.id)
                                .doOnNext(id -> Timber.d("vkGroup id to check: %s", id))
                                .flatMap(vkGroupToJoinId -> mApiClient.isUserJoinedVkGroup(vkGroupToJoinId)
                                        .flatMap(isUserJoinedVkGroup -> isUserJoinedVkGroup ?
                                                Observable.empty() :
                                                //TODO add error handling
                                                mApiClient.incrementScoreInFirebaseObservable(actionScore)
                                                        .flatMap(newTotalScore -> mDbProviderFactory.getDbProvider().updateUserScore(newTotalScore))
                                                        .flatMap(newTotalScore -> mApiClient.addJoinedVkGroup(vkGroupToJoinId).flatMap(aVoid -> Observable.just(actionScore)))))
                                .toList()
                                .flatMap(integers -> {
                                    mMyPreferencesManager.deleteUnsyncedVkGroups();
                                    return Observable.just(new Pair<>(
                                            artsAndScoreAdded.first,
                                            artsAndScoreAdded.second + actionScore * integers.size())
                                    );
                                });
                    }
                })
                //add unsynced score for apps
                .flatMap(artsAndScoreAdded -> {
                    ApplicationsResponse unsyncedScore = mMyPreferencesManager.getUnsyncedAppsJson();
                    if (unsyncedScore == null) {
                        //no need to update something
                        return Observable.just(artsAndScoreAdded);
                    } else {
                        //get uninstalled apps that we must sync
                        @ScoreAction
                        String action = ScoreAction.OUR_APP;
                        int actionScore = getTotalScoreToAddFromAction(action, mMyPreferencesManager);
                        return Observable.from(unsyncedScore.items)
                                .map(item -> item.id)
                                .doOnNext(id -> Timber.d("application id to check: %s", id))
                                .flatMap(itemId -> mApiClient.isUserInstallApp(itemId)
                                        .flatMap(isUserInstallApp -> isUserInstallApp ?
                                                Observable.empty() :
                                                //TODO add error handling
                                                mApiClient.incrementScoreInFirebaseObservable(actionScore)
                                                        .flatMap(newTotalScore -> mDbProviderFactory.getDbProvider().updateUserScore(newTotalScore))
                                                        .flatMap(newTotalScore -> mApiClient.addInstalledApp(itemId).flatMap(aVoid -> Observable.just(actionScore)))))
                                .toList()
                                .flatMap(integers -> {
                                    mMyPreferencesManager.deleteUnsyncedApps();
                                    return Observable.just(new Pair<>(
                                            artsAndScoreAdded.first,
                                            artsAndScoreAdded.second + actionScore * integers.size())
                                    );
                                });
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            Timber.d("articles saved to firebase/score added: %s/%s", data.first, data.second);
                            if (showResultMessage) {
                                if (data.first == 0 && data.second == 0) {
                                    //TODO add plurals support
                                    getView().showMessageLong(R.string.all_data_already_synced);
                                } else {
                                    getView().showMessageLong(BaseApplication.getAppInstance()
                                            .getString(R.string.all_data_sync_success, data.first, data.second));
                                }
                            }
                            dbProvider.close();
                            //we should set zero as unsynced score only in onSuccess callback,
                            //to not loose some score from broken connection
                            //reset unsynced score as we already sync it
                            mMyPreferencesManager.setNumOfUnsyncedScore(0);
                        },
                        e -> {
                            Timber.e(e);
                            if (showResultMessage) {
                                String errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
                                getView().showMessage(BaseApplication.getAppInstance().getString(R.string.error_while_all_data_sync, errorMessage));
                            }
                            dbProvider.close();
                        }
                );
    }

    /**
     * check if user logged in,
     * calculate final score to add value from modificators,
     * if user do not have subscription we increment unsynced score
     * if user has subscription we increment score in firebase
     * while incrementing we check if user already received score from group
     * and if so - do not increment it
     */
    @Override
    public void updateUserScoreForVkGroup(String id) {
        Timber.d("updateUserScore: %s", id);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Timber.d("user unlogined, do nothing");
            return;
        }

        @ScoreAction
        String action = ScoreAction.VK_GROUP;
        int totalScoreToAdd = getTotalScoreToAddFromAction(action, mMyPreferencesManager);

//        if (!mMyPreferencesManager.isHasSubscription()) {
//            long curNumOfAttempts = mMyPreferencesManager.getNumOfAttemptsToAutoSync();
//            long maxNumOfAttempts = FirebaseRemoteConfig.getInstance()
//                    .getLong(Constants.Firebase.RemoteConfigKeys.NUM_OF_SYNC_ATTEMPTS_BEFORE_CALL_TO_ACTION);
//
//            Timber.d("does not have subscription, so no auto sync: %s/%s", curNumOfAttempts, maxNumOfAttempts);
//
//            if (curNumOfAttempts >= maxNumOfAttempts) {
//                //show call to action
//                mMyPreferencesManager.setNumOfAttemptsToAutoSync(0);
//                getView().showSnackBarWithAction(Constants.Firebase.CallToActionReason.ENABLE_AUTO_SYNC);
//            } else {
//                mMyPreferencesManager.setNumOfAttemptsToAutoSync(curNumOfAttempts + 1);
//            }
//
//            //increment unsynced score to sync it later
//            mMyPreferencesManager.addUnsyncedVkGroup(id);
//            return;
//        }

        //increment scoreInFirebase
        mApiClient
                .isUserJoinedVkGroup(id)
                .flatMap(isUserJoinedVkGroup -> isUserJoinedVkGroup ?
                        Observable.empty() :
                        mApiClient
                                .incrementScoreInFirebaseObservable(totalScoreToAdd)
                                .flatMap(newTotalScore -> mApiClient.addJoinedVkGroup(id).flatMap(aVoid -> Observable.just(newTotalScore)))
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(newTotalScore -> mDbProviderFactory.getDbProvider().updateUserScore(newTotalScore))
                .subscribe(
                        newTotalScore -> {
                            Timber.d("new total score is: %s", newTotalScore);
                            Context context = BaseApplication.getAppInstance();
                            getView().showMessage(context.getString(R.string.score_increased, context.getResources().getQuantityString(R.plurals.plurals_score, totalScoreToAdd, totalScoreToAdd)));
                        },
                        e -> {
                            Timber.e(e, "error while increment userCore from action");
                            getView().showError(e);
                            //increment unsynced score to sync it later
                            mMyPreferencesManager.addUnsyncedVkGroup(id);
                        }
                );
    }

    /**
     * check if user logged in,
     * calculate final score to add value from modificators,
     * if user do not have subscription we increment unsynced score
     * if user has subscription we increment score in firebase
     * while incrementing we check if user already received score from group
     * and if so - do not increment it
     */
    @Override
    public void updateUserScoreForScoreAction(@ScoreAction String action) {
        Timber.d("updateUserScore: %s", action);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Timber.d("user unlogined, do nothing");
            return;
        }

        int totalScoreToAdd = getTotalScoreToAddFromAction(action, mMyPreferencesManager);

        if (!action.equals(ScoreAction.REWARDED_VIDEO) && !mMyPreferencesManager.isHasSubscription()) {
            long curNumOfAttempts = mMyPreferencesManager.getNumOfAttemptsToAutoSync();
            long maxNumOfAttempts = FirebaseRemoteConfig.getInstance()
                    .getLong(Constants.Firebase.RemoteConfigKeys.NUM_OF_SYNC_ATTEMPTS_BEFORE_CALL_TO_ACTION);

            Timber.d("does not have subscription, so no auto sync: %s/%s", curNumOfAttempts, maxNumOfAttempts);

            if (curNumOfAttempts >= maxNumOfAttempts) {
                //show call to action
                mMyPreferencesManager.setNumOfAttemptsToAutoSync(0);
                //do not show for adding score after showing ads
                if (!action.equals(ScoreAction.INTERSTITIAL_SHOWN)) {
                    getView().showSnackBarWithAction(Constants.Firebase.CallToActionReason.ENABLE_AUTO_SYNC);
                }
            } else {
                mMyPreferencesManager.setNumOfAttemptsToAutoSync(curNumOfAttempts + 1);
            }

            //increment unsynced score to sync it later
            mMyPreferencesManager.addUnsyncedScore(totalScoreToAdd);
            return;
        }

        //increment scoreInFirebase
        mApiClient.incrementScoreInFirebaseObservable(totalScoreToAdd)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(newTotalScore -> mDbProviderFactory.getDbProvider().updateUserScore(newTotalScore))
                .subscribe(
                        newTotalScore -> {
                            Timber.d("new total score is: %s", newTotalScore);
                            Context context = BaseApplication.getAppInstance();
                            if (action.equals(ScoreAction.REWARDED_VIDEO)
                                    || action.equals(ScoreAction.VK_GROUP)
                                    || action.equals(ScoreAction.OUR_APP)) {
                                getView().showMessage(context.getString(R.string.score_increased, context.getResources().getQuantityString(R.plurals.plurals_score, totalScoreToAdd, totalScoreToAdd)));
                            }
                        },
                        e -> {
                            Timber.e(e, "error while increment userCore from action");
                            getView().showError(e);
                            //increment unsynced score to sync it later
                            mMyPreferencesManager.addUnsyncedScore(totalScoreToAdd);
                        }
                );
    }

    @Override
    public void updateUserScoreForInapp(String sku) {
        Timber.d("updateUserScore: %s", sku);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Timber.d("user unlogined, do nothing");
            return;
        }

        //increment scoreInFirebase
        int totalScoreToAdd;

        switch (sku) {
            case "level_up_to_5":
                totalScoreToAdd = 10000;
                break;
            default:
                throw new IllegalArgumentException("unexpected sku");
        }

        mApiClient
                .incrementScoreInFirebaseObservable(totalScoreToAdd)
                .flatMap(newTotalScore -> mApiClient.addRewardedInapp(sku).flatMap(aVoid -> mDbProviderFactory.getDbProvider().updateUserScore(newTotalScore)))
                //TODO need to realize it as we realize vk groups and apps - write inapps to json and check if we need to add score for it
                .subscribe(
                        newTotalScore -> {
                            Timber.d("new total score is: %s", newTotalScore);
                            Context context = BaseApplication.getAppInstance();
                            getView().showMessage(context.getString(R.string.score_increased, context.getResources().getQuantityString(R.plurals.plurals_score, totalScoreToAdd, totalScoreToAdd)));
                        },
                        e -> {
                            Timber.e(e, "error while increment userCore from inapp");
                            getView().showError(e);
                            //increment unsynced score to sync it later
                            mMyPreferencesManager.addUnsyncedScore(totalScoreToAdd);
                        }
                );
    }

    @Override
    public void checkIfUserJoinedAppVkGroup() {
        Timber.d("checkIfUserJoinedAppVkGroup");
        if (!VKSdk.isLoggedIn() || !mMyPreferencesManager.isTimeToCheckAppVkGroupJoined()) {
            return;
        }
        mMyPreferencesManager.setLastTimeAppVkGroupJoinedChecked(System.currentTimeMillis());
        String appVkGroupId = FirebaseRemoteConfig.getInstance().getString(Constants.Firebase.RemoteConfigKeys.VK_APP_GROUP_ID);
        mApiClient.isUserJoinedVkGroup(appVkGroupId).subscribe(
                isJoinedAppVkGroup -> mMyPreferencesManager.setAppVkGroupJoined(isJoinedAppVkGroup),
                Timber::e
        );
    }

    public static int getTotalScoreToAddFromAction(@ScoreAction String action, MyPreferenceManager mMyPreferencesManager) {
        long score;

        //switch by action to get initial score value
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        switch (action) {
            case ScoreAction.FAVORITE:
                score = remoteConfig.getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_FAVORITE);
                break;
            case ScoreAction.READ:
                score = remoteConfig.getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_READ);
                break;
            case ScoreAction.INTERSTITIAL_SHOWN:
                score = remoteConfig.getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_INTERSTITIAL_SHOWN);
                break;
            case ScoreAction.REWARDED_VIDEO:
                score = remoteConfig.getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_REWARDED_VIDEO);
                break;
            case ScoreAction.VK_GROUP:
                score = remoteConfig.getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_VK_GROUP);
                break;
            case ScoreAction.OUR_APP:
                score = remoteConfig.getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_OUR_APP);
                break;
            case ScoreAction.AUTH:
                score = remoteConfig.getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_AUTH);
                break;
            case ScoreAction.INVITE:
                score = remoteConfig.getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_INVITE);
                break;
            case ScoreAction.NONE:
                score = remoteConfig.getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_NONE);
                break;
            default:
                throw new RuntimeException("unexpected score action");
        }

        double subscriptionModificator = remoteConfig.getDouble(Constants.Firebase.RemoteConfigKeys.SCORE_MULTIPLIER_SUBSCRIPTION);
        double vkGroupAppModificator = remoteConfig.getDouble(Constants.Firebase.RemoteConfigKeys.SCORE_MULTIPLIER_VK_GROUP_APP);
        Timber.d("subscriptionModificator/vkGroupAppModificator: %s/%s", subscriptionModificator, vkGroupAppModificator);

        boolean hasSubscriptionModificator = mMyPreferencesManager.isHasSubscription();
        boolean hasVkGroupAppModificator = mMyPreferencesManager.isAppVkGroupJoined();
        Timber.d("hasSubscriptionModificator/hasVkGroupAppModificator: %s/%s", hasSubscriptionModificator, hasVkGroupAppModificator);

        subscriptionModificator = hasSubscriptionModificator ? subscriptionModificator : 1;
        vkGroupAppModificator = hasVkGroupAppModificator ? vkGroupAppModificator : 1;
        Timber.d("subscriptionModificator/vkGroupAppModificator: %s/%s", subscriptionModificator, vkGroupAppModificator);
        //check if user has subs and joined vk group to add multilplier

        int totalScoreToAdd = (int) (score * subscriptionModificator * vkGroupAppModificator);
        Timber.d("totalScoreToAdd: %s", totalScoreToAdd);
        return totalScoreToAdd;
    }
}