package ru.kuchanov.scpcore.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;

import butterknife.BindView;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.base.BaseBottomSheetDialogFragment;
import timber.log.Timber;


public class TextSizeDialogFragment
        extends BaseBottomSheetDialogFragment {
    public static final String TAG = TextSizeDialogFragment.class.getSimpleName();

    private static final String EXTRA_TEXT_SIZE_TYPE = "EXTRA_TEXT_SIZE_TYPE";

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    @BindView(R2.id.title)
    TextView title;
    @BindView(R2.id.seekbarUi)
    SeekBar seekbarUI;
    @BindView(R2.id.seekbarArticle)
    SeekBar seekbarArticle;
    @BindView(R2.id.textSizeUi)
    TextView tvUi;
    @BindView(R2.id.textSizeArticle)
    TextView tvArticle;

    @TextSizeType
    private String mType;

    public static BottomSheetDialogFragment newInstance(@TextSizeType String type) {
        BottomSheetDialogFragment fragment = new TextSizeDialogFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_TEXT_SIZE_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        @TextSizeType
        String value = getArguments().getString(EXTRA_TEXT_SIZE_TYPE);
        mType = value;
    }

    @Override
    protected void callInjection() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        switch (mType) {
            case TextSizeType.ARTICLE:
                seekbarUI.setVisibility(View.GONE);
                tvUi.setVisibility(View.GONE);
                break;
            case TextSizeType.UI:
                seekbarArticle.setVisibility(View.GONE);
                tvArticle.setVisibility(View.GONE);
                break;
            case TextSizeType.ALL:
                //do nothing?
                break;
            default:
                Timber.wtf("unexpected type!");
                break;
        }

        seekbarUI.setMax(150);
        float scaleUI = mMyPreferenceManager.getUiTextScale();

        int curProgressUI = (int) ((scaleUI - 0.50f) * 100);
        seekbarUI.setProgress(curProgressUI);
        seekbarUI.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float size = (progress / 100f) + 0.50f;
                int textSizePrimaryInDp = (int) (getResources().getDimension(R.dimen.text_size_primary)
                        / getResources().getDisplayMetrics().density);
//                Timber.d("text_size_primary: %s", textSizePrimaryInDp);
                tvUi.setTextSize(size * textSizePrimaryInDp);
                mMyPreferenceManager.setUiTextScale(size);
            }
        });

        seekbarArticle.setMax(150);
        float scaleArt = mMyPreferenceManager.getArticleTextScale();
        int curProgressArt = (int) ((scaleArt - 0.50f) * 100);
        seekbarArticle.setProgress(curProgressArt);
        seekbarArticle.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float size = (progress / 100f) + 0.50f;
                int textSizePrimaryInDp = (int) (getResources().getDimension(R.dimen.text_size_primary)
                        / getResources().getDisplayMetrics().density);
                tvArticle.setTextSize(size * textSizePrimaryInDp);
                mMyPreferenceManager.setArticleTextScale(size);
            }
        });
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_text_size;
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TextSizeType.UI, TextSizeType.ARTICLE, TextSizeType.ALL})
    public @interface TextSizeType {
        String UI = "UI";
        String ARTICLE = "ARTICLE";
        String ALL = "ALL";
    }
}