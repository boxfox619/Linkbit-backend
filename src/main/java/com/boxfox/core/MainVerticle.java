package com.boxfox.core;

import com.boxfox.service.CryptoCurrencyManager;
import com.boxfox.service.EthereumService;
import com.boxfox.support.vertx.router.RouteRegister;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {

    private HttpServer server;

    @Override
    public void start(Future<Void> future) {
        RouteRegister routeRegister = RouteRegister.routing(vertx);
        routeRegister.route(this.getClass().getPackage().getName());
        Router router = routeRegister.getRouter();
        server = vertx.createHttpServer().requestHandler(router::accept).listen(8999);
        future.complete();
        registerServices();
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        server.close();
        stopFuture.complete();
    }

    private void registerServices(){
        CryptoCurrencyManager.register("eth", new EthereumService());
    }
}
