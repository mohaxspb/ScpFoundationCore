package ru.kuchanov.scpcore.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;

import java.util.List;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;

/**
 * Created by mohax on 08.01.2017.
 * <p>
 * for scp_ru
 */
public class IntentUtils {

    static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 987;

    public static void shareUrl(String url) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_SEND);
        String fullMessage = BaseApplication.getAppInstance().getString(
                R.string.share_link_text,
                url,
                BaseApplication.getAppInstance().getPackageName()
        );
        intent.putExtra(Intent.EXTRA_TEXT, fullMessage);
        intent.setType("text/plain");
        BaseApplication.getAppInstance().startActivity(
                Intent.createChooser(intent, BaseApplication.getAppInstance().getResources().getText(R.string.share_choser_text))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }

    public static void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        BaseApplication.getAppInstance().startActivity(
                Intent.createChooser(intent, BaseApplication.getAppInstance().getResources().getText(R.string.browser_choser_text))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        );
    }

    public static void shareBitmapWithText(AppCompatActivity activity, String text, Bitmap bitmap) {
        String pathOfBmp = StorageUtils.saveImageToGallery(activity, bitmap);
        if (pathOfBmp == null) {
            Toast.makeText(activity, R.string.error_getting_path_to_image, Toast.LENGTH_SHORT).show();
            return;
        }
        Uri bmpUri = Uri.parse(pathOfBmp);
        String fullMessage = BaseApplication.getAppInstance().getString(
                R.string.share_link_text,
                text,
                BaseApplication.getAppInstance().getPackageName()
        );
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, fullMessage);
        shareIntent.setType("image/png");

        activity.startActivity(shareIntent);
    }

    public static void firebaseInvite(FragmentActivity activity) {
        String message = activity.getString(R.string.invitation_message);
        if (message.length() > Constants.Firebase.INVITE_CTA_MAX_LENGTH) {
            message = message.substring(0, Constants.Firebase.INVITE_CTA_MAX_LENGTH);
        }
        String cta = activity.getString(R.string.invitation_cta);
        if (cta.length() > Constants.Firebase.INVITE_CTA_MAX_LENGTH) {
            cta = cta.substring(0, Constants.Firebase.INVITE_CTA_MAX_LENGTH);
        }
        Intent intent = new AppInviteInvitation.IntentBuilder(activity.getString(R.string.invitation_title))
                .setMessage(message)
                .setCallToActionText(cta)
                .setDeepLink(Uri.parse(activity.getString(R.string.firebase_deep_link)))
                .build();
        activity.startActivityForResult(intent, Constants.Firebase.REQUEST_INVITE);
    }

    public static void tryOpenPlayMarket(Context context, String appId) {
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.market_url, appId)));
        marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        checkAndStart(context, marketIntent, R.string.start_market_error);
    }

    private static boolean checkIntent(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        return activities != null && activities.size() > 0;
    }

    private static void checkAndStart(Context context, Intent intent, int errorRes) {
        if (checkIntent(context, intent)) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, context.getString(errorRes), Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            return false;
        }
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return !list.isEmpty();
    }
}