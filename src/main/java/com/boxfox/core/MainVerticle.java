package com.boxfox.core;

import com.boxfox.service.wallet.WalletServiceManager;
import com.boxfox.service.wallet.EthereumService;
import com.boxfox.support.vertx.middleware.CORSHandler;
import com.boxfox.support.vertx.router.RouteRegister;
import io.vertx.core.AbstractVerticle;
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
        server = vertx.createHttpServer().requestHandler(router::accept).listen(8999);
        registerServices();
        future.complete();
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        server.close();
        stopFuture.complete();
    }

    private void registerServices(){
        WalletServiceManager.register("eth", new EthereumService());
    }
}
