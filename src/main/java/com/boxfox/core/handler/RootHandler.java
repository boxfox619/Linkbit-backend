package com.boxfox.core.handler;

import com.boxfox.support.vertx.router.RouteRegistration;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

@RouteRegistration(uri="/", method = HttpMethod.GET)
public class RootHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        ctx.response().end("Service is running!");
    }
}
