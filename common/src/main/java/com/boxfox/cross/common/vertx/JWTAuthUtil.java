package com.boxfox.cross.common.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;

public class JWTAuthUtil {

    public static JWTAuth createAuth(Vertx vertx) {
        JsonObject config = new JsonObject()
                .put("public-key", "KEYCLOAK_PUBLIC_KEY")
                .put("permissionsClaimKey", "realm_access/roles");
        JWTAuth authProvider = JWTAuth.create(vertx, config);
        return authProvider;
    }


    public static String createToken(Vertx vertx, String uid) {
        JWTAuth jwt = createAuth(vertx);
        String token = jwt.generateToken(new JsonObject().put("sub", uid), new JWTOptions());
        return token;
    }
}
