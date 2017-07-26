package ru.kuchanov.scpcore.ui.dialog;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.model.AppInstallHeader;
import ru.kuchanov.scpcore.monetization.model.AppInviteModel;
import ru.kuchanov.scpcore.monetization.model.ApplicationsResponse;
import ru.kuchanov.scpcore.monetization.model.BaseModel;
import ru.kuchanov.scpcore.monetization.model.PlayMarketApplication;
import ru.kuchanov.scpcore.monetization.model.RewardedVideo;
import ru.kuchanov.scpcore.monetization.model.VkGroupToJoin;
import ru.kuchanov.scpcore.monetization.model.VkGroupsToJoinResponse;
import ru.kuchanov.scpcore.ui.activity.MainActivity;
import ru.kuchanov.scpcore.ui.adapter.FreeAdsDisableRecyclerAdapter;
import ru.kuchanov.scpcore.ui.base.BaseActivity;
import ru.kuchanov.scpcore.util.IntentUtils;
import timber.log.Timber;

public class FreeAdsDisablingDialogFragment extends DialogFragment {

    public static final String TAG = FreeAdsDisablingDialogFragment.class.getSimpleName();
    private static final int NOTIFICATION_ID = 103;

    @Inject
    Gson mGson;
    @Inject
    ApiClient mApiClient;

    public static DialogFragment newInstance() {
        return new FreeAdsDisablingDialogFragment();
    }

    @Inject
    protected MyPreferenceManager mMyPreferenceManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Timber.d("onCreateDialog");
        final MaterialDialog dialog;

        MaterialDialog.Builder dialogTextSizeBuilder = new MaterialDialog.Builder(getActivity());
        dialogTextSizeBuilder
                .title(R.string.dialog_free_ads_disable_title)
                .positiveText(android.R.string.cancel);

        List<BaseModel> data = new ArrayList<>();

        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        if (config.getBoolean(Constants.Firebase.RemoteConfigKeys.FREE_REWARDED_VIDEO_ENABLED)) {
            long numOfMillis = FirebaseRemoteConfig.getInstance()
                    .getLong(Constants.Firebase.RemoteConfigKeys.REWARDED_VIDEO_COOLDOWN_IN_MILLIS);
            long hours = numOfMillis / 1000 / 60 / 60;
            int score = (int) FirebaseRemoteConfig.getInstance()
                    .getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_REWARDED_VIDEO);
            data.add(new RewardedVideo(getString(R.string.watch_video_to_disable_ads, hours, score)));
        }
        if (config.getBoolean(Constants.Firebase.RemoteConfigKeys.FREE_INVITES_ENABLED)) {
            data.add(new AppInviteModel(getString(R.string.invite_friends)));
        }
        if (config.getBoolean(Constants.Firebase.RemoteConfigKeys.FREE_APPS_INSTALL_ENABLED)) {
            String jsonString = config.getString(Constants.Firebase.RemoteConfigKeys.APPS_TO_INSTALL_JSON);

            List<PlayMarketApplication> applications = null;
            try {
                applications = mGson.fromJson(jsonString, ApplicationsResponse.class).items;
            } catch (Exception e) {
                Timber.e(e);
            }
            if (applications != null) {
                List<PlayMarketApplication> availableAppsToInstall = new ArrayList<>();
                for (PlayMarketApplication application : applications) {
                    if (mMyPreferenceManager.isAppInstalledForPackage(application.id)) {
                        continue;
                    }
                    if (IntentUtils.isPackageInstalled(getActivity(), application.id)) {
                        continue;
                    }
                    availableAppsToInstall.add(application);
                }
                if (!availableAppsToInstall.isEmpty()) {
                    //add row with description
                    long numOfMillis = FirebaseRemoteConfig.getInstance()
                            .getLong(Constants.Firebase.RemoteConfigKeys.APP_INSTALL_REWARD_IN_MILLIS);
                    long hours = numOfMillis / 1000 / 60 / 60;
                    int score = (int) FirebaseRemoteConfig.getInstance()
                            .getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_OUR_APP);
                    data.add(new AppInstallHeader(getString(R.string.app_install_ads_disable_title, hours, score)));
                    data.addAll(availableAppsToInstall);
                }
            }
        }
        if (config.getBoolean(Constants.Firebase.RemoteConfigKeys.FREE_VK_GROUPS_ENABLED)) {
            String jsonString = config.getString(Constants.Firebase.RemoteConfigKeys.VK_GROUPS_TO_JOIN_JSON);

            List<VkGroupToJoin> items = null;
            try {
                items = mGson.fromJson(jsonString, VkGroupsToJoinResponse.class).items;
            } catch (Exception e) {
                Timber.e(e);
            }
            if (items != null) {
                List<VkGroupToJoin> availableItems = new ArrayList<>();
                for (VkGroupToJoin item : items) {
                    if (mMyPreferenceManager.isVkGroupJoined(item.id)) {
                        continue;
                    }
                    availableItems.add(item);
                }
                if (!availableItems.isEmpty()) {
                    //add row with description
                    long numOfMillis = FirebaseRemoteConfig.getInstance()
                            .getLong(Constants.Firebase.RemoteConfigKeys.FREE_VK_GROUPS_JOIN_REWARD);
                    long hours = numOfMillis / 1000 / 60 / 60;
                    int score = (int) FirebaseRemoteConfig.getInstance()
                            .getLong(Constants.Firebase.RemoteConfigKeys.SCORE_ACTION_VK_GROUP);
                    data.add(new AppInstallHeader(getString(R.string.vk_group_join_ads_disable_title, hours, score)));
                    data.addAll(availableItems);
                }
            }
        }

        FreeAdsDisableRecyclerAdapter adapter = new FreeAdsDisableRecyclerAdapter();
        adapter.setData(data);
        adapter.setItemClickListener(data1 -> {
            Timber.d("Clicked data: %s", data1);
            if (data1 instanceof AppInviteModel) {
                IntentUtils.firebaseInvite(getActivity());
            } else if (data1 instanceof PlayMarketApplication) {
                IntentUtils.tryOpenPlayMarket(getActivity(), ((PlayMarketApplication) data1).id);
            } else if (data1 instanceof RewardedVideo) {
                dismiss();
                getBaseActivity().startRewardedVideoFlow();
            } else if (data1 instanceof VkGroupToJoin) {
                String vkGroupId = ((VkGroupToJoin) data1).id;
                Timber.d("VkGroupToJoin: %s", vkGroupId);
                if (!VKSdk.isLoggedIn()) {
                    VKSdk.login(getActivity(), VKScope.EMAIL, VKScope.GROUPS);
                    return;
                } else if (!VKAccessToken.currentToken().hasScope(VKScope.GROUPS)) {
                    Toast.makeText(getActivity(), R.string.need_vk_group_access, Toast.LENGTH_LONG).show();
                    return;
                }
                mApiClient.joinVkGroup(vkGroupId).subscribe(
                        result -> {
                            if (result) {
                                Timber.d("Successful group join");
                                mMyPreferenceManager.setVkGroupJoined(vkGroupId);
                                mMyPreferenceManager.applyAwardVkGroupJoined();

                                long numOfMillis = FirebaseRemoteConfig.getInstance()
                                        .getLong(Constants.Firebase.RemoteConfigKeys.FREE_VK_GROUPS_JOIN_REWARD);
                                long hours = numOfMillis / 1000 / 60 / 60;

                                getBaseActivity().createPresenter().updateUserScoreForVkGroup(vkGroupId);

                                showNotificationSimple(getActivity(), getString(R.string.ads_reward_gained, hours), getString(R.string.thanks_for_supporting_us));

                                data.remove(data1);
                                adapter.notifyDataSetChanged();

                                Bundle bundle = new Bundle();
                                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "group" + vkGroupId);
                                FirebaseAnalytics.getInstance(getActivity()).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                            } else {
                                Timber.e("error group join");
                            }
                        },
                        e -> {
                            Timber.e(e, "error while join group");
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                );
            } else {
                Timber.wtf("Unexpected type!");
            }
        });

        dialogTextSizeBuilder.adapter(adapter, new LinearLayoutManager(getActivity()));

        dialog = dialogTextSizeBuilder.build();

        dialog.getRecyclerView().addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        return dialog;
    }

    protected BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    private void showNotificationSimple(Context context, String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT), 0);
        builder.setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setContentText(content)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(NOTIFICATION_ID, builder.build());
    }
}