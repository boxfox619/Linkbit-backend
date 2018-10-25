package com.boxfox.core;

import com.boxfox.cross.common.vertx.middleware.*;

import com.boxfox.cross.common.vertx.middleware.CORSHandler;
import com.boxfox.cross.common.vertx.middleware.ExceptionHandler;
import com.boxfox.cross.common.vertx.middleware.LocaleHandler;
import com.boxfox.cross.common.vertx.middleware.LoggerHandler;
import com.boxfox.cross.common.vertx.middleware.SecureHeaderHandler;
import com.boxfox.cross.service.price.PriceIndexingVerticle;
import com.boxfox.linkbit.wallet.WalletServiceRegistry;
import com.boxfox.linkbit.wallet.indexing.TransactionIndexingVerticle;
import com.boxfox.vertx.data.Config;
import com.boxfox.vertx.middleware.FirebaseAuthHandler;
import com.boxfox.vertx.router.RouteRegister;
import com.boxfox.vertx.service.AsyncService;
import com.boxfox.vertx.util.FirebaseUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import java.io.IOException;
import org.apache.log4j.Logger;

public class MainVerticle extends AbstractVerticle {

    private HttpServer server;

    @Override
    public void start(Future<Void> future) {
        AsyncService.create(vertx);
        try {
            FirebaseUtil.init("keystore/cross-c863f-3861d7d0cc90.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean isDebugMode = Config.getDefaultInstance().getBoolean("debug");
        Handler authHandler = (isDebugMode) ? DebugAuthHandler.create() : FirebaseAuthHandler.create();
        RouteRegister routeRegister = RouteRegister.routing(vertx, authHandler);
        Router router = routeRegister.getRouter();
        router.route().handler(SessionHandler
            .create(LocalSessionStore.create(vertx))
            .setCookieHttpOnlyFlag(true)
            .setCookieSecureFlag(true)
        );
        router.route().handler(SecureHeaderHandler.create());
        router.route().handler(CookieHandler.create());
        router.route().handler(BodyHandler.create().setBodyLimit(50 * 1048576L));
        router.route("/*").handler(CORSHandler.create());
        router.route("/*").handler(LocaleHandler.create());
        router.route("/*").handler(LoggerHandler.create());
        router.route("/assets/*").handler(StaticHandler.create("assets"));
        routeRegister.route(this.getClass().getPackage().getName());
        router.route().failureHandler(ExceptionHandler.create());
        int port = Config.getDefaultInstance().getInt("port", 8999);
        server = vertx.createHttpServer().requestHandler(router::accept).listen(port, rs -> {
            Logger.getRootLogger().info("Server started : " + server.actualPort());
            vertx.deployVerticle(TransactionIndexingVerticle.class.getName(), new DeploymentOptions().setWorker(true));
            vertx.deployVerticle(PriceIndexingVerticle.class.getName(), new DeploymentOptions().setWorker(true));
            WalletServiceRegistry.init(vertx);
        });
        future.complete();
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        server.close();
        stopFuture.complete();
    }
}
