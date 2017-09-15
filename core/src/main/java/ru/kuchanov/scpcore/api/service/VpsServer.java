package ru.kuchanov.scpcore.api.service;

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

    @GET("scp-ru-1/LeaderBoard")
    Observable<LeaderBoardResponse> getLeaderboard(@Query("lang") String lang);

    @FormUrlEncoded
    @POST("scp-ru-1/inviteReceived")
    Observable<OnInviteReceivedResponse> onInviteReceived(
            @Field("inviteId") String inviteId,
            @Field("isNewOne") boolean isNewOne,
            @Field("lang") String lang
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