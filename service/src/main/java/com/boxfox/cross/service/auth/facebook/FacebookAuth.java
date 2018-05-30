package com.boxfox.cross.service.auth.facebook;

import com.boxfox.cross.service.auth.Profile;
import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FacebookAuth {

    public static Profile validation(String accessToken){
        Profile profile = null;
        try {
            String url = "https://graph.facebook.com/v2.12/me?fields=id,name,picture,email&access_token=" + accessToken;
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
            profile = new Profile();
            JsonObject responseObj = new JsonObject(response.toString());
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
}
