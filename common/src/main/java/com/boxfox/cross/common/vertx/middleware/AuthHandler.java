package com.boxfox.cross.common.vertx.middleware;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface AuthHandler extends Handler<RoutingContext> {

    static AuthHandler create() {
        return new AuthHandlerImpl();
    }
}
