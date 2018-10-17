package com.boxfox.cross.common.vertx.middleware;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface ExceptionHandler extends Handler<RoutingContext> {
    static ExceptionHandler create() {
        return new ExceptionHandlerImpl();
    }
}
