package com.boxfox.cross.service.auth.facebook;

import com.boxfox.cross.service.auth.Profile;
import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FacebookAuth {

    public static Profile validation(String accessToken){
        Profile profile = null;
        try {
            String url = "https://graph.facebook.com/v3.0/me?fields=id,name,picture,email&access_token=" + accessToken;
            profile = new Profile();
            JsonObject responseObj = new JsonObject(request(url));
            profile.setUid(responseObj.getString("id"));
            profile.setName(responseObj.getString("name"));
            profile.setEmail(responseObj.getString("email"));
            JsonObject picture_reponse = responseObj.getJsonObject("picture");
            JsonObject data_response = picture_reponse.getJsonObject("data");
            profile.setProfile(data_response.getString("url"));
        }catch(IOException e){
            e.printStackTrace();
        }
        return profile;
    }

    public static List<Profile> getFriends(String accessToken){
        List<Profile> friends = new ArrayList<>();
        try {
            String url = "https://graph.facebook.com/v3.0/me/friends?access_token=" + accessToken;
            JsonObject responseObj = new JsonObject(request(url));
            responseObj.getJsonArray("data").forEach( data -> {
                JsonObject obj = (JsonObject)data;
                Profile profile = new Profile();
                profile.setUid(obj.getString("id"));
                profile.setName(obj.getString("name"));
                friends.add(profile);
            });
        }catch(IOException e){
            e.printStackTrace();
        }
        return friends;
    }

    private static String request(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
}
