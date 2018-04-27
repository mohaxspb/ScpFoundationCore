package ru.kuchanov.scpcore.ui.adapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.model.VkImage;
import ru.kuchanov.scpcore.util.AttributeGetter;

/**
 * Created by Ivan Semkin on 4/27/2017.
 * <p>
 * for scp_ru
 */
public class ImagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<VkImage> mVkImages;

    private ImageClickListener mImageClickListener;

    public void setImageClickListener(final ImageClickListener imageClickListener) {
        mImageClickListener = imageClickListener;
    }

    public void setData(final List<VkImage> vkImages) {
        mVkImages = vkImages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_small_img, parent, false);
        return new ViewHolderImage(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        ((ViewHolderImage) holder).bind(mVkImages.get(position));
    }

    @Override
    public int getItemCount() {
        if (mVkImages != null) {
            return mVkImages.size();
        }  else {
            return -1;
        }
    }

    public interface ImageClickListener {
        void onItemClick(int position, View v);
    }

    class ViewHolderImage extends RecyclerView.ViewHolder {
        @BindView(R2.id.image)
        ImageView imageView;

        ViewHolderImage(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(final VkImage vkImage) {
            final Context context = itemView.getContext();
            final String imageUrl = vkImage.allUrls.get(vkImage.allUrls.size() - 1).getVal();

            File file = null;
            if (!TextUtils.isEmpty(imageUrl)) {
                file = new File(context.getFilesDir(), "/image/" + ApiClient.formatUrlToFileName(imageUrl));
            }

            Glide.with(context)
                    .load(file!=null && file.exists() ? "file://" + file.getAbsolutePath() : imageUrl)
                    .error(AttributeGetter.getDrawableId(context, R.attr.iconEmptyImage))
                    .crossFade()
                    .centerCrop()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(
                                final Exception e,
                                final String model,
                                final Target<GlideDrawable> target,
                                final boolean isFirstResource
                        ) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(
                                final GlideDrawable resource,
                                final String model,
                                final Target<GlideDrawable> target,
                                final boolean isFromMemoryCache,
                                final boolean isFirstResource
                        ) {
                            imageView.setOnClickListener(v -> mImageClickListener.onItemClick(getAdapterPosition(), imageView));
                            return false;
                        }
                    })
                    .into(imageView);
        }
    }
}