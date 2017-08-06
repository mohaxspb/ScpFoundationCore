package ru.kuchanov.scpcore.api.model.response;

/**
 * Created by mohax on 05.08.2017.
 * <p>
 * for ScpCore
 */
public class ProductValidateResponse {

    public int consumptionState;
    public String developerPayload;
    public String kind;
    public int purchaseState;
    public long purchaseTimeMillis;

    @Override
    public String toString() {
        return "ProductValidateResponse{" +
                "consumptionState=" + consumptionState +
                ", developerPayload='" + developerPayload + '\'' +
                ", kind='" + kind + '\'' +
                ", purchaseState=" + purchaseState +
                ", purchaseTimeMillis=" + purchaseTimeMillis +
                '}';
    }
}