package com.boxfox.cross.service.auth;

import com.boxfox.cross.service.model.Profile;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import io.vertx.core.Future;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleAuth {
    private static GoogleIdTokenVerifier verifier;
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    public GoogleAuth(){
        verifier = new GoogleIdTokenVerifier.Builder(HTTP_TRANSPORT, JSON_FACTORY)
                .setAudience(Collections.singletonList("AIzaSyCazOMbb4M8fDscVrAxLuGS5Trr0e7M4pk"))
                .build();
    }

    public static Future<Profile> validation(String accessnToke){
        Future<Profile> future = Future.future();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    GoogleIdToken idToken = verifier.verify(accessnToke);
                    if (idToken != null) {
                        GoogleIdToken.Payload payload = idToken.getPayload();

                        String userId = payload.getSubject();
                        String email = payload.getEmail();
                        String name = (String) payload.get("name");
                        String pictureUrl = (String) payload.get("picture");
                        Profile profile = new Profile();
                        profile.setUid(userId);
                        profile.setEmail(email);
                        profile.setName(name);
                        profile.setProfile(pictureUrl);
                    } else {
                        future.fail("Invalid ID token.");
                    }
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                    future.fail(e);
                } catch (IOException e) {
                    e.printStackTrace();
                    future.fail(e);
                }
            }
        }).start();
        return future;
    }
}
