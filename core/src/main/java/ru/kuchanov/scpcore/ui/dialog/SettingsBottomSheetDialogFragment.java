package ru.kuchanov.scpcore.ui.dialog;

import android.app.Dialog;
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
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

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
import ru.kuchanov.scpcore.monetization.util.InAppHelper;
import ru.kuchanov.scpcore.ui.activity.SubscriptionsActivity;
import ru.kuchanov.scpcore.ui.adapter.SettingsSpinnerAdapter;
import ru.kuchanov.scpcore.ui.adapter.SettingsSpinnerCardDesignAdapter;
import ru.kuchanov.scpcore.ui.base.BaseBottomSheetDialogFragment;
import ru.kuchanov.scpcore.util.AttributeGetter;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * Created by mohax on 14.01.2017.
 * <p>
 * for scp_ru
 */
public class SettingsBottomSheetDialogFragment
        extends BaseBottomSheetDialogFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

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

    //design
    @BindView(R2.id.listItemStyle)
    View listItemStyle;
    @BindView(R2.id.listItemSpinner)
    Spinner listItemSpinner;
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

    @BindView(R2.id.buy)
    TextView mActivateAutoSync;

    @Inject
    MyPreferenceManager mMyPreferenceManager;
    @Inject
    MyNotificationManager mMyNotificationManager;
    @Inject
    InAppHelper mInAppHelper;

    public static BottomSheetDialogFragment newInstance() {
        return new SettingsBottomSheetDialogFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_bottom_sheet_settings;
    }

    @Override
    protected void callInjection() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        //design
        //card style
        listItemStyle.setOnClickListener(view -> listItemSpinner.performClick());
        String[] types = new String[]{ListItemType.MIN, ListItemType.MIDDLE, ListItemType.MAX};
        @ListItemType
        List<String> typesList = Arrays.asList(types);

        ArrayAdapter<String> adapterCard =
                new SettingsSpinnerCardDesignAdapter(getActivity(), R.layout.design_list_spinner_item, typesList);
        adapterCard.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Drawable.ConstantState spinnerDrawableConstantState = listItemSpinner.getBackground().getConstantState();
        if (spinnerDrawableConstantState != null) {
            Drawable spinnerDrawable = spinnerDrawableConstantState.newDrawable();
            spinnerDrawable.setColorFilter(AttributeGetter.getColor(getActivity(), R.attr.newArticlesTextColor), PorterDuff.Mode.SRC_ATOP);
            listItemSpinner.setBackground(spinnerDrawable);
        }

        listItemSpinner.setAdapter(adapterCard);

        listItemSpinner.post(() -> {
            listItemSpinner.setSelection(typesList.indexOf(mMyPreferenceManager.getListDesignType()));
            listItemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    mMyPreferenceManager.setListDesignType(types[i]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        });
        //font
        CalligraphyUtils.applyFontToTextView(getActivity(), fontPreferedTitle, mMyPreferenceManager.getFontPath());
        fontPrefered.setOnClickListener(view -> fontPreferedSpinner.performClick());

        List<String> fontsPathsList = Arrays.asList(getResources().getStringArray(R.array.fonts));
        @ListItemType
        List<String> fontsList = Arrays.asList(getResources().getStringArray(R.array.fonts_names));

        ArrayAdapter<String> adapter =
                new SettingsSpinnerAdapter(getActivity(), R.layout.design_list_spinner_item_font, fontsList, fontsPathsList);
        adapter.setDropDownViewResource(R.layout.design_list_spinner_item_font);

        Drawable.ConstantState fontsSpinnerDrawableConstantState = fontPreferedSpinner.getBackground().getConstantState();
        if (fontsSpinnerDrawableConstantState != null) {
            Drawable spinnerDrawable = fontsSpinnerDrawableConstantState.newDrawable();
            spinnerDrawable.setColorFilter(AttributeGetter.getColor(getActivity(), R.attr.newArticlesTextColor), PorterDuff.Mode.SRC_ATOP);
            fontPreferedSpinner.setBackground(spinnerDrawable);
        }

        fontPreferedSpinner.setAdapter(adapter);

        fontPreferedSpinner.setSelection(fontsPathsList.indexOf(mMyPreferenceManager.getFontPath()));

        fontPreferedSpinner.post(() -> {
            fontPreferedSpinner.setSelection(fontsList.indexOf(mMyPreferenceManager.getFontPath()));
            fontPreferedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    Timber.d("onItemSelected position: %s, font: %s", position, fontsList.get(position));
                    //close all except 2 for unsubscribed
//                    if (position > 1 && !getBaseActivity().getOwnedItems().isEmpty()) {
                    if (position > 1 && !mMyPreferenceManager.isHasSubscription()) {
                        Timber.d("show subs dialog");

                        fontPreferedSpinner.setSelection(fontsPathsList.indexOf(mMyPreferenceManager.getFontPath()));
                        showSnackBarWithAction(Constants.Firebase.CallToActionReason.ENABLE_FONTS);
                    } else {
                        mMyPreferenceManager.setFontPath(fontsPathsList.get(position));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
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
            Timber.d("notifOnCheckChanged checked: %s", checked);
            mMyPreferenceManager.setNotificationEnabled(checked);
            mMyNotificationManager.checkAlarm();
        });

        notifLedIsOnSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            Timber.d("notifOnCheckChanged checked: %s", checked);
            mMyPreferenceManager.setNotificationLedEnabled(checked);
            mMyNotificationManager.checkAlarm();
        });

        notifSoundIsOnSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            Timber.d("notifOnCheckChanged checked: %s", checked);
            mMyPreferenceManager.setNotificationSoundEnabled(checked);
            mMyNotificationManager.checkAlarm();
        });

        notifVibrateIsOnSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            Timber.d("notifOnCheckChanged checked: %s", checked);
            mMyPreferenceManager.setNotificationVibrationEnabled(checked);
            mMyNotificationManager.checkAlarm();
        });

        //hide activate subs for good users
        boolean noFullSubscription = !mMyPreferenceManager.isHasSubscription();
        mActivateAutoSync.setVisibility(noFullSubscription ? View.VISIBLE : View.GONE);
    }

    @OnClick(R2.id.buy)
    void onActivateAutoSyncClicked() {
        Timber.d("onActivateAutoSyncClicked");
        dismiss();

        SubscriptionsActivity.start(getActivity());

        Bundle bundle = new Bundle();
        bundle.putString(Constants.Firebase.Analitics.EventParam.PLACE, Constants.Firebase.Analitics.StartScreen.AUTO_SYNC_FROM_SETTINGS);
        FirebaseAnalytics.getInstance(getActivity()).logEvent(Constants.Firebase.Analitics.EventName.SUBSCRIPTIONS_SHOWN, bundle);
    }

    @OnClick(R2.id.sync)
    void onSyncClicked() {
        Timber.d("onSyncClicked");
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            showSnackBarWithAction(Constants.Firebase.CallToActionReason.SYNC_NEED_AUTH);
            return;
        }
        getBaseActivity().createPresenter().syncData(true);
        dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;

            FrameLayout bottomSheet = d.findViewById(android.support.design.R.id.design_bottom_sheet);
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!isAdded()) {
            return;
        }
        switch (key) {
            case MyPreferenceManager.Keys.DESIGN_FONT_PATH:
                CalligraphyUtils.applyFontToTextView(getActivity(), fontPreferedTitle, mMyPreferenceManager.getFontPath());
                break;
            default:
                //do nothing
                break;
        }
    }
}