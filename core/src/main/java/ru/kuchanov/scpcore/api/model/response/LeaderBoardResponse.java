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
public class LeaderBoardResponse implements Serializable {

    @Expose
    public long lastUpdated;

    @Expose
    public String timeZone;

    @Expose
    public List<FirebaseObjectUser> users;

    public LeaderBoardResponse(long lastUpdated, String timeZone, List<FirebaseObjectUser> users) {
        this.lastUpdated = lastUpdated;
        this.timeZone = timeZone;
        this.users = users;
    }

    @Override
    public String toString() {
        return "LeaderBoardResponse{" +
                "lastUpdated=" + lastUpdated +
                ", timeZone='" + timeZone + '\'' +
                ", users=" + users +
                '}';
    }
}