package com.boxfox.core;

import com.boxfox.cross.common.vertx.middleware.LocaleHandler;
import com.boxfox.cross.common.vertx.middleware.LoggerHandler;
import com.boxfox.cross.wallet.WalletServiceManager;
import com.boxfox.cross.wallet.eth.EthereumService;
import com.boxfox.cross.common.vertx.middleware.CORSHandler;
import com.boxfox.vertx.util.FirebaseUtil;
import com.boxfox.vertx.vertx.router.*;
import com.boxfox.vertx.vertx.service.*;
import com.boxfox.cross.wallet.indexing.TransactionIndexingVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.IOException;

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
        RouteRegister routeRegister = RouteRegister.routing(vertx);
        Router router = routeRegister.getRouter();
        router.route().handler(CookieHandler.create());
        router.route().handler(BodyHandler.create());
        router.route("/*").handler(CORSHandler.create());
        router.route("/*").handler(LocaleHandler.create());
        router.route("/*").handler(LoggerHandler.create());
        router.route("/assets/*").handler(StaticHandler.create("assets"));
        routeRegister.route(this.getClass().getPackage().getName());
        server = vertx.createHttpServer().requestHandler(router::accept).listen(getPort(), rs -> {
            System.out.println("Server started : "+ server.actualPort());
            vertx.deployVerticle(TransactionIndexingVerticle.class.getName(), new DeploymentOptions().setWorker(true));
            EthereumService ethereumService = new EthereumService(vertx);
            ethereumService.init();
            WalletServiceManager.register("ETH", ethereumService);
        });
        future.complete();
    }

    private int getPort(){
        int port = 8999;
        if(System.getenv("PORT")!=null)
            port = Integer.valueOf(System.getenv("PORT"));
        return port;
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        server.close();
        stopFuture.complete();
    }
}
