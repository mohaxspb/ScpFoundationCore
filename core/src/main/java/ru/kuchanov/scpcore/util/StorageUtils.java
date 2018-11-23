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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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

    public static String saveImageToGallery(final Activity activity, final Bitmap image) {
        final int permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
            );
        }
        return null;
    }

    public static void deleteCachedImages(final Context context) {
        deleteRecursive(new File(context.getFilesDir(), "/image"));
    }

    public static void deleteRecursive(final File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (final File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    public static int cachedImagesFilesCount(final Context context) {
        final File[] images = new File(context.getFilesDir(), "/image").listFiles();
        return images != null ? images.length : 0;
    }

    public static long cachedImagesFolderSize(final Context context) {
        return folderSize(new File(context.getFilesDir(), "/image"));
    }

    public static long folderSize(final File directory) {
        long length = 0;
        final File[] files = directory.listFiles();
        if (files == null) {
            return length;
        }
        for (final File file : files) {
            if (file.isFile()) {
                length += file.length();
            } else {
                length += folderSize(file);
            }
        }
        return length;
    }

    public static String humanReadableByteCount(final long bytes, final boolean si) {
        final int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        final int exp = (int) (Math.log(bytes) / Math.log(unit));
        final String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static boolean fileExistsInAssets(final String path) {
        try {
            final List<String> assetsFiles = Arrays.asList(BaseApplication.getAppInstance().getResources().getAssets().list(""));
            return assetsFiles.contains(path);
        } catch (final IOException e) {
            Timber.e(e);
            return false;
        }
    }

    public static String readFromAssets(final Context context, final String filename) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename), "UTF-8"));

        // do reading, usually loop until end of file reading
        final StringBuilder sb = new StringBuilder();
        String mLine = reader.readLine();
        while (mLine != null) {
            sb.append(mLine); // process line
            mLine = reader.readLine();
        }
        reader.close();
        return sb.toString();
    }

    public static void writeFileOnDevice(final String fileName, final String content) {
        try {
            final File root = new File(BaseApplication.getAppInstance().getFilesDir(), "test");
            if (!root.exists()) {
                root.mkdirs();
            }
            final File myFile = new File(root, fileName);
            if (!myFile.exists()) {
                myFile.createNewFile();
            }
            final FileWriter writer = new FileWriter(myFile);
            writer.write(content);
            writer.flush();
            writer.close();
            Timber.d("Saved: %s", myFile.getAbsolutePath());
        } catch (final IOException e) {
            Timber.e(e);
        }
    }
}