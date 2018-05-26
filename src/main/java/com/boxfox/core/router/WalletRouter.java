package com.boxfox.core.router;

import com.boxfox.service.wallet.WalletServiceManager;
import com.boxfox.service.wallet.WalletService;
import com.boxfox.support.vertx.router.RouteRegistration;
import com.boxfox.support.vertx.router.Param;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class WalletRouter {

    @RouteRegistration(uri = "/wallet:name/balance", method = HttpMethod.GET, auth = true)
    public void getBalance(RoutingContext ctx, @Param String address) {
        String name = ctx.pathParam("name");
        WalletService service = WalletServiceManager.getService(name);
        String balance = service.getBalance(address);
        if(balance==null){
            ctx.response().write(balance);
            ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
        }else{
            ctx.response().write(balance);
            ctx.response().setStatusCode(200);
        }
        ctx.response().end();
    }

    @RouteRegistration(uri = "/wallet/:name/create", method = HttpMethod.POST, auth = true)
    public void create(RoutingContext ctx, @Param String password) {
        String name = ctx.pathParam("name");
        WalletService service = WalletServiceManager.getService(name);
        JsonObject result = service.createWallet(password);
        ctx.response().putHeader("content-type", "application/json");
        ctx.response().write(result.encodePrettily());
        ctx.response().end();
    }

    @RouteRegistration(uri = "/wallet:name/send", method = HttpMethod.POST, auth = true)
    public void send(RoutingContext ctx,
                     @Param String walletFileName,
                     @Param String walletJsonFile,
                     @Param String password,
                     @Param String targetAddress,
                     @Param String amount) {
        String name = ctx.pathParam("name");
        WalletService service = WalletServiceManager.getService(name);
        JsonObject result = service.send(walletFileName, walletJsonFile, password, targetAddress, amount);
        ctx.response().putHeader("content-type", "application/json");
        ctx.response().write(result.encodePrettily());
        ctx.response().end();
    }
}
