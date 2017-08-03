package ru.kuchanov.scpcore.api.model.remoteconfig;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by mohax on 29.04.2017.
 * <p>
 * for scp_ru
 */
public class AppLangVersionsJson {

    public List<AppLangVersion> langs;

    public static class AppLangVersion {

        public String code;
        public String title;
        @SerializedName("package")
        public String appPackage;
        public String icon;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AppLangVersion appLangVersion = (AppLangVersion) o;

            return code.equals(appLangVersion.code);
        }

        @Override
        public int hashCode() {
            return code.hashCode();
        }

        @Override
        public String toString() {
            return "AppLangVersion{" +
                    "id=" + code +
                    ", title='" + title + '\'' +
                    ", appPackage=" + appPackage +
                    ", icon=" + icon +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "LevelsJson{" +
                "mAppLangVersions=" + langs +
                '}';
    }
}