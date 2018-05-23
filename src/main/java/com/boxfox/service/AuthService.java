package com.boxfox.service;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;

public class AuthService {

    public AuthService(){
    }

    public boolean signin(String username, String password){

        return true;
    }

    public JsonObject signinWithFacebook(String accessToken){
        JsonObject result = new JsonObject();

        return result;
    }

    public static String createToken(Vertx vertx, String username){
        JWTAuth jwt = createJWTAuth(vertx);
        String token = jwt.generateToken(new JsonObject().put("sub", username),  new JWTOptions());
        return token;
    }

    public static JWTAuth createJWTAuth(Vertx vertx){
        JsonObject config = new JsonObject()
                .put("public-key", "KEYCLOAK_PUBLIC_KEY")
                .put("permissionsClaimKey", "realm_access/roles");
        JWTAuth authProvider = JWTAuth.create(vertx, config);
        return authProvider;
    }
}
