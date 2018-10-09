package com.boxfox.core.router.wallet;

import com.boxfox.core.router.model.WalletCreateNetworkObject;
import com.boxfox.cross.service.WalletDatabaseService;
import com.boxfox.cross.wallet.WalletService;
import com.boxfox.cross.wallet.WalletServiceManager;
import com.boxfox.cross.wallet.model.WalletCreateResult;
import com.boxfox.vertx.router.AbstractRouter;
import com.boxfox.vertx.router.Param;
import com.boxfox.vertx.router.RouteRegistration;
import com.boxfox.vertx.service.Service;
import com.google.api.client.http.HttpStatusCodes;
import com.linkbit.android.entity.WalletModel;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import static com.boxfox.cross.util.LogUtil.getLogger;

public class WalletManageRouter extends AbstractRouter {

    @Service
    protected WalletDatabaseService walletDatabaseService;

    @RouteRegistration(uri = "/wallet/new", method = HttpMethod.POST, auth = true)
    public void create(RoutingContext ctx,
                       @Param(name = "address") String symbol,
                       @Param(name = "name") String name,
                       @Param(name = "password") String password,
                       @Param(name = "description") String description,
                       @Param(name = "major") boolean major,
                       @Param(name = "open") boolean open) {
        String uid = (String) ctx.data().get("uid");
        getLogger().debug("Wallet create test" + uid);
        doAsync(future -> {
            if (password != null) {
                WalletService service = WalletServiceManager.getService(symbol);
                WalletCreateResult result = service.createWallet(password);
                if(result.isSuccess()){
                    WalletCreateNetworkObject response = new WalletCreateNetworkObject();
                    response.setWalletData(result.getWalletData().toString());
                    response.setWalletFileName(result.getWalletName());
                    response.setAccountAddress(result.getAddress());
                    walletDatabaseService.createWallet(uid, symbol, name, result.getAddress(), description, open, major, dbRes -> {
                        if(dbRes.succeeded()){
                            WalletModel walletModel = dbRes.result();
                            response.setOwnerName(walletModel.getOwnerName());
                            response.setLinkbitAddress(walletModel.getLinkbitAddress());
                            response.setWalletName(walletModel.getWalletName());
                            response.setDescription(walletModel.getDescription());
                            response.setCoinSymbol(walletModel.getCoinSymbol());
                            response.setBalance(walletModel.getBalance());
                            response.setOwnerId(walletModel.getOwnerId());
                            future.complete(response);
                        }else{
                            future.fail("Failure create wallet");
                        }
                    });
                }else{
                    future.fail("Failure generate wallet data");
                }
            }else{
                future.fail("Illegal Argument");
            }
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

    @RouteRegistration(uri = "/wallet/add", method = HttpMethod.POST, auth = true)
    public void add(RoutingContext ctx,
                    @Param(name="address") String address,
                    @Param(name="symbol") String symbol,
                    @Param(name="name") String name,
                    @Param(name="description") String description,
                    @Param(name="major") boolean major,
                    @Param(name="open") boolean open) {
        String uid = (String) ctx.data().get("uid");
        doAsync(future -> {
            walletDatabaseService
                .createWallet(uid, symbol, name, address, description, open, major,
                    dbRes -> {
                        if (dbRes.succeeded()) {
                            future.complete(dbRes.result());
                        } else {
                            future.fail("Failure create wallet");
                        }
                    });
        }, res -> {
            if (res.succeeded()){
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
    public void updateWallet(RoutingContext ctx,
                             @Param(name = "address") String address,
                             @Param(name = "name") String name,
                             @Param(name = "description") String description,
                             @Param(name = "major") boolean major,
                             @Param(name = "open") boolean open) {
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
    public void deleteWallet(RoutingContext ctx, @Param(name = "address") String address) {
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
