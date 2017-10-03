package ru.kuchanov.scpcore.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import timber.log.Timber;

import static ru.kuchanov.scpcore.util.IntentUtils.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

/**
 * Created by Ivan Semkin on 4/27/2017.
 * <p>
 * for scp_ru
 */
public class StorageUtils {

    public static String saveImageToGallery(Activity activity, Bitmap image) {
        int permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return MediaStore.Images.Media.insertImage(
                    activity.getContentResolver(),
                    image,
                    activity.getString(R.string.image_title),
                    activity.getString(R.string.image_description)
            );
        } else {
            ActivityCompat.requestPermissions(
                    activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        return null;
    }

    public static boolean fileExistsInAssets(String path) {
        try {
            List<String> assetsFiles = Arrays.asList(BaseApplication.getAppInstance().getResources().getAssets().list(""));
//            Timber.d("assetsFiles: %s", assetsFiles);
            return assetsFiles.contains(path);
        } catch (IOException e) {
            Timber.e(e);
            return false;
        }
    }

    public static String readFromAssets(Context context, String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename), "UTF-8"));

        // do reading, usually loop until end of file reading
        StringBuilder sb = new StringBuilder();
        String mLine = reader.readLine();
        while (mLine != null) {
            sb.append(mLine); // process line
            mLine = reader.readLine();
        }
        reader.close();
        return sb.toString();
    }
}