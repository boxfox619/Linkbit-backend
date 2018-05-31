package com.boxfox.cross.common.vertx.middleware;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;

public class JWTAuthHandlerImpl implements JWTAuthHandler {

    @Override
    public void handle(RoutingContext ctx) {
        if(ctx.user()!=null) {
            ctx.user().isAuthorized("", res -> {
                if (res.succeeded()) {
                    boolean hasAuthority = res.result();
                    if (hasAuthority) {
                        System.out.println("User has the authority");
                    } else {
                        System.out.println("User does not have the authority");
                    }

                } else {
                    res.cause().printStackTrace();
                }
            });
        }else{
            String token = ctx.getCookie("token").getValue();
        }
        ctx.next();
    }
}