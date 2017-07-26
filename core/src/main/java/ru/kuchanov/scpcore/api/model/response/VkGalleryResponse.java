package ru.kuchanov.scpcore.api.model.response;

import com.vk.sdk.api.model.VKApiPhoto;

import java.util.List;

/**
 * Created by mohax on 10.02.2017.
 * <p>
 * for pacanskiypublic
 */
public class VkGalleryResponse {

    public Response response;

    public class Response {
        public int count;
        public List<VKApiPhoto> items;

        @Override
        public String toString() {
            return "Response{" +
                    "count=" + count +
                    ", items=" + items +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "LikesAddResponse{" +
                "response=" + response +
                '}';
    }
}