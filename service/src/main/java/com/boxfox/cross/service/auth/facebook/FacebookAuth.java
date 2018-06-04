package com.boxfox.cross.service.auth.facebook;

import com.boxfox.cross.service.model.Profile;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.boxfox.cross.service.network.RequestService.request;

public class FacebookAuth {

    public static Future<Profile> validation(String accessToken){
        Future<Profile> future = Future.future();
            String url = "https://graph.facebook.com/v3.0/me?fields=id,name,picture,email&access_token=" + accessToken;
            request(url).setHandler(event -> {
                if(event.succeeded()){
                    Profile profile = new Profile();
                    JsonObject responseObj = new JsonObject(event.result());
                    profile.setUid(responseObj.getString("id"));
                    profile.setName(responseObj.getString("name"));
                    profile.setEmail(responseObj.getString("email"));
                    JsonObject picture_reponse = responseObj.getJsonObject("picture");
                    if(picture_reponse!=null) {
                        JsonObject data_response = picture_reponse.getJsonObject("data");
                        profile.setProfile(data_response.getString("url"));
                    }
                    future.complete(profile);
                }else{
                    future.fail(event.cause());
                }
            });
        return future;
    }

    public static Future<List<Profile>> getFriends(String accessToken){
        Future<List<Profile>> future = Future.future();
            String url = "https://graph.facebook.com/v3.0/me/friends?access_token=" + accessToken;
            request(url).setHandler(event -> {
                if(event.succeeded()) {
                    List<Profile> friends = new ArrayList<>();
                    JsonObject responseObj = new JsonObject(event.result());
                    responseObj.getJsonArray("data").forEach(data -> {
                        JsonObject obj = (JsonObject) data;
                        Profile profile = new Profile();
                        profile.setUid(obj.getString("id"));
                        profile.setName(obj.getString("name"));
                        friends.add(profile);
                    });
                    future.complete(friends);
                }else{
                    future.fail(event.cause());
                }
            });
        return future;
    }
}
