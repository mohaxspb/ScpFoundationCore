package ru.kuchanov.scpcore.monetization.model;

import com.google.gson.GsonBuilder;

/**
 * Created by mohax on 14.01.2017.
 * <p>
 * for scp_ru
 */
public class Item {

    public PurchaseData purchaseData;
    public String signature;
    public String sku;
    public String continuationToken;

    public Item(String purchaseData, String signature, String sku, String continuationToken) {
        this.purchaseData = new GsonBuilder().create().fromJson(purchaseData, PurchaseData.class);
        this.signature = signature;
        this.sku = sku;
        this.continuationToken = continuationToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

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