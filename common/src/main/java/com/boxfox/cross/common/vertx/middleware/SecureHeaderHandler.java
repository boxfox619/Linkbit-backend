package com.boxfox.cross.common.vertx.middleware;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface SecureHeaderHandler extends Handler<RoutingContext> {

  static SecureHeaderHandler create(){
    return new SecureHeaderHandlerImpl();
  }

}
