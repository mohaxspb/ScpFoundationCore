package ru.kuchanov.scpcore.api.model.response;

import java.io.Serializable;

/**
 * Created by mohax on 05.05.2017.
 * <p>
 * for scp-ru
 */
public class OnInviteReceivedResponse implements Serializable {

    public boolean status;

    @Override
    public String toString() {
        return "OnInviteReceivedResponse{" +
                "status=" + status +
                '}';
    }
}