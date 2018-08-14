package com.boxfox.core.router.wallet;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.common.vertx.service.AsyncService;
import com.boxfox.cross.common.vertx.service.Service;
import com.boxfox.cross.service.AddressService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;


public class WalletRouter {

    @Service
    protected AddressService addressService;
    protected Gson gson;

    public WalletRouter() {
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
        this.addressService = new AddressService();
    }

    protected <T> void doAsync(Handler<Future<T>> handler){
        AsyncService.getInstance().doAsync("wallet-service-executor", handler);
    }

    protected <T> void doAsync(Handler<Future<T>> handler, Handler<AsyncResult<T>> resultHandler){
        AsyncService.getInstance().doAsync("wallet-service-executor", handler, resultHandler);
    }
}
