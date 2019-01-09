package com.boxfox.linkbit.common.vertx.middleware;

import io.vertx.ext.web.RoutingContext;

import java.util.Locale;

public class LocaleHandlerImpl implements LocaleHandler {
    @Override
    public void handle(RoutingContext ctx) {
        ctx.data().put("locale", Locale.US.getLanguage());
        ctx.next();
    }
}
