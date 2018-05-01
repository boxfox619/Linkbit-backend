package com.boxfox.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.HandlebarsTemplateEngine;
import io.vertx.ext.web.templ.TemplateEngine;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Date;

public class MainVerticle extends AbstractVerticle {

    private HttpServer server;
    private ScriptEngine scriptEngine;

    @Override
    public void start(Future<Void> future) {
        Router router = Router.router(vertx);



        server = vertx.createHttpServer().requestHandler(router::accept).listen(8999);
        future.complete();
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        server.close();
        stopFuture.complete();
    }
}
