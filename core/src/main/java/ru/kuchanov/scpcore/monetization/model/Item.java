package ru.kuchanov.scpcore.monetization.model;

import com.google.gson.GsonBuilder;

/**
 * Created by mohax on 14.01.2017.
 * <p>
 * for scp_ru
 */
public class Item {

    public PurchaseData purchaseData;
    private final String signature;
    public String sku;
    private final String continuationToken;

    public Item(
            final String purchaseData,
            final String signature,
            final String sku,
            final String continuationToken
    ) {
        super();
        this.purchaseData = new GsonBuilder().create().fromJson(purchaseData, PurchaseData.class);
        this.signature = signature;
        this.sku = sku;
        this.continuationToken = continuationToken;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Item item = (Item) o;

        return sku.equals(item.sku);
    }

    @Override
    public int hashCode() {
        return sku.hashCode();
    }

    @Override
    public String toString() {
        return "Item{" +
                "purchaseData='" + purchaseData + '\'' +
                ", signature='" + signature + '\'' +
                ", sku='" + sku + '\'' +
                ", continuationToken='" + continuationToken + '\'' +
                '}';
    }
}