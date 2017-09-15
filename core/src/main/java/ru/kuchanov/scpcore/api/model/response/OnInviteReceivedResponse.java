package ru.kuchanov.scpcore.api.model.response;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.List;

import ru.kuchanov.scpcore.api.model.firebase.FirebaseObjectUser;

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