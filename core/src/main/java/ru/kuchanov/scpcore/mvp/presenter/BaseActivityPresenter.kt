package ru.kuchanov.scpcore.mvp.presenter

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
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
import ru.kuchanov.scpcore.Constants.LEVEL_UP_SCORE_TO_ADD
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.api.ApiClient
import ru.kuchanov.scpcore.api.error.ScpLoginException
import ru.kuchanov.scpcore.api.model.firebase.ArticleInFirebase
import ru.kuchanov.scpcore.api.model.firebase.FirebaseObjectUser
import ru.kuchanov.scpcore.api.model.scpreader.CommonUserData
import ru.kuchanov.scpcore.db.DbProviderFactory
import ru.kuchanov.scpcore.db.model.SocialProviderModel
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.model.Item
import ru.kuchanov.scpcore.monetization.model.PurchaseData
import ru.kuchanov.scpcore.monetization.model.Subscription
import ru.kuchanov.scpcore.monetization.util.InappPurchaseUtil
import ru.kuchanov.scpcore.monetization.util.ItemsListWrapper
import ru.kuchanov.scpcore.monetization.util.PurchaseFailedError
import ru.kuchanov.scpcore.monetization.util.SubscriptionWrapper
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper
import ru.kuchanov.scpcore.mvp.base.BaseActivityMvp
import ru.kuchanov.scpcore.mvp.base.BasePresenter
import ru.kuchanov.scpcore.ui.activity.BaseActivity.RC_SIGN_IN
import ru.kuchanov.scpcore.ui.activity.BaseDrawerActivity.REQUEST_CODE_INAPP
import ru.kuchanov.scpcore.ui.fragment.monetization.SubscriptionsFragment.Companion.REQUEST_CODE_SUBSCRIPTION
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.subscribeBy
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.*

/**
 * Created by mohax on 23.03.2017.
 */
abstract class BaseActivityPresenter<V : BaseActivityMvp.View>(
        myPreferencesManager: MyPreferenceManager,
        dbProviderFactory: DbProviderFactory,
        apiClient: ApiClient,
        inAppHelper: InAppHelper
) : BasePresenter<V>(
        myPreferencesManager,
        dbProviderFactory,
        apiClient, inAppHelper
), BaseActivityMvp.Presenter<V> {

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
                                    onSuccess = { Timber.d("articles in realm updated!") },
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
                                    onSuccess = { Timber.d("score in realm updated!") },
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

        //update MyNativeBanners
        updateMyNativeBanners()
    }

    /**
     * @param provider login provider to use to login to firebase
     */
    override fun startFirebaseLogin(provider: Constants.Firebase.SocialProvider, id: String) {
        view.showProgressDialog(R.string.login_in_progress_custom_token)
        mApiClient.getAuthInFirebaseWithSocialProviderObservable(provider, id)
                .doOnSuccess { Timber.d("onSuccess after getAuthInFirebaseWithSocialProviderObservable: $it") }
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
                                .flatMap { Single.just(firebaseAuth.currentUser) }
                    } else {
                        Single.just(firebaseUser)
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
                                                    .flatMap { Single.just(FirebaseAuth.getInstance().currentUser) }
                                        } else {
                                            Single.just(it)
                                        }
                                    }
                                    .flatMap { mApiClient.userObjectFromFirebaseObservable }
                                    .flatMap {
                                        Single.error<FirebaseObjectUser>(
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
                                        .flatMap { Single.just(FirebaseAuth.getInstance().currentUser) }
                            } else {
                                Single.just(firebaseUser)
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
                                        .flatMap { Single.just(FirebaseAuth.getInstance().currentUser) }
                            } else {
                                Single.just(firebaseUser)
                            }
                                    .flatMap { mApiClient.userObjectFromFirebaseObservable }
                                    .flatMap { mApiClient.updateFirebaseUsersSocialProvidersObservable(userObjectInFirebase.socialProviders) }
                                    .flatMap<FirebaseObjectUser> { Single.just(userObjectInFirebase) }
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
                                        .flatMap { Single.just(FirebaseAuth.getInstance().currentUser) }
                            } else {
                                Single.just(firebaseUser)
                            }
                                    .flatMap { mApiClient.userObjectFromFirebaseObservable }
                                    .flatMap { mApiClient.incrementScoreInFirebase(score) }
                                    .flatMap<Boolean> { mApiClient.setUserRewardedForAuthInFirebaseObservable() }
                                    .flatMap {
                                        userObjectInFirebase.score += score
                                        userObjectInFirebase.signInRewardGained = true
                                        Single.just(userObjectInFirebase)
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
                                    .flatMap { Single.just(FirebaseAuth.getInstance().currentUser) }
                        } else {
                            Single.just(firebaseUser)
                        }
                                .flatMap { mApiClient.userObjectFromFirebaseObservable }
                                .flatMap { Single.just(userObjectInFirebase) }
                    }
                }
                //save user articles to realm
                .flatMap { userObjectInFirebase ->
                    if (userObjectInFirebase.articles == null)
                        Single.just(userObjectInFirebase)
                    else
                        mDbProviderFactory.dbProvider
                                .saveArticlesFromFirebase(ArrayList(userObjectInFirebase.articles.values))
                                .flatMap { Single.just(userObjectInFirebase) }
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
                    return@flatMap mApiClient
                            .loginToScpReaderServer(
                                    provider,
                                    stringWithDataForProvider
                            )
                            .doOnSuccess {
                                myPreferencesManager.apply {
                                    accessToken = it.accessToken
                                    refreshToken = it.refreshToken
                                }
                            }
//                            .toObservable()
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
                                                        e.message
                                                )
                                        )
                                )
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

    override fun onPurchaseClick(
            id: String,
            ignoreUserCheck: Boolean
    ) {
        Timber.d("onPurchaseClick: %s, %s", id, ignoreUserCheck)
        //show warning if user not logged in
        if (!ignoreUserCheck && user == null) {
            view.showOfferLoginForLevelUpPopup()
            return
        }

        @InappPurchaseUtil.InappType
        val type = if (inAppHelper.getNewInAppsSkus().contains(id)) {
            InappPurchaseUtil.InappType.IN_APP
        } else {
            InappPurchaseUtil.InappType.SUBS
        }

        inAppHelper.intentSenderSingle(type, id)
                .flatMap { inAppHelper.startPurchase(it) }
                .onErrorResumeNext { error ->
                    return@onErrorResumeNext if (error is PurchaseFailedError) {
                        Timber.e(error, "error is PurchaseFailedError")
                        when (type) {
                            InappPurchaseUtil.InappType.IN_APP -> {
                                inAppHelper
                                        .getInAppHistory()
//                                        .doOnSubscribe { view.showProgressDialog(R.string.wait) }
//                                        .doOnEach { view.dismissProgressDialog() }
                                        .flatMap { inAppHelper.consumeInApp(it.productId, "") }
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .doOnSuccess {
                                            val context = BaseApplication.getAppInstance()
                                            view.showMessage(
                                                    context.getString(
                                                            R.string.score_increased,
                                                            context.resources.getQuantityString(
                                                                    R.plurals.plurals_score,
                                                                    LEVEL_UP_SCORE_TO_ADD,
                                                                    LEVEL_UP_SCORE_TO_ADD
                                                            )
                                                    )
                                            )
                                        }
                                        .flatMap { inAppHelper.intentSenderSingle(type, id) }
                                        .flatMap {
                                            inAppHelper.startPurchase(it)
                                        }
                            }
                            InappPurchaseUtil.InappType.SUBS -> {
                                inAppHelper
                                        .validateSubsObservable()
                                        .doOnSuccess { view.showMessage("Subscriptions state updated") }
                                        .flatMap { Single.error<Subscription>(error) }
                            }
                            else -> throw error
                        }
                    } else {
                        throw error
                    }
                }
                .flatMap { subscription ->
                    if (subscription.type == InappPurchaseUtil.InappType.IN_APP) {
                        inAppHelper
                                .consumeInApp(subscription.productId, "")
                                .map { subscription }
                    } else {
                        Single.just(subscription)
                    }
                }
                .doOnSuccess { subscription ->
                    if (subscription.type == InappPurchaseUtil.InappType.SUBS) {
                        view.updateOwnedMarketItems()
                    } else if (subscription.type == InappPurchaseUtil.InappType.IN_APP) {
                        val context = BaseApplication.getAppInstance()
                        view.showMessage(
                                context.getString(
                                        R.string.score_increased,
                                        context.resources.getQuantityString(
                                                R.plurals.plurals_score,
                                                LEVEL_UP_SCORE_TO_ADD,
                                                LEVEL_UP_SCORE_TO_ADD
                                        )
                                )
                        )
                        if (!myPreferencesManager.isHasAnySubscription) {
                            view.showOfferSubscriptionPopup()
                        }
                    }
                }
                .subscribeBy(
                        onSuccess = {},
                        onError = { e ->
                            Timber.e(e, "error while purchase clicked")
                            view.showError(e)
                        }
                )
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
                                Constants.Firebase.SocialProvider.VK,
                                VKAccessToken.currentToken().accessToken
                        )
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
        } else if (requestCode == REQUEST_CODE_SUBSCRIPTION) {
            if (data == null) {
                view.showMessageLong("Error while parse result, please try again")
                return true
            }
            val responseCode = data.getIntExtra("RESPONSE_CODE", 0)
            val purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA")

            if (resultCode == Activity.RESULT_OK && responseCode == InappPurchaseUtil.RESULT_OK) {
                try {
                    val jo = JSONObject(purchaseData)
                    val sku = jo.getString("productId")
                    Timber.d("You have bought the %s", sku)

                    inAppHelper.getOwnedSubsRelay()
                            .call(ItemsListWrapper(listOf(Item(sku = sku))))
                } catch (e: JSONException) {
                    Timber.e(e, "Failed to parse purchase data.")
                    inAppHelper.getOwnedSubsRelay()
                            .call(
                                    ItemsListWrapper(
                                            error = Error("Failed to parse purchase data.", e)
                                    )
                            )
                }
            } else {
                inAppHelper.getOwnedSubsRelay()
                        .call(
                                ItemsListWrapper(
                                        error = Error("Error: response code is not \"0\". Please try again")
                                )
                        )
            }
            return true
        } else if (requestCode == REQUEST_CODE_INAPP) {
            Timber.d("REQUEST_CODE_INAPP resultCode == Activity.RESULT_OK: ${resultCode == Activity.RESULT_OK}")

            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    Timber.e("error_inapp data is NULL")

                    inAppHelper.getBoughtInappRelay()
                            .call(
                                    SubscriptionWrapper(
                                            error = Error(BaseApplication.getAppInstance().getString(R.string.error_inapp))
                                    )
                            )
                    return true
                }
                //int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
                val purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA")
                //String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
                Timber.d("purchaseData %s", purchaseData)
                val item = GsonBuilder().create().fromJson(purchaseData, PurchaseData::class.java)
                Timber.d("You have bought the %s", item.productId)

                if (item.productId == inAppHelper.getNewInAppsSkus().first()) {
                    inAppHelper.getBoughtInappRelay().call(
                            SubscriptionWrapper(
                                    Subscription(
                                            item.productId,
                                            InappPurchaseUtil.InappType.IN_APP,
                                            "N/A", //receipt.price,
                                            -1,//price_amount_micros
                                            "N/A", //price_currency_code
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            0,
                                            null,
                                            0,
                                            item.purchaseToken
                                    )
                            )
                    )
                } else {
                    val message = "Unexpected item.productId: ${item.productId}"
                    Timber.wtf(message)
                    inAppHelper.getBoughtInappRelay()
                            .call(
                                    SubscriptionWrapper(
                                            error = Error(message)
                                    )
                            )
                }
            } else if (data?.getIntExtra("RESPONSE_CODE", 0) == InappPurchaseUtil.RESULT_ITEM_ALREADY_OWNED) {
                val message = "RESPONSE_CODE is: InAppHelper.RESULT_ITEM_ALREADY_OWNED"
                Timber.wtf(message)
                inAppHelper.getBoughtInappRelay()
                        .call(
                                SubscriptionWrapper(
                                        error = PurchaseFailedError(message)
                                )
                        )
            } else {
                val extras = data?.extras
                val keySet = extras?.keySet()
                val message = "Unexpected resultCode: $resultCode/${keySet?.map { "$it/${extras[it]}" }}"
                inAppHelper.getBoughtInappRelay()
                        .call(
                                SubscriptionWrapper(
                                        error = Error(message)
                                )
                        )
            }
            return true
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
            return true
        }
    }
}
