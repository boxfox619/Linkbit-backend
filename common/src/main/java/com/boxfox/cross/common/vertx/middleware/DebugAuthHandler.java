package com.boxfox.cross.common.vertx.middleware;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface DebugAuthHandler extends Handler<RoutingContext> {

    static DebugAuthHandler create(){
        return new DebugAuthHandlerImpl();
    }
}
