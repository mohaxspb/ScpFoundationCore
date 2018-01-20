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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Currency;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import timber.log.Timber;

/**
 * Created by mohax on 01.01.2017.
 * <p>
 * for scp_ru
 */
public class SystemUtils {

    public static SortedMap<Currency, Locale> currencyLocaleMap;

    static {
        currencyLocaleMap = new TreeMap<Currency, Locale>(new Comparator<Currency>() {
            public int compare(Currency c1, Currency c2) {
                return c1.getCurrencyCode().compareTo(c2.getCurrencyCode());
            }
        });
        for (Locale locale : Locale.getAvailableLocales()) {
            try {
                Currency currency = Currency.getInstance(locale);
                currencyLocaleMap.put(currency, locale);
            } catch (Exception e) {
            }
        }
    }


    public static String getCurrencySymbol(String currencyCode) {
        Currency currency = Currency.getInstance(currencyCode);
//        System.out.println(currencyCode + ":-" + currency.getSymbol(currencyLocaleMap.get(currency)));
        return currency.getSymbol(currencyLocaleMap.get(currency));
    }

    private static String[] getCertificateFingerprints(Context context) {
        return VKUtil.getCertificateFingerprint(context, context.getPackageName());
    }

    public static void printCertificateFingerprints() {
        String[] fingerprints = getCertificateFingerprints(BaseApplication.getAppInstance());
        Timber.d("sha fingerprints");
        for (String sha1 : fingerprints) {
//            System.out.println("sha1: " + sha1);
            Timber.d("sha1: %s", sha1);
        }

        try {
            Context context = BaseApplication.getAppInstance();
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Timber.i("printHashKey() Hash Key: %s", hashKey);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public static String MD5(String md5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Timber.e(e);
        }
        return null;
    }

    public static PackageInfo getPackageInfo() {
        try {
            PackageManager manager = BaseApplication.getAppInstance().getPackageManager();
            return manager.getPackageInfo(BaseApplication.getAppInstance().getPackageName(), 0);
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }

    public static Spanned coloredTextForSnackBar(Context context, String text) {
        String textColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(context, R.color.material_blue_gray_50)));
        return Html.fromHtml("<font color=\"" + textColor + "\">" + text + "</font>");
    }

    public static Spanned coloredTextForSnackBar(Context context, @StringRes int text) {
        return coloredTextForSnackBar(context, context.getString(text));
    }
}