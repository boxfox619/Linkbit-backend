package com.boxfox.cross.common.vertx.middleware;

import io.vertx.ext.web.RoutingContext;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class LoggerHandlerImpl implements LoggerHandler {
  private final Logger logger;

  public LoggerHandlerImpl(){
    logger = LogManager.getRootLogger();
  }

  @Override
  public void handle(RoutingContext ctx) {
    Object uid = ctx.data().get("uid");
    String url = ctx.request().uri();
    this.logger.info(String.format("URL : %s / UID : %s", url, uid));
    ctx.next();
  }

}
