package ru.kuchanov.scpcore.ui.dialog;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.MyNotificationManager;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.ui.activity.SubscriptionsActivity;
import ru.kuchanov.scpcore.ui.adapter.SettingsSpinnerAdapter;
import ru.kuchanov.scpcore.ui.adapter.SettingsSpinnerCardDesignAdapter;
import ru.kuchanov.scpcore.ui.util.DialogUtils;
import ru.kuchanov.scpcore.ui.util.FontUtils;
import ru.kuchanov.scpcore.util.AttributeGetter;
import ru.kuchanov.scpcore.util.StorageUtils;
import timber.log.Timber;

/**
 * Created by mohax on 14.01.2017.
 * <p>
 * for scp_ru
 */
public class SettingsBottomSheetDialogFragment
        extends BaseBottomSheetDialogFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static BottomSheetDialogFragment newInstance() {
        return new SettingsBottomSheetDialogFragment();
    }

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    @Inject
    MyNotificationManager mMyNotificationManager;

    @Inject
    InAppHelper mInAppHelper;

    @Inject
    protected DialogUtils mDialogUtils;

    //design
    @BindView(R2.id.listItemStyle)
    View listItemStyle;

    @BindView(R2.id.listItemSpinner)
    Spinner listItemSpinner;

    @BindView(R2.id.textIsSelectableSwitch)
    SwitchCompat textIsSelectableSwitch;

    @BindView(R2.id.fontPreferedTitle)
    TextView fontPreferedTitle;

    @BindView(R2.id.fontPrefered)
    View fontPrefered;

    @BindView(R2.id.fontPreferedSpinner)
    Spinner fontPreferedSpinner;

    //notif
    @BindView(R2.id.notifIsOnSwitch)
    SwitchCompat notifIsOnSwitch;

    @BindView(R2.id.notifLedisOnSwitch)
    SwitchCompat notifLedIsOnSwitch;

    @BindView(R2.id.notifSoundIsOnSwitch)
    SwitchCompat notifSoundIsOnSwitch;

    @BindView(R2.id.notifVibrateIsOnSwitch)
    SwitchCompat notifVibrateIsOnSwitch;

    @BindView(R2.id.randomOfflineIsOnSwitch)
    SwitchCompat randomOfflineIsOnSwitch;

    @BindView(R2.id.offlineRandomTextView)
    TextView offlineRandomTextView;

    //downloads
    @BindView(R2.id.downloadsForceUpdateSwitch)
    SwitchCompat downloadsForceUpdateSwitch;

    @BindView(R2.id.downloadsNewArticlesSwitch)
    SwitchCompat downloadsNewArticlesSwitch;

    @BindView(R2.id.downloadInnerDepthValueTextView)
    TextView downloadInnerDepthValueTextView;

    @BindView(R2.id.downloadsDepthSeekbar)
    SeekBar downloadsDepthSeekbar;

    @BindView(R2.id.activate)
    TextView activateTextView;
    //downloads END

    //images cache
    @BindView(R2.id.imagesCacheEnabledSwitch)
    SwitchCompat imagesCacheEnabledSwitch;

    @BindView(R2.id.cachedImagesCountValueTextView)
    TextView cachedImagesCountValueTextView;

    @BindView(R2.id.cachedImagesSizeValueTextView)
    TextView cachedImagesSizeValueTextView;

    @BindView(R2.id.clearImagesButton)
    View clearImagesButton;
    //images cache END

    @BindView(R2.id.buy)
    TextView mActivateAutoSync;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_bottom_sheet_settings;
    }

    @Override
    protected void callInjection() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    public void setupDialog(final Dialog dialog, final int style) {
        super.setupDialog(dialog, style);

        //design
        //card style
        listItemStyle.setOnClickListener(view -> listItemSpinner.performClick());
        final String[] types = {ListItemType.MIN, ListItemType.MIDDLE, ListItemType.MAX};
        @ListItemType final List<String> typesList = Arrays.asList(types);

        final Context context = getActivity();

        final ArrayAdapter<String> adapterCard =
                new SettingsSpinnerCardDesignAdapter(context, R.layout.design_list_spinner_item, typesList);
        adapterCard.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Drawable.ConstantState spinnerDrawableConstantState = listItemSpinner.getBackground().getConstantState();
        if (spinnerDrawableConstantState != null) {
            final Drawable spinnerDrawable = spinnerDrawableConstantState.newDrawable();
            spinnerDrawable.setColorFilter(AttributeGetter.getColor(context, R.attr.newArticlesTextColor), PorterDuff.Mode.SRC_ATOP);
            listItemSpinner.setBackground(spinnerDrawable);
        }

        listItemSpinner.setAdapter(adapterCard);

        listItemSpinner.post(() -> {
            if (listItemSpinner == null) {
                return;
            }
            listItemSpinner.setSelection(typesList.indexOf(mMyPreferenceManager.getListDesignType()));
            listItemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(final AdapterView<?> adapterView, final View view, final int i, final long l) {
                    mMyPreferenceManager.setListDesignType(types[i]);
                }

                @Override
                public void onNothingSelected(final AdapterView<?> adapterView) {

                }
            });
        });

        textIsSelectableSwitch.setChecked(mMyPreferenceManager.isTextSelectable());
        final CompoundButton.OnCheckedChangeListener changeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, final boolean checked) {
                Timber.d("onCheckedChanged: %s", checked);
                textIsSelectableSwitch.setOnCheckedChangeListener(null);
                textIsSelectableSwitch.setChecked(false);
                textIsSelectableSwitch.setOnCheckedChangeListener(this);
                if (checked) {
                    mDialogUtils.showSelectableTextWarningDialog(
                            getActivity(),
                            (dialog1, which) -> {
                                textIsSelectableSwitch.setOnCheckedChangeListener(null);
                                textIsSelectableSwitch.setChecked(true);
                                mMyPreferenceManager.setTextIsSelectable(true);
                                textIsSelectableSwitch.setOnCheckedChangeListener(this);
                            },
                            (dialog1, which) -> {
                                textIsSelectableSwitch.setOnCheckedChangeListener(null);
                                textIsSelectableSwitch.setChecked(false);
                                mMyPreferenceManager.setTextIsSelectable(false);
                                textIsSelectableSwitch.setOnCheckedChangeListener(this);
                            }
                    );
                } else {
                    mMyPreferenceManager.setTextIsSelectable(false);
                }
            }
        };
        textIsSelectableSwitch.setOnCheckedChangeListener(changeListener);

        //font
        fontPrefered.setOnClickListener(view -> fontPreferedSpinner.performClick());

        final List<String> fontsPathsList = Arrays.asList(getResources().getStringArray(R.array.fonts));
        @ListItemType final List<String> fontsList = Arrays.asList(getResources().getStringArray(R.array.fonts_names));

        final ArrayAdapter<String> adapter =
                new SettingsSpinnerAdapter(context, R.layout.design_list_spinner_item_font, fontsList, fontsPathsList);
        adapter.setDropDownViewResource(R.layout.design_list_spinner_item_font);

        final Drawable.ConstantState fontsSpinnerDrawableConstantState = fontPreferedSpinner.getBackground().getConstantState();
        if (fontsSpinnerDrawableConstantState != null) {
            final Drawable spinnerDrawable = fontsSpinnerDrawableConstantState.newDrawable();
            spinnerDrawable.setColorFilter(AttributeGetter.getColor(context, R.attr.newArticlesTextColor), PorterDuff.Mode.SRC_ATOP);
            fontPreferedSpinner.setBackground(spinnerDrawable);
        }

        fontPreferedSpinner.setAdapter(adapter);

        fontPreferedSpinner.setSelection(fontsPathsList.indexOf(mMyPreferenceManager.getFontPath()));

        fontPreferedSpinner.post(() -> {
            if (fontPreferedSpinner == null) {
                return;
            }
            fontPreferedSpinner.setSelection(fontsList.indexOf(mMyPreferenceManager.getFontPath()));
            fontPreferedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(final AdapterView<?> adapterView, final View view, final int position, final long id) {
                    //close all except 2 for unsubscribed
                    if (position > 1 && !mMyPreferenceManager.isHasSubscription()) {
                        fontPreferedSpinner.setSelection(fontsPathsList.indexOf(mMyPreferenceManager.getFontPath()));
                        showSnackBarWithAction(Constants.Firebase.CallToActionReason.ENABLE_FONTS);
                    } else {
                        mMyPreferenceManager.setFontPath(fontsPathsList.get(position));
                    }
                }

                @Override
                public void onNothingSelected(final AdapterView<?> adapterView) {
                    Timber.d("onNothingSelected");
                }
            });
        });

        //notif
        notifIsOnSwitch.setChecked(mMyPreferenceManager.isNotificationEnabled());
        notifLedIsOnSwitch.setChecked(mMyPreferenceManager.isNotificationLedEnabled());
        notifSoundIsOnSwitch.setChecked(mMyPreferenceManager.isNotificationSoundEnabled());
        notifVibrateIsOnSwitch.setChecked(mMyPreferenceManager.isNotificationVibrationEnabled());

        notifIsOnSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            mMyPreferenceManager.setNotificationEnabled(checked);
            mMyNotificationManager.checkAlarm();
        });

        notifLedIsOnSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            mMyPreferenceManager.setNotificationLedEnabled(checked);
            mMyNotificationManager.checkAlarm();
        });

        notifSoundIsOnSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            mMyPreferenceManager.setNotificationSoundEnabled(checked);
            mMyNotificationManager.checkAlarm();
        });

        notifVibrateIsOnSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            mMyPreferenceManager.setNotificationVibrationEnabled(checked);
            mMyNotificationManager.checkAlarm();
        });

        randomOfflineIsOnSwitch.setChecked(mMyPreferenceManager.isOfflineRandomEnabled());
        final String randomLabel = getString(R.string.drawer_item_5) + " offline";
        offlineRandomTextView.setText(randomLabel);
        randomOfflineIsOnSwitch.setOnCheckedChangeListener((compoundButton, checked) -> mMyPreferenceManager.setOfflineRandomEnabled(checked));

        //downloads
        downloadsForceUpdateSwitch.setChecked(mMyPreferenceManager.isDownloadForceUpdateEnabled());
        downloadsForceUpdateSwitch.setOnCheckedChangeListener((compoundButton, checked) -> mMyPreferenceManager.setDownloadForceUpdateEnabled(checked));

        //download new articles
        downloadsNewArticlesSwitch.setChecked(mMyPreferenceManager.isSaveNewArticlesEnabled());
        downloadsNewArticlesSwitch.setOnCheckedChangeListener((compoundButton, checked) -> mMyPreferenceManager.setSaveNewArticlesEnabled(checked));

        downloadsDepthSeekbar.setMax(MyPreferenceManager.MAX_DOWNLOADS_DEPTH);
        downloadsDepthSeekbar.setProgress(mMyPreferenceManager.getInnerArticlesDepth());
        downloadsDepthSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                downloadInnerDepthValueTextView.setText(String.valueOf(progress));
                mMyPreferenceManager.setInnerArticlesDepth(progress);
            }
        });
        downloadInnerDepthValueTextView.setText(String.valueOf(mMyPreferenceManager.getInnerArticlesDepth()));
        activateTextView.setVisibility(!mMyPreferenceManager.isHasSubscription() ? View.VISIBLE : View.GONE);
        //downloads END

        //images cache
        imagesCacheEnabledSwitch.setChecked(mMyPreferenceManager.isImagesCacheEnabled());
        imagesCacheEnabledSwitch.setOnCheckedChangeListener((compoundButton, checked) -> mMyPreferenceManager.setImagesCacheEnabled(checked));

        final int cachedImagesCount = StorageUtils.cachedImagesFilesCount(context);

        cachedImagesCountValueTextView.setText(String.valueOf(cachedImagesCount));

        cachedImagesSizeValueTextView.setText(StorageUtils.humanReadableByteCount(StorageUtils.cachedImagesFolderSize(context), true));
        clearImagesButton.setEnabled(cachedImagesCount > 0);
        //images cache END

        //hide activate subs for good users
        mActivateAutoSync.setVisibility(!mMyPreferenceManager.isHasSubscription() ? View.VISIBLE : View.GONE);
    }

    @OnClick(R2.id.activate)
    void onActivateClicked() {
        dismiss();

        SubscriptionsActivity.start(getActivity());

        final Bundle bundle = new Bundle();
        bundle.putString(Constants.Firebase.Analytics.EventParam.PLACE, Constants.Firebase.Analytics.StartScreen.INNER_DOWNLOADS_FROM_SETTINGS);
        FirebaseAnalytics.getInstance(getActivity()).logEvent(Constants.Firebase.Analytics.EventName.SUBSCRIPTIONS_SHOWN, bundle);
    }

    @OnClick(R2.id.clearImagesButton)
    void onClearImagesClicked() {
        //todo show confirm dialog
        final Context context = getActivity();
        StorageUtils.deleteCachedImages(context);

        final int cachedImagesCount = StorageUtils.cachedImagesFilesCount(context);

        cachedImagesCountValueTextView.setText(String.valueOf(cachedImagesCount));

        cachedImagesSizeValueTextView.setText(StorageUtils.humanReadableByteCount(StorageUtils.cachedImagesFolderSize(context), true));

        clearImagesButton.setEnabled(cachedImagesCount > 0);
    }

    @OnClick(R2.id.buy)
    void onActivateAutoSyncClicked() {
        dismiss();

        SubscriptionsActivity.start(getActivity());

        final Bundle bundle = new Bundle();
        bundle.putString(Constants.Firebase.Analytics.EventParam.PLACE, Constants.Firebase.Analytics.StartScreen.AUTO_SYNC_FROM_SETTINGS);
        FirebaseAnalytics.getInstance(getActivity()).logEvent(Constants.Firebase.Analytics.EventName.SUBSCRIPTIONS_SHOWN, bundle);
    }

    @OnClick(R2.id.sync)
    void onSyncClicked() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            showSnackBarWithAction(Constants.Firebase.CallToActionReason.SYNC_NEED_AUTH);
            return;
        }
        getBaseActivity().createPresenter().syncData(true);
        dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialog1 -> {
            final BottomSheetDialog d = (BottomSheetDialog) dialog1;

            final FrameLayout bottomSheet = d.findViewById(android.support.design.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        // Do something with your dialog like setContentView() or whatever
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (!isAdded()) {
            return;
        }
        switch (key) {
            case MyPreferenceManager.Keys.DESIGN_FONT_PATH:
                fontPreferedTitle.setTypeface(FontUtils.getTypeFaceFromName(mMyPreferenceManager.getFontPath()));
                break;
            default:
                //do nothing
                break;
        }
    }

    @StringDef({
            ListItemType.MIN,
            ListItemType.MIDDLE,
            ListItemType.MAX
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ListItemType {

        String MIN = "MIN";
        String MIDDLE = "MIDDLE";
        String MAX = "MAX";
    }
}