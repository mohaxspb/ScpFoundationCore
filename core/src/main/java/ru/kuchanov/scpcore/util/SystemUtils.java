package ru.kuchanov.scpcore.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;

import com.vk.sdk.util.VKUtil;

import org.jetbrains.annotations.Nullable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Currency;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import ru.kuchanov.scpcore.BaseApplication;
import timber.log.Timber;

/**
 * Created by mohax on 01.01.2017.
 */
public class SystemUtils {

    private static final SortedMap<Currency, Locale> currencyLocaleMap;

    static {
        currencyLocaleMap = new TreeMap<>((c1, c2) -> c1.getCurrencyCode().compareTo(c2.getCurrencyCode()));
        for (final Locale locale : Locale.getAvailableLocales()) {
            try {
                final Currency currency = Currency.getInstance(locale);
                currencyLocaleMap.put(currency, locale);
            } catch (final Exception ignored) {
            }
        }
    }

    public static String getCurrencySymbol(final String currencyCode) {
        final Currency currency = Currency.getInstance(currencyCode);
        return currency.getSymbol(currencyLocaleMap.get(currency));
    }

    public static String getCurrencySymbol2(@Nullable final String currencyCode) {
        Timber.d("getCurrencySymbol2: %s", currencyCode);
        if (currencyCode == null) {
            return "";
        } else {
            return Currency.getInstance(currencyCode).getSymbol(Locale.getDefault());
        }
    }

    private static String[] getCertificateFingerprints(final Context context) {
        return VKUtil.getCertificateFingerprint(context, context.getPackageName());
    }

    public static void printCertificateFingerprints() {
        final String[] fingerprints = getCertificateFingerprints(BaseApplication.getAppInstance());
        Timber.d("sha fingerprints");
        if (fingerprints != null) {
            for (final String sha1 : fingerprints) {
                Timber.d("sha1: %s", sha1);
            }
        } else {
            Timber.e(new NullPointerException(), "fingerprints arr is null!");
        }

        try {
            final Context context = BaseApplication.getAppInstance();
            @SuppressLint("PackageManagerGetSignatures") final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (final Signature signature : info.signatures) {
                final MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                final String hashKey = new String(Base64.encode(md.digest(), 0));
                Timber.i("printHashKey() Hash Key: %s", hashKey);
            }
        } catch (final Exception e) {
            Timber.e(e);
        }
    }

    public static String MD5(final String md5) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] array = md.digest(md5.getBytes());
            final StringBuilder sb = new StringBuilder();
            for (final byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (final NoSuchAlgorithmException e) {
            Timber.e(e);
        }
        return null;
    }

    public static PackageInfo getPackageInfo() {
        try {
            final PackageManager manager = BaseApplication.getAppInstance().getPackageManager();
            return manager.getPackageInfo(BaseApplication.getAppInstance().getPackageName(), 0);
        } catch (final Exception e) {
            Timber.e(e);
            return null;
        }
    }

    public static Spanned coloredTextForSnackBar(final Context context, final String text) {
//        String textColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(context, R.color.material_blue_gray_50)));
        final String textColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(context, android.R.color.white)));
        return Html.fromHtml("<font color=\"" + textColor + "\">" + text + "</font>");
    }

    public static Spanned coloredTextForSnackBar(final Context context, @StringRes final int text) {
        return coloredTextForSnackBar(context, context.getString(text));
    }
}
