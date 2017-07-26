package ru.kuchanov.scpcore.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;

import ru.kuchanov.scpcore.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by mohax on 08.06.2017.
 * <p>
 * for scp_en
 */
public class MigrationUtils {

    /**
     * @return title of article if it exists, null otherwise
     */
    public static String hasFavoriteWithURL(Context ctx, String url) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getString(R.string.pref_favorites), MODE_PRIVATE);
        return sharedPreferences.getString(url, null);
    }

    public static Set<String> getAllReadUrls(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.read_articles), Context.MODE_PRIVATE);
        return sharedPreferences.getAll().keySet();
    }

    public static boolean hasReadWithURL(Context ctx, String url) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ctx.getString(R.string.read_articles), Context.MODE_PRIVATE);
        return sharedPreferences.contains(url);
    }

    public static Map<String, String> getAllFavorites(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.pref_favorites), Context.MODE_PRIVATE);
        return (Map<String, String>) sharedPreferences.getAll();
    }

    public static boolean hasDataToRestore(Context context) {
        return !getAllFavorites(context).keySet().isEmpty() || !getAllReadUrls(context).isEmpty();
    }
}