package ru.kuchanov.scpcore.api.error;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class ScpException extends Throwable {
    private String mUrl;

    public ScpException(Throwable cause, String url) {
        super(cause);
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }
}