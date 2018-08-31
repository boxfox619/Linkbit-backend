package com.boxfox.cross.common.vertx.middleware;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface LoggerHandler  extends Handler<RoutingContext> {

  static LoggerHandler create() {
    return new LoggerHandlerImpl();
  }

}
