package ru.kuchanov.scpcore.mvp.presenter

import android.content.Intent
import android.text.TextUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.api.error.ScpLoginException
import ru.kuchanov.scpcore.api.model.firebase.ArticleInFirebase
import ru.kuchanov.scpcore.api.model.firebase.FirebaseObjectUser
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.db.model.SocialProviderModel
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.mvp.base.BaseActivityMvp
import ru.kuchanov.scpcore.mvp.base.BasePresenter
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.subscribeBy
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.*

/**
 * Created by mohax on 23.03.2017.
 *
 *
 * for scp_ru
 */
abstract class BaseActivityPresenter<V : BaseActivityMvp.View>(
    myPreferencesManager: MyPreferenceManager,
    dbProviderFactory: DbProviderFactory,
    apiClient: ApiClient
) : BasePresenter<V>(myPreferencesManager, dbProviderFactory, apiClient), BaseActivityMvp.Presenter<V> {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val mAuthListener = { mAuth: FirebaseAuth ->
        val firebaseUser = mAuth.currentUser
        if (firebaseUser != null) {
            // User is signed in
            Timber.d("onAuthStateChanged:signed_in: %s", firebaseUser.uid)
            listenToChangesInFirebase(mMyPreferencesManager.isHasSubscription)
        } else {
            // User is signed out
            Timber.d("onAuthStateChanged: signed_out")
            listenToChangesInFirebase(false)
        }
    }

    private var firebaseArticlesRef: DatabaseReference? = null
    private var firebaseScoreRef: DatabaseReference? = null

    private val articlesChangeListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            Timber.d("articles in user changed!")
            val t = object : GenericTypeIndicator<Map<String, ArticleInFirebase>>() {

            }
            val map = dataSnapshot.getValue(t)

            if (map != null) {
                mDbProviderFactory.dbProvider
                        .saveArticlesFromFirebase(ArrayList(map.values))
                        .subscribeBy(
                            onNext = { Timber.d("articles in realm updated!") },
                            onError = { Timber.e(it) }
                        )
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Timber.e(databaseError.toException())
        }
    }

    private val scoreChangeListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            Timber.d("score in user changed!")
            val score = dataSnapshot.getValue(Int::class.java)

            if (score != null) {
                mDbProviderFactory.dbProvider
                        .updateUserScore(score)
                        .subscribeBy(
                            onNext = { Timber.d("score in realm updated!") },
                            onError = { Timber.e(it) }
                        )
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Timber.e(databaseError.toException())
        }
    }

    /**
     * @param provider login provider to use to login to firebase
     */
    override fun startFirebaseLogin(provider: Constants.Firebase.SocialProvider, id: String) {
        view.showProgressDialog(R.string.login_in_progress_custom_token)
        mApiClient.getAuthInFirebaseWithSocialProviderObservable(provider, id)
                .flatMap<FirebaseUser> { firebaseUser ->
                    if (TextUtils.isEmpty(firebaseUser.email)) {
                        mApiClient.getAuthInFirebaseWithSocialProviderObservable(provider, id)
                                .flatMap { mApiClient.nameAndAvatarFromProviderObservable(provider) }
                                .flatMap { nameAvatar ->
                                    mApiClient.updateFirebaseUsersNameAndAvatarObservable(
                                        nameAvatar.first,
                                        nameAvatar.second
                                    )
                                }
                                .flatMap { mApiClient.updateFirebaseUsersEmailObservable() }
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .flatMap { Observable.just(firebaseAuth.currentUser) }
                    } else {
                        mApiClient.getAuthInFirebaseWithSocialProviderObservable(provider, id)
                                .flatMap { Observable.just(firebaseUser) }
                    }
                }
                .flatMap { mApiClient.userObjectFromFirebaseObservable }
                .flatMap<FirebaseObjectUser> { userObjectInFirebase ->
                    if (userObjectInFirebase == null) {
                        Timber.d("there is no User object in firebase database, so create new one")
                        val firebaseUser = FirebaseAuth.getInstance().currentUser
                        if (firebaseUser == null) {
                            mApiClient.getAuthInFirebaseWithSocialProviderObservable(provider, id)
                                    .flatMap {
                                        if (TextUtils.isEmpty(it.email)) {
                                            mApiClient.nameAndAvatarFromProviderObservable(provider)
                                                    .flatMap { nameAvatar ->
                                                        mApiClient.updateFirebaseUsersNameAndAvatarObservable(
                                                            nameAvatar.first,
                                                            nameAvatar.second
                                                        )
                                                    }
                                                    .flatMap { mApiClient.updateFirebaseUsersEmailObservable() }
                                                    .subscribeOn(AndroidSchedulers.mainThread())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .flatMap { aVoid -> Observable.just(FirebaseAuth.getInstance().currentUser) }
                                        } else {
                                            Observable.just(it)
                                        }
                                    }
                                    .flatMap { mApiClient.userObjectFromFirebaseObservable }
                                    .flatMap {
                                        Observable.error<FirebaseObjectUser>(
                                            ScpLoginException(
                                                BaseApplication.getAppInstance()
                                                        .getString(
                                                            R.string.error_login_firebase_connection,
                                                            "firebase user is null")))
                                    }
                        } else {
                            val userToWriteToDb = FirebaseObjectUser()
                            userToWriteToDb.uid = firebaseUser.uid
                            userToWriteToDb.fullName = firebaseUser.displayName
                            if (firebaseUser.photoUrl != null) {
                                userToWriteToDb.avatar = firebaseUser.photoUrl!!.toString()
                            }
                            userToWriteToDb.email = firebaseUser.email
                            userToWriteToDb.socialProviders = ArrayList()
                            val socialProviderModel = SocialProviderModel.getSocialProviderModelForProvider(provider)
                            userToWriteToDb.socialProviders.add(socialProviderModel)

                            //in case of first login we should add score and disable ads temporary
                            mMyPreferencesManager.applyAwardSignIn()

                            val score = FirebaseRemoteConfig.getInstance()
                                    .getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_AUTH).toInt()
                            userToWriteToDb.score += score

                            userToWriteToDb.signInRewardGained = true

                            mApiClient.getAuthInFirebaseWithSocialProviderObservable(provider, id)
                                    .flatMap {
                                        if (TextUtils.isEmpty(it.email)) {
                                            mApiClient.nameAndAvatarFromProviderObservable(provider)
                                                    .flatMap { nameAvatar ->
                                                        mApiClient.updateFirebaseUsersNameAndAvatarObservable(
                                                            nameAvatar.first,
                                                            nameAvatar.second
                                                        )
                                                    }
                                                    .flatMap { mApiClient.updateFirebaseUsersEmailObservable() }
                                                    .subscribeOn(AndroidSchedulers.mainThread())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .flatMap { Observable.just(FirebaseAuth.getInstance().currentUser) }
                                        } else {
                                            Observable.just(it)
                                        }
                                    }
                                    .doOnNext {
                                        Timber.d(
                                            "firebaseUser: %s, %s, %s, %s",
                                            it?.uid,
                                            it?.email,
                                            it?.photoUrl,
                                            it?.displayName
                                        )
                                    }
                                    .flatMap { mApiClient.userObjectFromFirebaseObservable }
                                    .flatMap { mApiClient.writeUserToFirebaseObservable(userToWriteToDb) }
                        }
                    } else {
                        val socialProviderModel = SocialProviderModel.getSocialProviderModelForProvider(provider)
                        if (!userObjectInFirebase.socialProviders.contains(socialProviderModel)) {
                            Timber.d("User does not contains provider info: %s", provider)
                            //                            socialProviderModel.id = id;
                            userObjectInFirebase.socialProviders.add(socialProviderModel)
                            return@flatMap mApiClient.getAuthInFirebaseWithSocialProviderObservable(provider, id)
                                    .flatMap { firebaseUser ->
                                        if (TextUtils.isEmpty(firebaseUser.email)) {
                                            mApiClient.nameAndAvatarFromProviderObservable(provider)
                                                    .flatMap { nameAvatar ->
                                                        mApiClient.updateFirebaseUsersNameAndAvatarObservable(
                                                            nameAvatar.first,
                                                            nameAvatar.second)
                                                    }
                                                    .flatMap { mApiClient.updateFirebaseUsersEmailObservable() }
                                                    .subscribeOn(AndroidSchedulers.mainThread())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .flatMap { Observable.just(FirebaseAuth.getInstance().currentUser) }
                                        } else {
                                            Observable.just(firebaseUser)
                                        }
                                    }
                                    .flatMap { mApiClient.userObjectFromFirebaseObservable }
                                    .flatMap { mApiClient.updateFirebaseUsersSocialProvidersObservable(userObjectInFirebase.socialProviders) }
                                    .flatMap<FirebaseObjectUser> { Observable.just(userObjectInFirebase) }
                        }

                        //in case of unrewarded user we should add score and disable ads temporary too
                        if (!userObjectInFirebase.signInRewardGained) {
                            mMyPreferencesManager.applyAwardSignIn()

                            val score = FirebaseRemoteConfig.getInstance()
                                    .getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_AUTH).toInt()

                            return@flatMap mApiClient.getAuthInFirebaseWithSocialProviderObservable(provider, id)
                                    .flatMap { firebaseUser ->
                                        if (TextUtils.isEmpty(firebaseUser.email)) {
                                            mApiClient.nameAndAvatarFromProviderObservable(provider)
                                                    .flatMap { nameAvatar ->
                                                        mApiClient.updateFirebaseUsersNameAndAvatarObservable(
                                                            nameAvatar.first,
                                                            nameAvatar.second)
                                                    }
                                                    .flatMap { mApiClient.updateFirebaseUsersEmailObservable() }
                                                    .subscribeOn(AndroidSchedulers.mainThread())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .flatMap { Observable.just(FirebaseAuth.getInstance().currentUser) }
                                        } else {
                                            Observable.just(firebaseUser)
                                        }
                                    }
                                    .doOnNext { firebaseUser ->
                                        Timber.d(
                                            "firebaseUser: %s, %s, %s, %s",
                                            firebaseUser?.uid,
                                            firebaseUser?.email,
                                            firebaseUser?.photoUrl,
                                            firebaseUser?.displayName
                                        )
                                    }
                                    .flatMap { mApiClient.userObjectFromFirebaseObservable }
                                    .flatMap { mApiClient.incrementScoreInFirebaseObservable(score) }
                                    .flatMap<Boolean> { mApiClient.setUserRewardedForAuthInFirebaseObservable() }
                                    .flatMap {
                                        userObjectInFirebase.score += score
                                        userObjectInFirebase.signInRewardGained = true
                                        Observable.just(userObjectInFirebase)
                                    }
                        }

                        return@flatMap mApiClient.getAuthInFirebaseWithSocialProviderObservable(provider, id)
                                .flatMap { firebaseUser ->
                                    if (TextUtils.isEmpty(firebaseUser.email)) {
                                        mApiClient.nameAndAvatarFromProviderObservable(provider)
                                                .flatMap { nameAvatar ->
                                                    mApiClient.updateFirebaseUsersNameAndAvatarObservable(
                                                        nameAvatar.first,
                                                        nameAvatar.second)
                                                }
                                                .flatMap { mApiClient.updateFirebaseUsersEmailObservable() }
                                                .subscribeOn(AndroidSchedulers.mainThread())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .flatMap { Observable.just(FirebaseAuth.getInstance().currentUser) }
                                    } else {
                                        Observable.just(firebaseUser)
                                    }
                                }
                                .doOnNext { firebaseUser ->
                                    Timber.d(
                                        "firebaseUser: %s, %s, %s, %s",
                                        firebaseUser?.uid,
                                        firebaseUser?.email,
                                        firebaseUser?.photoUrl,
                                        firebaseUser?.displayName
                                    )
                                }
                                .flatMap { mApiClient.userObjectFromFirebaseObservable }
                                .flatMap { Observable.just<FirebaseObjectUser>(userObjectInFirebase) }
                    }
                }
                //save user articles to realm
                .flatMap { userObjectInFirebase ->
                    if (userObjectInFirebase.articles == null)
                        Observable.just(userObjectInFirebase)
                    else
                        mDbProviderFactory.dbProvider
                                .saveArticlesFromFirebase(ArrayList(userObjectInFirebase.articles.values))
                                .flatMap { Observable.just(userObjectInFirebase) }
                }
                //save user to realm
                .flatMap { userObjectInFirebase -> mDbProviderFactory.dbProvider.saveUser(userObjectInFirebase.toRealmUser()) }
                .subscribe(
                    { userInRealm ->
                        Timber.d("user saved")
                        view.dismissProgressDialog()
                        view.showMessage(
                            BaseApplication.getAppInstance()
                                    .getString(R.string.on_user_logined, userInRealm.fullName))
                    },
                    { e ->
                        Timber.e(e, "error while save user to DB")
                        logoutUser()
                        view.dismissProgressDialog()
                        if (e is FirebaseAuthUserCollisionException) {
                            view.showError(
                                ScpLoginException(
                                    BaseApplication.getAppInstance()
                                            .getString(R.string.error_login_firebase_user_collision)))
                        } else {
                            view.showError(
                                ScpLoginException(
                                    BaseApplication.getAppInstance()
                                            .getString(
                                                R.string.error_login_firebase_connection,
                                                e.message)))
                        }
                    }
                )
    }

    override fun logoutUser() {
        mDbProviderFactory.dbProvider.logout().subscribe(
            { Timber.d("logout successful") },
            { e -> Timber.e(e, "error while logout user") }
        )
    }

    override fun onActivityStarted() {
        firebaseAuth.addAuthStateListener(mAuthListener)

        listenToChangesInFirebase(mMyPreferencesManager.isHasSubscription)
    }

    override fun onActivityStopped() {
        firebaseAuth.removeAuthStateListener(mAuthListener)

        listenToChangesInFirebase(false)
    }

    private fun listenToChangesInFirebase(listen: Boolean) {
        Timber.d("listenToChangesInFirebase: %s", listen)
        if (listen) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null && !TextUtils.isEmpty(firebaseUser.uid)) {
                if (firebaseArticlesRef != null) {
                    firebaseArticlesRef!!.removeEventListener(articlesChangeListener)
                }
                firebaseArticlesRef = FirebaseDatabase.getInstance().reference
                        .child(Constants.Firebase.Refs.USERS)
                        .child(firebaseUser.uid)
                        .child(Constants.Firebase.Refs.ARTICLES)

                firebaseArticlesRef!!.addValueEventListener(articlesChangeListener)

                if (firebaseScoreRef != null) {
                    firebaseScoreRef!!.removeEventListener(scoreChangeListener)
                }
                firebaseScoreRef = FirebaseDatabase.getInstance().reference
                        .child(Constants.Firebase.Refs.USERS)
                        .child(firebaseUser.uid)
                        .child(Constants.Firebase.Refs.SCORE)

                firebaseScoreRef!!.addValueEventListener(scoreChangeListener)
            } else {
                if (firebaseArticlesRef != null) {
                    firebaseArticlesRef!!.removeEventListener(articlesChangeListener)
                }
                if (firebaseScoreRef != null) {
                    firebaseScoreRef!!.removeEventListener(scoreChangeListener)
                }
            }
        } else {
            if (firebaseArticlesRef != null) {
                firebaseArticlesRef!!.removeEventListener(articlesChangeListener)
            }
            if (firebaseScoreRef != null) {
                firebaseScoreRef!!.removeEventListener(scoreChangeListener)
            }
        }
    }

    override fun onInviteReceived(inviteId: String) {
        //After invite receive we'll check if it's first time invite received and,
        //if so, send its ID to server, which will check for ID existing and will send push to sender and delete inviteID-pushID pair,
        //else we'll send to server command to delete IDs pair, to prevent collecting useless data.
        mApiClient.inviteReceived(inviteId, !mMyPreferencesManager.isInviteAlreadyReceived)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = { Timber.d("invite id successfully sent to server") },
                    onError = { Timber.e(it) }
                )
    }

    override fun onInviteSent(inviteId: String) {
        mApiClient.inviteSent(inviteId, FirebaseInstanceId.getInstance().token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = { Timber.d("invite id and fcmToken successfully sent to server") },
                    onError = { e ->
                        Timber.e(e)
                        view.showError(e)
                    }
                )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        Timber.d("onActivityResult: ")
        //todo
    }
}