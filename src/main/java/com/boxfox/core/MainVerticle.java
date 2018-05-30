package com.boxfox.core;

import com.boxfox.cross.service.wallet.WalletServiceManager;
import com.boxfox.cross.service.wallet.indexing.TransactionIndexingVerticle;
import com.boxfox.cross.wallet.eth.EthereumService;
import com.boxfox.cross.common.vertx.middleware.CORSHandler;
import com.boxfox.cross.common.vertx.router.RouteRegister;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class MainVerticle extends AbstractVerticle {

    private HttpServer server;

    @Override
    public void start(Future<Void> future) {
        RouteRegister routeRegister = RouteRegister.routing(vertx);
        Router router = routeRegister.getRouter();
        router.route().handler(CookieHandler.create());
        router.route("/*").handler(CORSHandler.create());
        router.route("/assets/*").handler(StaticHandler.create("assets"));
        routeRegister.route(this.getClass().getPackage().getName());
        server = vertx.createHttpServer().requestHandler(router::accept).listen(8999, rs -> {
            System.out.println("asdasd");
            vertx.deployVerticle(TransactionIndexingVerticle.class.getName(), new DeploymentOptions().setWorker(true));
            WalletServiceManager.register("eth", new EthereumService());
        });
        future.complete();
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        server.close();
        stopFuture.complete();
    }
}
