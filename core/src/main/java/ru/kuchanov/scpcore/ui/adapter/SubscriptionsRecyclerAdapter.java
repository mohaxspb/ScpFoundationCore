package ru.kuchanov.scpcore.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.model.Subscription;
import ru.kuchanov.scpcore.monetization.util.InappHelper;
import timber.log.Timber;

/**
 * Created by Dante on 17.01.2016.
 * <p>
 * for scp_ru
 */
public class SubscriptionsRecyclerAdapter extends RecyclerView.Adapter<SubscriptionsRecyclerAdapter.ViewHolderText> {

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    private List<Subscription> mData;

    private SubscriptionClickListener mArticleClickListener;

    public void setArticleClickListener(SubscriptionClickListener articleClickListener) {
        mArticleClickListener = articleClickListener;
    }

    public SubscriptionsRecyclerAdapter() {
        BaseApplication.getAppComponent().inject(this);
    }

    public void setData(List<Subscription> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public SubscriptionsRecyclerAdapter.ViewHolderText onCreateViewHolder(ViewGroup parent, int viewType) {
        SubscriptionsRecyclerAdapter.ViewHolderText viewHolder;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_subscription, parent, false);
        viewHolder = new ViewHolderText(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SubscriptionsRecyclerAdapter.ViewHolderText holder, int position) {
        holder.bind(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class ViewHolderText extends RecyclerView.ViewHolder {

        @BindView(R2.id.title)
        TextView title;
        @BindView(R2.id.percent)
        TextView percent;

        ViewHolderText(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Subscription subscription) {
            Timber.d("bind with date: %s", subscription);
            Context context = itemView.getContext();
            float uiTextScale = mMyPreferenceManager.getUiTextScale();
            int textSizePrimary = context.getResources().getDimensionPixelSize(R.dimen.text_size_primary);

            itemView.setOnClickListener(v -> mArticleClickListener.onSubscriptionClicked(subscription));

            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizePrimary);
            String text = subscription.title.replace("(" + context.getString(R.string.app_name) + ")", "") + " - " + subscription.price;
            title.setText(text);

            //calculate percent
            long initialMonthCostsInMicros;
            if (FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)) {
                //do not show percent for no ads subs
                if (subscription.productId.equals(mData.get(0).productId) || subscription.productId.equals(mData.get(1).productId)) {
                    percent.setVisibility(View.GONE);
                    return;
                } else {
                    percent.setVisibility(View.VISIBLE);
                }
                initialMonthCostsInMicros = mData.get(1).price_amount_micros;
            } else {
                if (subscription.productId.equals(mData.get(0).productId)) {
                    percent.setVisibility(View.GONE);
                } else {
                    percent.setVisibility(View.VISIBLE);
                }
                initialMonthCostsInMicros = mData.get(0).price_amount_micros;
            }
            int months = InappHelper.getMonthsFromSku(subscription.productId);
            long oneMonthPriceForMonths = initialMonthCostsInMicros * months;
            if (oneMonthPriceForMonths != 0) {
                long percentCosts = 100L - subscription.price_amount_micros * 100L / oneMonthPriceForMonths;
                percent.setText("-" + percentCosts + "%");
            }
        }
    }

    public interface SubscriptionClickListener {
        void onSubscriptionClicked(Subscription article);
    }
}