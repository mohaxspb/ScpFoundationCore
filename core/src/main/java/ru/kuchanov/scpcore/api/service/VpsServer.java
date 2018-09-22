package ru.kuchanov.scpcore.api.service;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import ru.kuchanov.scpcore.api.model.response.LeaderBoardResponse;
import ru.kuchanov.scpcore.api.model.response.OnInviteReceivedResponse;
import ru.kuchanov.scpcore.api.model.response.PurchaseValidateResponse;
import rx.Observable;

/**
 * Created by mohax on 06.05.2017.
 * <p>
 * for scp_ru
 */
public interface VpsServer {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            InviteAction.RECEIVED,
            InviteAction.SENT,
    })
    @interface InviteAction {

        String RECEIVED = "inviteReceived";
        String SENT = "inviteSent";
    }

    @Deprecated
    @GET("scp-ru-1/LeaderBoard")
    Observable<LeaderBoardResponse> getLeaderboard(@Query("lang") String lang);

    @FormUrlEncoded
    @POST("scp-ru-1/OnInviteReceived")
    Observable<OnInviteReceivedResponse> onInviteReceived(
            @InviteAction @Field("action") String action,
            @Field("inviteId") String inviteId,
            @Field("lang") String lang,
            @Field("isNewOne") boolean isNewOne
    );

    @FormUrlEncoded
    @POST("scp-ru-1/OnInviteReceived")
    Observable<OnInviteReceivedResponse> onInviteSent(
            @InviteAction @Field("action") String action,
            @Field("inviteId") String inviteId,
            @Field("lang") String lang,
            @Field("fcmToken") String fcmToken
    );

    @GET("purchaseValidation/validate")
    Observable<PurchaseValidateResponse> validatePurchase(
            @Query("isSubscription") boolean isSubscription,
            @Query("package") String packageName,
            @Query("sku") String sku,
            @Query("purchaseToken") String purchaseToken
    );

    @GET("purchaseValidation/validate")
    Call<PurchaseValidateResponse> validatePurchaseSync(
            @Query("isSubscription") boolean isSubscription,
            @Query("package") String packageName,
            @Query("sku") String sku,
            @Query("purchaseToken") String purchaseToken
    );
}