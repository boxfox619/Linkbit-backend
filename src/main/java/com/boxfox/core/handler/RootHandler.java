package com.boxfox.core.handler;

import com.boxfox.core.MainVerticle;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.common.vertx.router.RouterMapDoc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import java.io.IOException;

@RouteRegistration(uri="/", method = HttpMethod.GET)
public class RootHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        String apiDoc = "";
        try {
            JsonArray arr = RouterMapDoc.createAPIDoc(MainVerticle.class.getPackage().getName());
            ObjectMapper mapper = new ObjectMapper();
            Object obj = mapper.readValue(arr.toString(), Object.class);
            apiDoc = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ctx.response().setChunked(true);
        ctx.response().putHeader("Content-Type", "text/html; charset=utf-8");
        ctx.response().write("Service is running!<br/>");
        ctx.response().write("<pre>"+apiDoc.replaceAll("\"", "")+"</pre>");
        ctx.response().end();
    }
}
