package com.boxfox.cross.common.vertx.middleware;

import io.vertx.ext.web.RoutingContext;

import java.util.Locale;

public class LocaleHandlerImpl implements LocaleHandler {
    @Override
    public void handle(RoutingContext ctx) {
        ctx.data().put("locale", Locale.KOREA.getCountry());
        ctx.next();
    }
}