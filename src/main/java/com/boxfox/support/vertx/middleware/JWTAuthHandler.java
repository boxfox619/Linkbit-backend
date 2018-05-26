package com.boxfox.support.vertx.middleware;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface JWTAuthHandler extends Handler<RoutingContext> {

    static JWTAuthHandler create() {
        return new JWTAuthHandlerImpl();
    }
}