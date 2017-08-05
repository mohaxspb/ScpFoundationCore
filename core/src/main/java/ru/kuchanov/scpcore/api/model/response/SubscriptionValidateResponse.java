package ru.kuchanov.scpcore.api.model.response;

/**
 * Created by mohax on 05.08.2017.
 * <p>
 * for ScpCore
 */
public class SubscriptionValidateResponse {

    public boolean autoRenewing;
    public String countryCode;
    public String developerPayload;
    public int expiryTimeMillis;
    public String kind;
    public int paymentState;
    public int priceAmountMicros;
    public String priceCurrencyCode;
    public int startTimeMillis;

    @Override
    public String toString() {
        return "SubscriptionValidateResponse{" +
                "autoRenewing=" + autoRenewing +
                ", countryCode='" + countryCode + '\'' +
                ", developerPayload='" + developerPayload + '\'' +
                ", expiryTimeMillis=" + expiryTimeMillis +
                ", kind='" + kind + '\'' +
                ", paymentState=" + paymentState +
                ", priceAmountMicros=" + priceAmountMicros +
                ", priceCurrencyCode='" + priceCurrencyCode + '\'' +
                ", startTimeMillis=" + startTimeMillis +
                '}';
    }
}