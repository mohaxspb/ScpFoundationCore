package ru.kuchanov.scpcore.ui.fragment.monetization

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.Space
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import kotlinx.android.synthetic.main.fragment_leaderboard.*
import kotlinx.android.synthetic.main.fragment_leaderboard.view.*
import org.json.JSONException
import org.json.JSONObject
import ru.kuchanov.scpcore.BaseApplication
import ru.kuchanov.scpcore.Constants
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.DividerDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.LabelDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.leaderboard.LeaderboardDelegate
import ru.kuchanov.scpcore.controller.adapter.delegate.monetization.subscriptions.InAppDelegate
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard.LeaderboardUserViewModel
import ru.kuchanov.scpcore.db.model.LeaderboardUser
import ru.kuchanov.scpcore.manager.InAppBillingServiceConnectionObservable
import ru.kuchanov.scpcore.manager.MyPreferenceManager
import ru.kuchanov.scpcore.monetization.model.PurchaseData
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper
import ru.kuchanov.scpcore.mvp.contract.monetization.LEADERBOARD_REQUEST_LIMIT
import ru.kuchanov.scpcore.mvp.contract.monetization.LeaderboardContract
import ru.kuchanov.scpcore.mvp.presenter.monetization.LeaderboardPresenter
import ru.kuchanov.scpcore.ui.activity.BaseActivity
import ru.kuchanov.scpcore.ui.activity.BaseDrawerActivity
import ru.kuchanov.scpcore.ui.fragment.BaseFragment
import ru.kuchanov.scpcore.ui.holder.login.SocialLoginHolder
import ru.kuchanov.scpcore.ui.util.EndlessRecyclerViewScrollListener
import ru.kuchanov.scpcore.util.DimensionUtils
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.subscribeBy
import rx.schedulers.Schedulers
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


/**
 * Created by mohax on 22.01.2018.
 *
 * for ScpCore
 */
class LeaderboardFragment :
        BaseFragment<LeaderboardContract.View, LeaderboardContract.Presenter>(),
        LeaderboardContract.View {

    companion object {
        val simpleDateFormat = SimpleDateFormat("HH:mm dd.MM.yy", Locale.getDefault())

        fun newInstance(): LeaderboardFragment = LeaderboardFragment()
    }

    @Inject
    lateinit var mGson: Gson
    @Inject
    lateinit var mInAppHelper: InAppHelper
    @Inject
    lateinit var myPreferenceManager: MyPreferenceManager

    private lateinit var adapter: ListDelegationAdapter<List<MyListItem>>

    override fun getLayoutResId() = R.layout.fragment_leaderboard

    override fun callInjections() = BaseApplication.getAppComponent().inject(this)

    override fun initViews() {
        InAppBillingServiceConnectionObservable.getInstance().serviceStatusObservable.subscribe { connected ->
            if (!isAdded) {
                return@subscribe
            }
            if (connected && isAdded && activity is BaseActivity<*, *>) {
                getPresenter().inAppService = (activity as BaseActivity<*, *>).getIInAppBillingService()
                if (!getPresenter().isDataLoaded) {
                    getPresenter().loadInitialData()
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        fastScroller.setRecyclerView(recyclerView)

        val animator = recyclerView.itemAnimator
        if (animator is DefaultItemAnimator) {
            animator.supportsChangeAnimations = false
        } else if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
        recyclerView.itemAnimator?.changeDuration = 0

        swipeRefresh.setColorSchemeResources(R.color.zbs_color_red)
        swipeRefresh.setOnRefreshListener { mPresenter.updateLeaderboardFromApi(0) }

        val delegateManager = AdapterDelegatesManager<List<MyListItem>>()
        delegateManager.addDelegate(DividerDelegate())
        delegateManager.addDelegate(LabelDelegate())
        delegateManager.addDelegate(LeaderboardDelegate())
        delegateManager.addDelegate(InAppDelegate {
            when (it) {
                LeaderboardPresenter.APPODEAL_ID -> presenter.onRewardedVideoClick()
                else -> presenter.onSubscriptionClick(it, this)
            }
        })

        adapter = ListDelegationAdapter(delegateManager)
        recyclerView.adapter = adapter

        if (presenter.data.isEmpty()) {
            enableSwipeRefresh(false)
            getPresenter().loadInitialData()
        } else {
            showProgressCenter(false)
            presenter.apply { showData(data); onUserChanged(myUser); showUpdateDate(updateTime) }
        }

        refresh.setOnClickListener { getPresenter().loadInitialData() }
    }

    override fun showProgressCenter(show: Boolean) {
        progressContainer.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showData(data: List<MyListItem>) {
        adapter.items = data
        adapter.notifyDataSetChanged()
    }

    override fun showSwipeRefreshProgress(show: Boolean) {
        if (!isAdded) {
            return
        }
        if (!swipeRefresh.isRefreshing && !show) {
            return
        }
        swipeRefresh.setProgressViewEndTarget(false, DimensionUtils.getActionBarHeight(activity!!))
        swipeRefresh.isRefreshing = show
    }

    override fun enableSwipeRefresh(enable: Boolean) {
        swipeRefresh.isEnabled = enable
    }

    override fun showUser(myUser: LeaderboardUserViewModel?) {
        if (!isAdded) {
            return
        }
        Timber.d("showUser: $myUser")
        if (myUser == null) {
            val providers = ArrayList<Constants.Firebase.SocialProvider>(Arrays.asList<Constants.Firebase.SocialProvider>(*Constants.Firebase.SocialProvider.values()))
            if (!resources.getBoolean(R.bool.social_login_vk_enabled)) {
                providers.remove(Constants.Firebase.SocialProvider.VK)
            }
            val socialProviders = SocialLoginHolder.SocialLoginModel.getModels(providers)

            providersContainer.removeAllViews()
            val startView = Space(activity!!)
            providersContainer.addView(startView)
            (startView.layoutParams as LinearLayout.LayoutParams).weight = 1f
            val inflater = LayoutInflater.from(activity)
            for (loginModel in socialProviders) {
                val view = inflater.inflate(R.layout.view_social_login, providersContainer, false)
                providersContainer.addView(view)
                val holder = SocialLoginHolder(
                    view
                ) { baseActivity?.startLogin(loginModel.socialProvider) }
                holder.bind(loginModel)

                val endView = Space(activity!!)
                providersContainer.addView(endView)
                (endView.layoutParams as LinearLayout.LayoutParams).weight = 1f
            }

            userDataView.visibility = View.GONE
            loginView.visibility = View.VISIBLE
            return
        }
        val user = myUser.user
        Timber.d("user: $user")
        with(userDataView) {
            chartPlaceTextView.text = if (myUser.position == LeaderboardUserViewModel.POSITION_NONE) {
                "N/A"
            } else {
                (myUser.position + 1).toString()
            }

            Glide.with(context)
                    .load(user.avatar)
                    .asBitmap()
                    .centerCrop()
                    .error(R.mipmap.ic_launcher)
                    .into(object : BitmapImageViewTarget(avatarImageView) {
                        override fun setResource(resource: Bitmap) {
                            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.resources, resource)
                            circularBitmapDrawable.isCircular = true
                            avatarImageView.setImageDrawable(circularBitmapDrawable)
                        }
                    })

            nameTextView.text = user.fullName
            readArticlesCountTextView.text = if (user.numOfReadArticles == LeaderboardUser.READ_ARTICLES_COUNT_NONE) {
                "N/A"
            } else {
                context.getString(R.string.leaderboard_articles_read, user.numOfReadArticles)
            }

            userScoreTextView.text = user.score.toString()

            val levelViewModel = myUser.levelViewModel
            val level = myUser.levelViewModel.level
            levelNumTextView.text = level.id.toString()
            levelTextView.text = context.getString(R.string.level_num, level.id)
            if (levelViewModel.isMaxLevel) {
                maxLevelTextView.visibility = View.VISIBLE
                experienceProgressBar.max = 1
                experienceProgressBar.progress = 1
                expToNextLevelTextView.text = ""
            } else {
                maxLevelTextView.visibility = View.GONE
                experienceProgressBar.max = levelViewModel.nextLevelScore
                experienceProgressBar.progress = user.score - level.score
                expToNextLevelTextView.text = context.getString(R.string.score_num, levelViewModel.scoreToNextLevel)
            }
        }
        loginView.visibility = View.GONE
        userDataView.visibility = View.VISIBLE
    }

    override fun showUpdateDate(lastUpdated: Long) {
        baseActivity?.getSupportActionBar()?.apply {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = lastUpdated
            val refreshed = simpleDateFormat.format(calendar.time)

            subtitle = getString(R.string.refreshed, refreshed)
        }
    }

    override fun showOfferLoginForLevelUpPopup() {
        baseActivity?.apply {
            MaterialDialog.Builder(this)
                    .title(R.string.need_login)
                    .content(R.string.need_login_for_level_up_content)
                    .positiveText(R.string.authorize)
                    .onPositive { _, _ -> showLoginProvidersPopup() }
                    .negativeText(android.R.string.cancel)
                    .onNegative { dialog: MaterialDialog, _ -> dialog.dismiss() }
                    .build()
                    .show()
        }
    }

    override fun showRefreshButton(show: Boolean) {
        refresh.visibility = if (show) VISIBLE else GONE
    }

    override fun onRewardedVideoClick() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            baseActivity?.showOfferLoginPopup { _, _ -> baseActivity?.startRewardedVideoFlow() }
        } else {
            baseActivity?.startRewardedVideoFlow()
        }
    }

    override fun getToolbarTitle(): Int = R.string.leaderboard_activity_title

    override fun getToolbarTextColor(): Int = android.R.color.white

    /**
     * override it to change or disable endless scrolling behavior
     */
    override fun resetOnScrollListener() {
        recyclerView.clearOnScrollListeners()
        if (presenter.usersCount < LEADERBOARD_REQUEST_LIMIT || presenter.usersCount % LEADERBOARD_REQUEST_LIMIT != 0) {
            //so there is to less arts to be able to load from bottom
            //this can be if we receive few search results
            //si we just no need to set scrollListener
            return
        }
        recyclerView.addOnScrollListener(object : EndlessRecyclerViewScrollListener() {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                Timber.d("onLoadMode with page: %s, and offset: %s", page, view.adapter?.itemCount)
                showBottomProgress(true)
                mPresenter.updateLeaderboardFromApi(presenter.usersCount)
            }
        })

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        recyclerView.addOnScrollListener(fastScroller.onScrollListener)
    }

    override fun showBottomProgress(show: Boolean) {
        if (!isAdded) {
            return
        }

        if (show) {
            val screenHeight = DimensionUtils.getScreenHeight()
            swipeRefresh.setProgressViewEndTarget(
                false,
                (screenHeight - DimensionUtils.getActionBarHeight(activity!!) * 3.5f).toInt()
            )
        }

        swipeRefresh.isRefreshing = show
    }

    //todo move to base activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult called in fragment")
        if (requestCode == SubscriptionsFragment.REQUEST_CODE_SUBSCRIPTION) {
            if (data == null) {
                if (isAdded) {
                    showMessageLong("Error while parse result, please try again")
                }
                return
            }
            val responseCode = data.getIntExtra("RESPONSE_CODE", 0)
            val purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA")

            if (resultCode == Activity.RESULT_OK && responseCode == InAppHelper.RESULT_OK) {
                try {
                    val jo = JSONObject(purchaseData)
                    val sku = jo.getString("productId")
                    Timber.d("You have bought the %s", sku)

                    //validate subs list
                    if (baseActivity != null) {
                        baseActivity!!.updateOwnedMarketItems()
                    } else {
                        Timber.wtf("baseActivity is null!!!")
                    }
                } catch (e: JSONException) {
                    Timber.e(e, "Failed to parse purchase data.")
                    showError(e)
                }
            } else {
                if (isAdded) {
                    showMessageLong("Error: response code is not \"0\". Please try again")
                }
            }
        } else if (requestCode == BaseDrawerActivity.REQUEST_CODE_INAPP) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    Timber.d("error_inapp data is NULL")
                    showMessage(R.string.error_inapp)
                    return
                }
                //            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
                val purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA")
                //            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
                Timber.d("purchaseData %s", purchaseData)
                val item = mGson.fromJson(purchaseData, PurchaseData::class.java)
                Timber.d("You have bought the %s", item.productId)

                if (item.productId == InAppHelper.getNewInAppsSkus().first()) {
                    //levelUp 5
                    //add 10 000 score
                    mInAppHelper.consumeInApp(item.productId, item.purchaseToken, baseActivity?.getIInAppBillingService())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                onSuccess = {
                                    Timber.d("consume inapp successful, so update user score")
                                    mPresenter.updateUserScoreForInapp(item.productId)

                                    if (!myPreferenceManager.isHasAnySubscription) {
                                        baseActivity?.showOfferSubscriptionPopup()
                                    }
                                },
                                onError = {
                                    //todo show dialog with retry button
                                    Timber.e(it, "error while consume inapp... X3 what to do)))")
                                    showError(it)
                                }
                            )
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
