package ru.kuchanov.scpcore.monetization.util.admob;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import com.google.android.gms.ads.AdRequest;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.BuildConfig;
import ru.kuchanov.scpcore.util.SystemUtils;

/**
 * Created by mohax on 03.08.2017.
 * <p>
 * for ScpCore
 */
public class AdMobHelper {

    public static AdRequest buildAdRequest() {
        final AdRequest.Builder adRequestBuilder = new AdRequest.Builder();

        if (BuildConfig.FLAVOR_mode.equals("dev")) {
            @SuppressLint("HardwareIds")
            final String androidId = Settings.Secure.getString(BaseApplication.getAppInstance().getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceId = SystemUtils.MD5(androidId);
            if (deviceId != null) {
                deviceId = deviceId.toUpperCase();
                adRequestBuilder.addTestDevice(deviceId);
            }
            adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        }
        return adRequestBuilder.build();
    }
}
