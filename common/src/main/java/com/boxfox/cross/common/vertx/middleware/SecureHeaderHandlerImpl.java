package com.boxfox.cross.common.vertx.middleware;

import io.vertx.ext.web.RoutingContext;

public class SecureHeaderHandlerImpl implements SecureHeaderHandler {

  @Override
  public void handle(RoutingContext ctx) {
    ctx.response()
        // do not allow proxies to cache the data
        .putHeader("Cache-Control", "no-store, no-cache")
        // prevents Internet Explorer from MIME - sniffing a
        // response away from the declared content-type
        .putHeader("X-Content-Type-Options", "nosniff")
        // Strict HTTPS (for about ~6Months)
        .putHeader("Strict-Transport-Security", "max-age=" + 15768000)
        // IE8+ do not allow opening of attachments in the context of this resource
        .putHeader("X-Download-Options", "noopen")
        // enable XSS for IE
        .putHeader("X-XSS-Protection", "1; mode=block")
        // deny frames
        .putHeader("X-FRAME-OPTIONS", "DENY");
    ctx.next();
  }
}
