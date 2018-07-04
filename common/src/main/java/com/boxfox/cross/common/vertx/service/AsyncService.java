package com.boxfox.cross.common.vertx.service;

import io.vertx.core.*;

public class AsyncService {
    private static AsyncService instance;

    private Vertx vertx;

    public static AsyncService create(Vertx vertx){
        AsyncService service = new AsyncService();
        service.vertx = vertx;
        return service;
    }

    public static AsyncService getInstance(){
        return instance;
    }

    protected WorkerExecutor getExecutor(String executorName){
        return vertx.createSharedWorkerExecutor(executorName);
    }

    public <T> void doAsync(String executorName, Handler<Future<T>> handler, Handler<AsyncResult<T>> resultHandler) {
        getExecutor(executorName).executeBlocking(event -> {
            try {
                handler.handle(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, resultHandler);
    }

    public <T> void doAsync(String executorName, Handler<Future<T>> handler) {
        doAsync(executorName, handler, e->{});
    }
}
