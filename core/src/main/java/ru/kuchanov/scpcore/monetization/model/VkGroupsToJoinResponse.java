package ru.kuchanov.scpcore.monetization.model;

import java.util.List;

/**
 * Created by mohax on 24.02.2017.
 * <p>
 * for pacanskiypublic
 */
public class VkGroupsToJoinResponse {

    public List<VkGroupToJoin> items;

    @Override
    public String toString() {
        return "ApplicationsResponse{" +
                "items=" + items +
                '}';
    }
}