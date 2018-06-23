package com.boxfox.core.router.wallet;

import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.service.model.Wallet;
import com.boxfox.cross.wallet.WalletService;
import com.boxfox.cross.wallet.WalletServiceManager;
import com.boxfox.cross.wallet.model.TransactionResult;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public class RemittanceRouter extends WalletRouter{

    @RouteRegistration(uri = "/wallet/:symbol/send", method = HttpMethod.POST, auth = true)
    public void send(RoutingContext ctx,
                     @Param String symbol,
                     @Param String walletFileName,
                     @Param String walletFileData,
                     @Param String password,
                     @Param String targetAddress,
                     @Param String amount) {
        WalletService service = WalletServiceManager.getService(symbol);
        addressService.findByAddress(targetAddress, res -> {
            Wallet wallet = res.result();
            if(wallet!=null) {
                String destAddress = targetAddress;
                if (res != null)
                    destAddress = wallet.getOriginalAddress();
                TransactionResult result = service.send(walletFileName, walletFileData, password, destAddress, amount);
                ctx.response().putHeader("content-type", "application/json");
                ctx.response().setChunked(true).write(gson.toJson(result));
            }else{
                ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
            }
            ctx.response().end();
        });
    }
}
