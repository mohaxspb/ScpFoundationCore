package ru.kuchanov.scpcore.monetization.model;

import android.text.TextUtils;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Comparator;

import ru.kuchanov.scpcore.monetization.util.InAppHelper;
import timber.log.Timber;

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
    @InAppHelper.InappType
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
            String productId,
            String type,
            String price,
            long price_amount_micros,
            String price_currency_code,
            String title,
            String description,
            String subscriptionPeriod,
            String freeTrialPeriod,
            String introductoryPrice,
            long introductoryPriceAmountMicros,
            String introductoryPricePeriod,
            int introductoryPriceCycles
    ) {
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

    public int freeTrialPeriodInDays() {
        if (TextUtils.isEmpty(freeTrialPeriod)) {
            return NO_TRIAL_PERIOD;
        }
//        try {
//            Duration dur = DatatypeFactoryImpl.newInstance().newDuration(freeTrialPeriod);
//            return dur.getDays();
//        } catch (DatatypeConfigurationException e) {
//            Timber.e(e);
//            return NO_TRIAL_PERIOD;
//        }
        //TODO replace with Jode-time... or find java7 variant
        Duration duration = Duration.parse("PT20.345S");
        return (int) duration.toDays();
    }
}