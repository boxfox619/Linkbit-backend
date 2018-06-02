package com.boxfox.cross.common.vertx.middleware;

import com.boxfox.cross.common.vertx.JWTAuthUtil;
import com.google.common.net.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

public class JWTHandlerImpl implements JWTHandler {

    @Override
    public void handle(RoutingContext ctx) {
        String token = ctx.request().getHeader(HttpHeaders.AUTHORIZATION);
        JWTAuthUtil.createAuth(ctx.vertx()).authenticate(new JsonObject().put("jwt", token), res -> {
            if (res.succeeded()) {
                User theUser = res.result();
                ctx.setUser(theUser);
                ctx.data().put("uid", theUser.principal().getString("sub"));
                ctx.next();
            } else {
                ctx.fail(HttpResponseStatus.UNAUTHORIZED.code());
            }
        });
    }

}
