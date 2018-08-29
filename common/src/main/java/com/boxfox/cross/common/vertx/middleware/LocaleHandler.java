package com.boxfox.cross.common.vertx.middleware;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface LocaleHandler extends Handler<RoutingContext> {

    static LocaleHandler create() {
        return new LocaleHandlerImpl();
    }
}
