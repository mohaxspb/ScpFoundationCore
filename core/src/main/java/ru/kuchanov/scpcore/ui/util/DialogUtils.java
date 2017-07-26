package ru.kuchanov.scpcore.ui.util;

import android.app.Dialog;
import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import timber.log.Timber;

/**
 * Created by mohax on 29.05.2017.
 * <p>
 * for ScpFoundationRu
 */
public class DialogUtils {

    private MyPreferenceManager mPreferenceManager;
    private DbProviderFactory mDbProviderFactory;
    private ApiClient mApiClient;

    public DialogUtils(
            MyPreferenceManager preferenceManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        mPreferenceManager = preferenceManager;
        mDbProviderFactory = dbProviderFactory;
        mApiClient = apiClient;
    }

    public void showFaqDialog(Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.faq)
                .positiveText(R.string.close)
                .items(R.array.fag_items)
                .alwaysCallSingleChoiceCallback()
                .itemsCallback((dialog, itemView, position, text) -> {
                    Timber.d("itemsCallback: %s", text);
                    new MaterialDialog.Builder(context)
                            .title(text)
                            .content(context.getResources().getStringArray(R.array.fag_items_content)[position])
                            .positiveText(R.string.close)
                            .build()
                            .show();
                })
                .build()
                .show();
    }

    //TODO think how to restore image dialog Maybe use fragment dialog?..
    public void showImageDialog(Context mContext, String imgUrl) {
        Timber.d("showImageDialog");
        Dialog nagDialog = new Dialog(mContext, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        nagDialog.setCancelable(true);
        nagDialog.setContentView(R.layout.preview_image);

        final PhotoView photoView = (PhotoView) nagDialog.findViewById(R.id.image_view_touch);
        photoView.setMaximumScale(5f);

        Glide.with(photoView.getContext())
                .load(imgUrl)
                .placeholder(R.drawable.ic_image_white_48dp)
                .into(photoView);

        nagDialog.show();
    }
}