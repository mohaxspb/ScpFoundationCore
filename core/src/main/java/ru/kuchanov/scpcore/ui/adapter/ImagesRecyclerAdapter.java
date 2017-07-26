package ru.kuchanov.scpcore.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.db.model.VkImage;
import ru.kuchanov.scpcore.util.AttributeGetter;

/**
 * Created by Ivan Semkin on 4/27/2017.
 * <p>
 * for scp_ru
 */
public class ImagesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<VkImage> mVkImages;

    private ImageClickListener mImageClickListener;

    public void setImageClickListener(ImageClickListener imageClickListener) {
        mImageClickListener = imageClickListener;
    }

    public void setData(List<VkImage> vkImages) {
        mVkImages = vkImages;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_small_img, parent, false);
        viewHolder = new ViewHolderImage(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
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

        ViewHolderImage(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(VkImage vkImage) {
            Context context = itemView.getContext();
            String imageUrl = vkImage.allUrls.get(vkImage.allUrls.size() - 1).getVal();

            Glide.with(context)
                    .load(imageUrl)
                    .error(AttributeGetter.getDrawableId(context, R.attr.iconEmptyImage))
                    .crossFade()
                    .centerCrop()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            imageView.setOnClickListener(v -> mImageClickListener.onItemClick(getAdapterPosition(), imageView));
                            return false;
                        }
                    })
                    .into(imageView);
        }
    }
}