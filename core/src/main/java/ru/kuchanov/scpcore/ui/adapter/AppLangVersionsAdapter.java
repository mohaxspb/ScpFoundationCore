package ru.kuchanov.scpcore.ui.adapter;

import com.bumptech.glide.Glide;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.model.remoteconfig.AppLangVersionsJson;
import ru.kuchanov.scpcore.util.DimensionUtils;

public class AppLangVersionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<AppLangVersionsJson.AppLangVersion> items;

    private ItemCallback itemCallback;

    public AppLangVersionsAdapter(final List<AppLangVersionsJson.AppLangVersion> items) {
        super();
        this.items = items;
    }

    public void setCallbacks(final ItemCallback itemCallback) {
        this.itemCallback = itemCallback;
    }

    @Override
    public int getItemViewType(final int position) {
        if (position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View view;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_title, parent, false);
                return new TextVH(view);
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_title_content_image_ads, parent, false);
                return new ButtonVH(view, this);
            default:
                throw new IllegalStateException("unexpected viewType: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case 0:
                final TextVH holderText = (TextVH) holder;
                holderText.content.setTextSize(16f);
                holderText.content.setText(R.string.app_lang_dialog_google_description);
                break;
            case 1:
                final AppLangVersionsJson.AppLangVersion appLangVersion = items.get(position);
                final ButtonVH holderMain = (ButtonVH) holder;
                holderMain.title.setText(appLangVersion.title);
                holderMain.title.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_shop, 0);
                holderMain.title.setCompoundDrawablePadding(DimensionUtils.dpToPx(8));
                holderMain.content.setVisibility(View.GONE);
                Glide.with(holderMain.mImageView.getContext())
                        .load(appLangVersion.icon)
                        .into(holderMain.mImageView);
                break;
            default:
                throw new IllegalStateException("unexpected viewType: " + viewType);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface ItemCallback {

        void onItemClicked(int itemIndex);
    }

    static class ButtonVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView title;

        final TextView content;

        final ImageView mImageView;

        final AppLangVersionsAdapter adapter;

        ButtonVH(final View itemView, final AppLangVersionsAdapter adapter) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            mImageView = itemView.findViewById(R.id.image);

            this.adapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(final View view) {
            if (adapter.itemCallback == null) {
                return;
            }
            adapter.itemCallback.onItemClicked(getAdapterPosition());
        }
    }

    static class TextVH extends RecyclerView.ViewHolder {

        final TextView content;

        TextVH(final View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.text);
        }
    }
}