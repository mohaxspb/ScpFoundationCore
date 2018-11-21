package ru.kuchanov.scpcore.mvp.presenter

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.appinvite.AppInviteInvitation
import com.google.android.gms.auth.api.Auth
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.GsonBuilder
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import org.json.JSONException
import org.json.JSONObject
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.api.error.ScpLoginException
import ru.kuchanov.scpcore.api.model.firebase.ArticleInFirebase
import ru.kuchanov.scpcore.api.model.firebase.FirebaseObjectUser
import ru.kuchanov.scpcore.api.model.scpreader.CommonUserData
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.db.model.SocialProviderModel
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.model.PurchaseData
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper
import ru.kuchanov.scpcore.mvp.base.BaseActivityMvp
import ru.kuchanov.scpcore.mvp.base.BasePresenter
import ru.kuchanov.scpcore.ui.activity.BaseActivity.RC_SIGN_IN
import ru.kuchanov.scpcore.ui.activity.BaseDrawerActivity
import ru.kuchanov.scpcore.ui.fragment.monetization.SubscriptionsFragment
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
        apiClient: ApiClient,
        private val inAppHelper: InAppHelper
) : BasePresenter<V>(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper), BaseActivityMvp.Presenter<V> {

    //facebook
    private val callbackManager: CallbackManager = CallbackManager.Factory.create()

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val authListener = { mAuth: FirebaseAuth ->
        val firebaseUser = mAuth.currentUser
        if (firebaseUser != null) {
            // User is signed in
            Timber.d("onAuthStateChanged:signed_in: %s", firebaseUser.uid)
            listenToChangesInFirebase(myPreferencesManager.isHasSubscription)
        } else {
            // User is signed out
            Timber.d("onAuthStateChanged: signed_out")
            listenToChangesInFirebase(false)
        }
    }

    private var firebaseArticlesRef: DatabaseReference? = null
    private var firebaseScoreRef: DatabaseReference? = null

    private val articlesChangeListener: ValueEventListener by lazy {
        object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Timber.d("articles in user changed!")
                val t = object : GenericTypeIndicator<Map<String, @JvmSuppressWildcards ArticleInFirebase>>() {

                }
                val map: Map<String, @JvmSuppressWildcards ArticleInFirebase>? = dataSnapshot.getValue(t)

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
    }

    private val scoreChangeListener: ValueEventListener by lazy {
        object : ValueEventListener {
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
    }

    override fun onCreate() {
        super.onCreate()

        //facebook login
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Timber.d("onSuccess: %s", loginResult)
                startFirebaseLogin(Constants.Firebase.SocialProvider.FACEBOOK, loginResult.accessToken.token)
            }

            override fun onCancel() {
                Timber.e("onCancel")
            }

            override fun onError(error: FacebookException) {
                Timber.e(error)
            }
        })
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
                        Observable.just(firebaseUser)
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
                                                    .flatMap { Observable.just(FirebaseAuth.getInstance().currentUser) }
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
                            myPreferencesManager.applyAwardSignIn()

                            val score = FirebaseRemoteConfig.getInstance()
                                    .getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_AUTH).toInt()
                            userToWriteToDb.score += score

                            userToWriteToDb.signInRewardGained = true

                            if (TextUtils.isEmpty(firebaseUser.email)) {
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
                                Observable.just(firebaseUser)
                            }
                                    .flatMap { mApiClient.userObjectFromFirebaseObservable }
                                    .flatMap { mApiClient.writeUserToFirebaseObservable(userToWriteToDb) }
                        }
                    } else {
                        val firebaseUser = FirebaseAuth.getInstance().currentUser!!
                        val socialProviderModel: SocialProviderModel = SocialProviderModel
                                .getSocialProviderModelForProvider(provider)
                        if (!userObjectInFirebase.socialProviders.contains(socialProviderModel)) {
                            Timber.d("User does not contains provider info: %s", provider)
                            userObjectInFirebase.socialProviders.add(socialProviderModel)

                            return@flatMap if (TextUtils.isEmpty(firebaseUser.email)) {
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
                                    .flatMap { mApiClient.userObjectFromFirebaseObservable }
                                    .flatMap { mApiClient.updateFirebaseUsersSocialProvidersObservable(userObjectInFirebase.socialProviders) }
                                    .flatMap<FirebaseObjectUser> { Observable.just(userObjectInFirebase) }
                        }

                        //in case of unrewarded user we should add score and disable ads temporary too
                        if (!userObjectInFirebase.signInRewardGained) {
                            myPreferencesManager.applyAwardSignIn()

                            val score = FirebaseRemoteConfig.getInstance()
                                    .getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_AUTH).toInt()

                            return@flatMap if (TextUtils.isEmpty(firebaseUser.email)) {
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
                                    .flatMap { mApiClient.userObjectFromFirebaseObservable }
                                    .flatMap { mApiClient.incrementScoreInFirebaseObservable(score) }
                                    .flatMap<Boolean> { mApiClient.setUserRewardedForAuthInFirebaseObservable() }
                                    .flatMap {
                                        userObjectInFirebase.score += score
                                        userObjectInFirebase.signInRewardGained = true
                                        Observable.just(userObjectInFirebase)
                                    }
                        }

                        return@flatMap if (TextUtils.isEmpty(firebaseUser.email)) {
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
                .observeOn(Schedulers.io())
                .flatMap { userInRealm ->
                    val stringWithDataForProvider = if (provider == Constants.Firebase.SocialProvider.VK) {
                        val commonUserDataFromVk = CommonUserData(
                                id = VKAccessToken.currentToken().userId,
                                email = VKAccessToken.currentToken().email,
                                avatarUrl = userInRealm.avatar,
                                fullName = userInRealm.fullName
                        )
                        mApiClient.gson.toJson(commonUserDataFromVk)
                    } else {
                        id
                    }
                    mApiClient.loginToScpReaderServer(
                            provider,
                            stringWithDataForProvider
                    )
                            .doOnSuccess {
                                myPreferencesManager.apply {
                                    accessToken = it.accessToken
                                    refreshToken = it.refreshToken
                                }
                            }
                            .toObservable()
                            .map { userInRealm }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { userInRealm ->
                            Timber.d("user saved")
                            view.dismissProgressDialog()
                            view.showMessage(
                                    BaseApplication.getAppInstance().getString(
                                            R.string.on_user_logined,
                                            userInRealm.fullName))
                        },
                        { e ->
                            Timber.e(e, "error while save user to DB")
                            logoutUser()
                            view.dismissProgressDialog()
                            if (e is FirebaseAuthUserCollisionException) {
                                view.showError(ScpLoginException(BaseApplication.getAppInstance().getString(R.string.error_login_firebase_user_collision)))
                            } else {
                                view.showError(
                                        ScpLoginException(
                                                BaseApplication.getAppInstance().getString(
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
        firebaseAuth.addAuthStateListener(authListener)

        listenToChangesInFirebase(myPreferencesManager.isHasSubscription)
    }

    override fun onActivityStopped() {
        firebaseAuth.removeAuthStateListener(authListener)

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
        mApiClient.inviteReceived(inviteId, !myPreferencesManager.isInviteAlreadyReceived)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = { Timber.d("invite id successfully sent to server") },
                        onError = { Timber.e(it) }
                )
    }

    override fun onInviteSent(inviteId: String) {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            Timber.d("onInviteSent: ${it.result?.token}")
            mApiClient.inviteSent(inviteId, it.result?.token)
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        Timber.d("onActivityResult: $requestCode, $resultCode, $data")

        val vkCallback = object : VKCallback<VKAccessToken> {
            override fun onResult(vkAccessToken: VKAccessToken?) {
                //Пользователь успешно авторизовался
                if (vkAccessToken?.email != null) {
                    Timber.d("Auth successful: %s", vkAccessToken.email)
                    //here can be case, when we login via Google or Facebook, but try to join group to receive reward
                    //in this case we have firebase user already, so no need to login to firebase
                    if (FirebaseAuth.getInstance().currentUser != null) {
                        Timber.e("Firebase user exists, do nothing as we do not implement connect VK acc to Firebase as social provider")
                    } else {
                        startFirebaseLogin(
                                Constants.Firebase.SocialProvider.VK, VKAccessToken.currentToken().accessToken)
                    }
                } else {
                    view.showMessage(R.string.error_login_no_email)
                    logoutUser()
                }
            }

            override fun onError(error: VKError?) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                val errorMessage = if (error == null) {
                    BaseApplication.getAppInstance().getString(R.string.error_unexpected)
                } else {
                    error.errorMessage
                }
                Timber.e("error/errMsg: %s/%s", error, errorMessage)
                view.showMessage(errorMessage)
            }
        }

        if (VKSdk.onActivityResult(requestCode, resultCode, data, vkCallback)) {
            Timber.d("Vk receives and handled onActivityResult")
            return true
        } else if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result == null) {
                Timber.wtf("GoogleSignInResult is NULL!!!")
                view.showMessage(R.string.error_unexpected)
                return true
            }
            if (result.isSuccess) {
                Timber.d("Auth successful: %s", result)
                // Signed in successfully, show authenticated UI.
                val acct = result.signInAccount
                if (acct == null) {
                    Timber.wtf("GoogleSignInAccount is NULL!")
                    view.showMessage("GoogleSignInAccount is NULL!")
                    return true
                }
                val email = acct.email
                if (email?.isNotEmpty() == true) {
                    startFirebaseLogin(Constants.Firebase.SocialProvider.GOOGLE, acct.idToken!!)
                } else {
                    view.showMessage(R.string.error_login_no_email)
                    logoutUser()
                }
                return true
            } else {
                // Signed out, show unauthenticated UI.
                logoutUser()
                return true
            }
        } else if (requestCode == Constants.Firebase.REQUEST_INVITE) {
            if (resultCode == Activity.RESULT_OK) {
                // Get the invitation IDs of all sent messages
                val ids = AppInviteInvitation.getInvitationIds(resultCode, data!!)
                for (id in ids) {
                    Timber.d("onActivityResult: sent invitation %s", id)
                    //todo we need to be able to send multiple IDs in one request
                    onInviteSent(id)

                    FirebaseAnalytics.getInstance(BaseApplication.getAppInstance()).logEvent(
                            Constants.Firebase.Analitics.EventName.INVITE_SENT,
                            null
                    )
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                Timber.d("invitation failed for some reason")
            }

            return true
        } else if (requestCode == SubscriptionsFragment.REQUEST_CODE_SUBSCRIPTION) {
            if (data == null) {
                view.showMessageLong("Error while parse result, please try again")
                return true
            }
            val responseCode = data.getIntExtra("RESPONSE_CODE", 0)
            val purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA")

            if (resultCode == Activity.RESULT_OK && responseCode == InAppHelper.RESULT_OK) {
                try {
                    val jo = JSONObject(purchaseData)
                    val sku = jo.getString("productId")
                    Timber.d("You have bought the %s", sku)

                    //validate subs list
                    view.updateOwnedMarketItems()
                } catch (e: JSONException) {
                    Timber.e(e, "Failed to parse purchase data.")
                    view.showError(e)
                }
            } else {
                view.showMessageLong("Error: response code is not \"0\". Please try again")
            }
            return true
        } else if (requestCode == BaseDrawerActivity.REQUEST_CODE_INAPP) {
            Timber.d("REQUEST_CODE_INAPP resultCode == Activity.RESULT_OK: ${resultCode == Activity.RESULT_OK}")
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    Timber.d("error_inapp data is NULL")
                    view.showMessage(R.string.error_inapp)
                    return true
                }
                //            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
                val purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA")
                //            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
                Timber.d("purchaseData %s", purchaseData)
                val item = GsonBuilder().create().fromJson(purchaseData, PurchaseData::class.java)
                Timber.d("You have bought the %s", item.productId)

                if (item.productId == InAppHelper.getNewInAppsSkus().first()) {
                    //levelUp 5
                    //add 10 000 score
                    inAppHelper.consumeInApp(item.productId, item.purchaseToken, view.iInAppBillingService)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                    onSuccess = {
                                        Timber.d("consume inapp successful, so update user score")
                                        updateUserScoreForInapp(item.productId)

                                        if (!myPreferencesManager.isHasAnySubscription) {
                                            view.showOfferSubscriptionPopup()
                                        }
                                    },
                                    onError = {
                                        Timber.e(it, "error while consume inapp!")
                                        view.showError(it)
                                        view.showInAppErrorDialog(
                                                it.message
                                                        ?: BaseApplication.getAppInstance().getString(R.string.error_unexpected)
                                        )
                                    }
                            )
                } else {
                    Timber.wtf("Unexpected item.productId: ${item.productId}")
                }
            } else if (data?.getIntExtra("RESPONSE_CODE", 0) == InAppHelper.RESULT_ITEM_ALREADY_OWNED) {
                val message = "RESPONSE_CODE is: InAppHelper.RESULT_ITEM_ALREADY_OWNED"
                Timber.wtf(message)
                view.iInAppBillingService?.let { onLevelUpRetryClick(it) }
                        ?: view.showInAppErrorDialog(BaseApplication.getAppInstance().getString(R.string.error_unexpected))
            } else {
                val message = "Unexpected resultCode: $resultCode/${data?.extras?.keySet()?.map { "$it/${data.extras[it]}" }}"
                Timber.wtf(message)
                if (data?.getIntExtra("RESPONSE_CODE", 0) != InAppHelper.RESULT_USER_CANCELED) {
                    view.showMessage(message)
                }
            }
            return true
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
            return true
        }
    }
}