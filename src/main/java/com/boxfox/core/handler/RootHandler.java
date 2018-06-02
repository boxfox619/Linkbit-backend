package com.boxfox.core.handler;

import com.boxfox.core.MainVerticle;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.common.vertx.router.RouterMapDoc;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;

@RouteRegistration(uri="/", method = HttpMethod.GET)
public class RootHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        JsonArray arr = RouterMapDoc.createAPIDoc(MainVerticle.class.getPackage().getName());
        ctx.response().setChunked(true);
        ctx.response().putHeader("Content-Type", "text/html; charset=utf-8");
        ctx.response().write("Service is running!<br/>");
        ctx.response().write("<pre>"+arr.encodePrettily()+"</pre>");
        ctx.response().end();
    }
}
