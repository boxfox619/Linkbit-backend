package com.boxfox.cross.common.vertx.router;

import com.boxfox.cross.common.vertx.service.AsyncService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class AbstractRouter {
    protected Gson gson;

    public AbstractRouter(){
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
    }

    protected <T> void doAsync(Handler<Future<T>> handler){
        AsyncService.getInstance().doAsync("router-service-executor", handler);
    }

    protected <T> void doAsync(Handler<Future<T>> handler, Handler<AsyncResult<T>> resultHandler){
        AsyncService.getInstance().doAsync("router-service-executor", handler, resultHandler);
    }
}