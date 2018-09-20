package com.boxfox.cross.common.vertx.middleware;

import com.boxfox.vertx.data.Config;
import com.boxfox.vertx.util.LogUtil;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import io.vertx.ext.web.RoutingContext;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutionException;

public class LoggerHandlerImpl implements LoggerHandler {
  private final Logger logger;

  public LoggerHandlerImpl(){
    logger = LogManager.getRootLogger();
  }

  @Override
  public void handle(RoutingContext ctx) {
    String uid = null;
    String url = ctx.request().uri();
    String token = ctx.request().getHeader(HttpHeaders.AUTHORIZATION);
    if (token != null) {
      FirebaseToken decodedToken = null;
      try {
        decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(token).get();
        if (decodedToken != null) {
          uid = decodedToken.getUid();
        }
      } catch (InterruptedException | ExecutionException e) {
        LogUtil.getLogger().debug(e.getMessage());
      }
    }
    String log = String.format("URL : %s / UID : %s", url, uid);
    this.logger.info(log);
    if(Config.getDefaultInstance().getBoolean("debug")){
      System.out.println(log);
    }
    ctx.next();
  }

}
