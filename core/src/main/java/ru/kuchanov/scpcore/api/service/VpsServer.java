package ru.kuchanov.scpcore.api.service;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import ru.kuchanov.scpcore.api.model.response.OnInviteReceivedResponse;
import rx.Observable;
import rx.Single;

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

    @FormUrlEncoded
    @POST("OnInviteReceived")
    Observable<OnInviteReceivedResponse> onInviteReceived(
            @InviteAction @Field("action") String action,
            @Field("inviteId") String inviteId,
            @Field("lang") String lang,
            @Field("isNewOne") boolean isNewOne
    );

    @FormUrlEncoded
    @POST("OnInviteReceived")
    Observable<OnInviteReceivedResponse> onInviteSent(
            @InviteAction @Field("action") String action,
            @Field("inviteId") String inviteId,
            @Field("lang") String lang,
            @Field("fcmToken") String fcmToken
    );

    @GET("MyServlet")
    Single<String> getFirebaseTokenForVkUserId(
            @Query("provider") String provider,
            @Query("id") String userId

    );
}