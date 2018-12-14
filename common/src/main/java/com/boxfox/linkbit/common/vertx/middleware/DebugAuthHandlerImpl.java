package com.boxfox.linkbit.common.vertx.middleware;

import io.vertx.ext.web.RoutingContext;

public class DebugAuthHandlerImpl implements DebugAuthHandler {
    @Override
    public void handle(RoutingContext ctx) {
        ctx.data().put("uid", "uaAefWqVJtUTALedy6GIPFv4fvm2");
        ctx.next();
    }
}
