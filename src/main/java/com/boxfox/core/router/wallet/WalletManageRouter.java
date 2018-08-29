package com.boxfox.core.router.wallet;

import com.boxfox.cross.common.vertx.router.AbstractRouter;
import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.common.vertx.service.Service;
import com.boxfox.cross.service.WalletDatabaseService;
import com.boxfox.cross.wallet.WalletService;
import com.boxfox.cross.wallet.WalletServiceManager;
import com.boxfox.cross.wallet.model.WalletCreateResult;
import com.google.api.client.http.HttpStatusCodes;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public class WalletManageRouter extends AbstractRouter {

    @Service
    protected WalletDatabaseService walletDatabaseService;

    @RouteRegistration(uri = "/wallet", method = HttpMethod.POST, auth = true)
    public void create(RoutingContext ctx, @Param String symbol, @Param String name, @Param String password, @Param String description, @Param boolean major, @Param boolean open) {
        String uid = (String) ctx.data().get("uid");
        doAsync(future -> {
            if (password != null) {
                WalletService service = WalletServiceManager.getService(symbol);
                WalletCreateResult result = service.createWallet(password);
                if(result.isSuccess()){
                    walletDatabaseService.createWallet(uid, symbol, name, result.getAddress(), description, open, major, dbRes -> {
                        if(dbRes.succeeded()){
                            future.complete(result);
                        }else{
                            future.fail("Failure create wallet");
                        }
                    });
                }else{
                    future.fail("Failure generate wallet data");
                }
            }
            future.fail("Illegal Argument");
        }, res -> {
            if(res.succeeded()){
                ctx.response().end(gson.toJson(res.result()));
            } else {
                ctx.response()
                    .setStatusMessage(res.cause().getMessage())
                    .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                    .end();
            }
        });
    }

    @RouteRegistration(uri = "/wallet", method = HttpMethod.PUT, auth = true)
    public void updateWallet(RoutingContext ctx, @Param String address, @Param String name, @Param String description, @Param boolean major, @Param boolean open) {
        String uid = (String) ctx.data().get("uid");
        walletDatabaseService.checkOwner(uid, address, res -> {
            if (res.succeeded()) {
                walletDatabaseService.updateWallet(uid, address, name, description, major, open, res2 -> {
                    if (res2.succeeded()) {
                        ctx.response()
                                .setStatusCode(HttpStatusCodes.STATUS_CODE_OK)
                                .end();
                    } else {
                        ctx.response()
                                .setStatusMessage(res2.cause().getMessage())
                                .setStatusCode(HttpStatusCodes.STATUS_CODE_NOT_FOUND)
                                .end();
                    }
                });
            } else {
                ctx.response()
                        .setStatusMessage(res.cause().getMessage())
                        .setStatusCode(HttpStatusCodes.STATUS_CODE_METHOD_NOT_ALLOWED)
                        .end();
            }
        });
    }

    @RouteRegistration(uri = "/wallet", method = HttpMethod.DELETE)
    public void deleteWallet(RoutingContext ctx, @Param String address) {
        String uid = (String) ctx.data().get("uid");
        walletDatabaseService.checkOwner(uid, address, res -> {
            if (res.succeeded()) {
                walletDatabaseService.deleteWallet(uid, address, res2 -> {
                    if (res2.succeeded()) {
                        ctx.response()
                                .setStatusCode(HttpStatusCodes.STATUS_CODE_OK)
                                .end();
                    } else {
                        ctx.response()
                                .setStatusMessage(res2.cause().getMessage())
                                .setStatusCode(HttpStatusCodes.STATUS_CODE_PRECONDITION_FAILED)
                                .end();
                    }
                });
            } else {
                ctx.response()
                        .setStatusMessage(res.cause().getMessage())
                        .setStatusCode(HttpStatusCodes.STATUS_CODE_METHOD_NOT_ALLOWED)
                        .end();
            }
        });
    }

}
