package ru.kuchanov.scpcore.api.error;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class ScpNoSearchResultsException extends Throwable {
    private String message;

    public ScpNoSearchResultsException(String s) {
        super(s);
        message = s;
    }

    public String getMessage() {
        return message;
    }
}