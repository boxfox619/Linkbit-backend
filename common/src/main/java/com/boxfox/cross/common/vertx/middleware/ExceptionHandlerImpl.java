package com.boxfox.cross.common.vertx.middleware;

import com.boxfox.cross.common.RoutingException;
import io.vertx.ext.web.RoutingContext;

public class ExceptionHandlerImpl implements ExceptionHandler {
    @Override
    public void handle(RoutingContext ctx) {
        Throwable t = ctx.failure();
        if (t != null && t instanceof RoutingException) {
            RoutingException e = (RoutingException) t;
            ctx.response().setStatusCode(e.getCode());
            ctx.response().end(e.getMessage());
        } else {
            ctx.response().setStatusCode(ctx.statusCode()).end("fail code : " + ctx.statusCode());
        }
    }
}
