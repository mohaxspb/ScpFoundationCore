package ru.kuchanov.scpcore.monetization.model;

import java.util.Comparator;

/**
 * Created by mohax on 14.01.2017.
 * <p>
 * for scp_ru
 */
public class Subscription {
    /**
     * aka SKU
     */
    public String productId;
    public String price;
    public String title;

    public Subscription(String productId, String price, String title) {
        this.productId = productId;
        this.price = price;
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subscription item = (Subscription) o;

        return productId.equals(item.productId);
    }

    @Override
    public int hashCode() {
        return productId.hashCode();
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "productId='" + productId + '\'' +
                ", price='" + price + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    public static final Comparator<Subscription> COMPARATOR_SKU =
            (d, d1) -> d.productId.compareTo(d1.productId);

    public static final Comparator<Subscription> COMPARATOR_PRICE = (d, d1) -> {
        try {
            return Integer.valueOf(d.price.replaceAll("[^\\d.]", ""))
                    .compareTo(Integer.valueOf((d1.price.replaceAll("[^\\d.]", ""))));
        } catch (Exception e) {
            return d.price.compareTo(d1.price);
        }
    };
}