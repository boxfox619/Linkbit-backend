package com.boxfox.cross.common.vertx.middleware;

import com.boxfox.cross.common.RoutingException;
import com.google.api.client.http.HttpStatusCodes;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;

public class ExceptionHandlerImpl implements ExceptionHandler {
    @Override
    public void handle(RoutingContext ctx) {
        Throwable t = ctx.failure();
        if (t != null && t instanceof RoutingException) {
            RoutingException e = (RoutingException) t;
            ctx.response().setStatusCode(e.getCode());
            ctx.response().end(e.getMessage());
        } else {
            int code = ctx.statusCode();
            String message = ctx.response().getStatusMessage();
            if(code == -1){
                code = HttpStatusCodes.STATUS_CODE_SERVER_ERROR;
            }
            if(message == null || message.length() == 0){
                message = HttpResponseStatus.valueOf(code).reasonPhrase();
            }
            ctx.response().setStatusCode(code).end(message);
        }

    }
}
