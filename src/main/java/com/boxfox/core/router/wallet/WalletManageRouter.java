package com.boxfox.core.router.wallet;

import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.service.wallet.WalletService;
import com.boxfox.cross.service.wallet.WalletServiceManager;
import com.boxfox.cross.service.wallet.model.WalletCreateResult;
import com.google.gson.Gson;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.one.sys.db.tables.records.WalletRecord;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import static io.one.sys.db.tables.Wallet.WALLET;

public class WalletManageRouter extends WalletRouter {

    @RouteRegistration(uri = "/wallet", method = HttpMethod.POST, auth = true)
    public void create(RoutingContext ctx, @Param String symbol, @Param String name, @Param String password, @Param String description) {
        boolean major = false;
        String uid = (String) ctx.data().get("uid");
        if (password != null) {
            WalletService service = WalletServiceManager.getService(symbol);
            service.createWallet(uid, name, password, description, res->{
                ctx.response().putHeader("content-type", "application/json");
                ctx.response().setChunked(true).write(new Gson().toJson(res.result()));
                if (res.succeeded() && major) {
                    addressService.setMajorWallet(uid, symbol, res.result().getAddress(), res2 -> {
                        ctx.response().end();
                    });
                }else{
                    ctx.response().end();
                }
            });
        } else {
            ctx.response().setStatusMessage("Illegal Argument").setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
            ctx.response().end();
        }
    }

    @RouteRegistration(uri = "/wallet", method = HttpMethod.PUT, auth = true)
    public void walletOptionsSetting(RoutingContext ctx, @Param String symbol, @Param String address, @Param boolean major){
        String uid = ctx.user().principal().getString("su");
        addressService.setMajorWallet(uid, symbol, address, res->{

        });
    }

    @RouteRegistration(uri = "/wallet", method = HttpMethod.DELETE)
    public void delete(RoutingContext ctx, @Param String address){

    }

}
