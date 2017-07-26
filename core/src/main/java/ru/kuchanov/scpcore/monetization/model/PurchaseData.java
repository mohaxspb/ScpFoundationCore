package ru.kuchanov.scpcore.monetization.model;

/**
 * Created by mohax on 03.06.2017.
 * <p>
 * for ScpFoundationRu
 */
public class PurchaseData {

    public String autoRenewing;

    public String orderId;

    public String packageName;

    public String productId;

    public String purchaseTime;

    public String developerPayload;

    public String purchaseToken;

    @Override
    public String toString() {
        return "PurchaseData{" +
                "autoRenewing='" + autoRenewing + '\'' +
                ", orderId='" + orderId + '\'' +
                ", packageName='" + packageName + '\'' +
                ", productId='" + productId + '\'' +
                ", purchaseTime='" + purchaseTime + '\'' +
                ", developerPayload='" + developerPayload + '\'' +
                ", purchaseToken='" + purchaseToken + '\'' +
                '}';
    }
}