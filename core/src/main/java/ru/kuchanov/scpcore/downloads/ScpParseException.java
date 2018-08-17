package ru.kuchanov.scpcore.downloads;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class ScpParseException extends Throwable {

    private final String message;

    public ScpParseException(final String s) {
        super(s);
        message = s;
    }

    @Override
    public String getMessage() {
        return message;
    }
}