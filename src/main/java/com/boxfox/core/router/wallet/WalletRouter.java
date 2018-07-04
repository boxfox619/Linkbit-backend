package com.boxfox.core.router.wallet;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.common.vertx.service.AsyncService;
import com.boxfox.cross.common.vertx.service.Service;
import com.boxfox.cross.service.AddressService;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.one.sys.db.tables.daos.CoinDao;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import sun.plugin2.server.main.ResultHandler;


public class WalletRouter {

    @Service
    protected AddressService addressService;
    protected Gson gson;

    public WalletRouter() {
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
        this.addressService = new AddressService();
    }

    @RouteRegistration(uri = "/support/wallet", method = HttpMethod.GET, auth = true)
    public void getSupportWalletList(RoutingContext ctx) {
        CoinDao dao = new CoinDao(PostgresConfig.create());
        JsonArray coins = new JsonArray();
        dao.findAll().forEach(c -> {
            coins.add(new JsonObject().put("symbol", c.getSymbol()).put("name", c.getName()));
        });
        ctx.response().end(coins.encode());
    }

    protected <T> void doAsync(Handler<Future<T>> handler){
        AsyncService.getInstance().doAsync("wallet-service-executor", handler);
    }

    protected <T> void doAsync(Handler<Future<T>> handler, Handler<AsyncResult<T>> resultHandler){
        AsyncService.getInstance().doAsync("wallet-service-executor", handler, resultHandler);
    }
}
