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

    @RouteRegistration(uri = "/wallet/:symbol/create", method = HttpMethod.POST, auth = true)
    public void create(RoutingContext ctx, @Param String password, @Param String symbol, @Param String name, @Param String description, @Param boolean major) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String uid = ctx.user().principal().getString("su");
                if (password != null) {
                    WalletService service = WalletServiceManager.getService(symbol);
                    WalletCreateResult result = service.createWallet(password, uid, symbol, name, description);
                    if(result.isSuccess() && major){
                        addressService.setMajorWallet(uid, symbol, result.getAddress());
                    }
                    ctx.response().putHeader("content-type", "application/json");
                    ctx.response().setChunked(true).write(new Gson().toJson(result));
                } else {
                    ctx.response().setStatusMessage("Illegal Argument").setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
                }
                ctx.response().end();
            }
        }).start();
    }

    @RouteRegistration(uri = "/wallet/:symbol/setting", method = HttpMethod.PUT, auth = true)
    public void walletOptionsSetting(RoutingContext ctx, @Param String symbol, @Param String address, @Param boolean major){
        String uid = ctx.user().principal().getString("su");
        addressService.setMajorWallet(uid, symbol, address);
    }

}
