package ru.kuchanov.scpcore.api.model.response;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mohax on 05.08.2017.
 * <p>
 * for ScpCore
 */
public class PurchaseValidateResponse {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            PurchaseValidationStatus.STATUS_VALID,
            PurchaseValidationStatus.STATUS_INVALID,
            PurchaseValidationStatus.STATUS_GOOGLE_SERVER_ERROR
    })
    public @interface PurchaseValidationStatus {
        int STATUS_VALID = 0;
        int STATUS_INVALID = 1;
        int STATUS_GOOGLE_SERVER_ERROR = 2;
    }

    @PurchaseValidationStatus
    private int status;

    @PurchaseValidationStatus
    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "PurchaseValidateResponse{" +
                "status=" + status +
                '}';
    }
}