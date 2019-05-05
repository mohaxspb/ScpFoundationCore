package ru.kuchanov.scpcore.monetization.model;

import android.text.TextUtils;

import org.joda.time.Days;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

import java.util.Comparator;

import ru.kuchanov.scpcore.monetization.util.InappPurchaseUtil;

/**
 * Created by mohax on 14.01.2017.
 * <p>
 * for scp_ru
 */
public class Subscription {

    private static final int NO_TRIAL_PERIOD = -1;

    /**
     * aka SKU
     */
    public String productId;

    @InappPurchaseUtil.InappType
    public String type;

    /**
     * Formatted price of the item, including its currency sign. The price does not include tax.
     */
    public String price;

    /**
     * Price in micro-units, where 1,000,000 micro-units equal one unit of the currency. For example, if price is "€7.99", price_amount_micros is "7990000". This value represents the localized, rounded price for a particular currency.
     */
    public long price_amount_micros;

    /**
     * ISO 4217 currency code for price. For example, if price is specified in British pounds sterling, price_currency_code is "GBP".
     */
    public String price_currency_code;

    public String title;

    public String description;

    /**
     * Subscription period, specified in ISO 8601 format. For example, P1W equates to one week, P1M equates to one month, P3M equates to three months, P6M equates to six months, and P1Y equates to one year.
     * Note: Returned only for subscriptions.
     */
    public String subscriptionPeriod;

    /**
     * Trial period configured in Google Play Console, specified in ISO 8601 format. For example, P7D equates to seven days. To learn more about free trial eligibility, see In-app Subscriptions.
     * Note: Returned only for subscriptions which have a trial period configured.
     */
    public String freeTrialPeriod;

    /**
     * Formatted introductory price of a subscription, including its currency sign, such as €3.99. The price doesn't include tax.
     * Note: Returned only for subscriptions which have an introductory period configured.
     */
    public String introductoryPrice;

    /**
     * Introductory price in micro-units. The currency is the same as price_currency_code.
     * Note: Returned only for subscriptions which have an introductory period configured.
     */
    public long introductoryPriceAmountMicros;

    /**
     * The billing period of the introductory price, specified in ISO 8601 format.
     * Note: Returned only for subscriptions which have an introductory period configured.
     */
    public String introductoryPricePeriod;

    /**
     * The number of subscription billing periods for which the user will be given the introductory price, such as 3.
     */
    public int introductoryPriceCycles;

    /**
     * @see <a href="https://developer.android.com/google/play/billing/billing_reference.html#getSkuDetails">docs</a>
     */
    public Subscription(
            final String productId,
            final String type,
            final String price,
            final long price_amount_micros,
            final String price_currency_code,
            final String title,
            final String description,
            final String subscriptionPeriod,
            final String freeTrialPeriod,
            final String introductoryPrice,
            final long introductoryPriceAmountMicros,
            final String introductoryPricePeriod,
            final int introductoryPriceCycles
    ) {
        super();
        this.productId = productId;
        this.type = type;
        this.price = price;
        this.price_amount_micros = price_amount_micros;
        this.price_currency_code = price_currency_code;
        this.title = title;
        this.description = description;
        this.subscriptionPeriod = subscriptionPeriod;
        this.freeTrialPeriod = freeTrialPeriod;
        this.introductoryPrice = introductoryPrice;
        this.introductoryPriceAmountMicros = introductoryPriceAmountMicros;
        this.introductoryPricePeriod = introductoryPricePeriod;
        this.introductoryPriceCycles = introductoryPriceCycles;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "productId='" + productId + '\'' +
                ", type='" + type + '\'' +
                ", price='" + price + '\'' +
                ", price_amount_micros=" + price_amount_micros +
                ", price_currency_code='" + price_currency_code + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", subscriptionPeriod='" + subscriptionPeriod + '\'' +
                ", freeTrialPeriod='" + freeTrialPeriod + '\'' +
                ", introductoryPrice='" + introductoryPrice + '\'' +
                ", introductoryPriceAmountMicros=" + introductoryPriceAmountMicros +
                ", introductoryPricePeriod='" + introductoryPricePeriod + '\'' +
                ", introductoryPriceCycles=" + introductoryPriceCycles +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Subscription item = (Subscription) o;

        return productId.equals(item.productId);
    }

    @Override
    public int hashCode() {
        return productId.hashCode();
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

    public static final Comparator<Subscription> COMPARATOR_MONTH = (d, d1) ->
            Integer.valueOf(d.freeTrialPeriodInDays()).compareTo(d1.freeTrialPeriodInDays());

    public int freeTrialPeriodInDays() {
        if (TextUtils.isEmpty(freeTrialPeriod)) {
            return NO_TRIAL_PERIOD;
        }
        //java 8 only, ***!
        //replace with Jode-time... or find java7 variant
//        Duration duration = Duration.parse("PT20.345S");
//        return (int) duration.toDays();

        final PeriodFormatter formatter = ISOPeriodFormat.standard();
        final Period p = formatter.parsePeriod(freeTrialPeriod);

        final Days days = p.toStandardDays();
        return days.getDays();
    }
}
