package com.boxfox.cross.common.vertx.middleware;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.Method;

public interface BaseHandler extends Handler<RoutingContext> {

    static BaseHandler create(Object instance, Method m) {
        return new BaseHandlerImpl(instance, m);
    }
}
