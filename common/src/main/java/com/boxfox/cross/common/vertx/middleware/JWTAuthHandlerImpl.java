package com.boxfox.cross.common.vertx.middleware;

import io.vertx.ext.web.RoutingContext;

public class JWTAuthHandlerImpl implements JWTAuthHandler {

    @Override
    public void handle(RoutingContext ctx) {
       /* String token = ctx.getCookie("token").getValue();
        AuthService.createJWTAuth(ctx.vertx()).authenticate(new JsonObject().put("jwt", token), res->{
            if(res.succeeded()){
                ctx.next();
            }else{
                ctx.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
            }
        });*/
        ctx.next();
    }
}