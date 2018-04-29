package ru.kuchanov.scpcore.ui.adapter;

import com.bumptech.glide.Glide;

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

public class AppLangVersionsAdapter extends RecyclerView.Adapter<AppLangVersionsAdapter.ButtonVH> {

    private final List<AppLangVersionsJson.AppLangVersion> items;

    private ItemCallback itemCallback;

    public AppLangVersionsAdapter(List<AppLangVersionsJson.AppLangVersion> items) {
        this.items = items;
    }

    public void setCallbacks(ItemCallback itemCallback) {
        this.itemCallback = itemCallback;
    }

    @Override
    public ButtonVH onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_title_content_image, parent, false);
        return new ButtonVH(view, this);
    }

    @Override
    public void onBindViewHolder(ButtonVH holder, int position) {
        AppLangVersionsJson.AppLangVersion appLangVersion = items.get(position);
        holder.title.setText(appLangVersion.title);
        holder.title.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_shop, 0);
        holder.title.setCompoundDrawablePadding(DimensionUtils.dpToPx(8));
        holder.content.setVisibility(View.GONE);
        Glide.with(holder.mImageView.getContext())
                .load(appLangVersion.icon)
                .into(holder.mImageView);
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

        ButtonVH(View itemView, AppLangVersionsAdapter adapter) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            mImageView = itemView.findViewById(R.id.image);

            this.adapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (adapter.itemCallback == null) {
                return;
            }
            adapter.itemCallback.onItemClicked(getAdapterPosition());
        }
    }
}