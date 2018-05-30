package com.boxfox.cross.common.vertx.middleware;

import com.boxfox.service.AuthService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class JWTAuthHandlerImpl implements JWTAuthHandler {

    @Override
    public void handle(RoutingContext ctx) {
        String token = ctx.getCookie("token").getValue();
        AuthService.createJWTAuth(ctx.vertx()).authenticate(new JsonObject().put("jwt", token), res->{
            if(res.succeeded()){
                ctx.next();
            }else{
                ctx.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
            }
        });
    }
}