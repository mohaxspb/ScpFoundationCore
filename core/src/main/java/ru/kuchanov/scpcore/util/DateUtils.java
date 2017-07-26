package ru.kuchanov.scpcore.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by mohax on 22.01.2017.
 * <p>
 * for scp_ru
 */
public class DateUtils {

    //17 Jan 2017 21:16
    private static final String SITE_DATE_FORMAT_0 = "dd MMM yyyy HH:mm";
    private static final String SITE_DATE_FORMAT = "HH:mm dd.MM.yyyy";

    private static final String SIMPLE_DATE_FORMAT = "dd.MM.yyyy";

    public static String getArticleDateShortFormat(String rawDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(SITE_DATE_FORMAT, Locale.ENGLISH);
        Date convertedDate;
        try {
            convertedDate = dateFormat.parse(rawDate);
            SimpleDateFormat sdf = new SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.getDefault());
            return sdf.format(convertedDate);
        } catch (Exception e) {
            try {
                dateFormat = new SimpleDateFormat(SITE_DATE_FORMAT_0, Locale.ENGLISH);
                convertedDate = dateFormat.parse(rawDate);
                SimpleDateFormat sdf = new SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.getDefault());
                return sdf.format(convertedDate);
            } catch (Exception error) {
                Timber.e(error, "error while parse date");
                return rawDate;
            }
        }
    }
}