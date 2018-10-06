package com.boxfox.core.router.wallet;

import com.boxfox.vertx.router.*;
import com.boxfox.vertx.service.*;
import com.boxfox.cross.service.WalletDatabaseService;
import com.boxfox.cross.wallet.WalletService;
import com.boxfox.cross.wallet.WalletServiceManager;
import com.boxfox.cross.wallet.model.TransactionResult;
import com.linkbit.android.entity.WalletModel;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public class RemittanceRouter extends AbstractRouter {

    @Service
    private WalletDatabaseService walletDatabaseService;

    @RouteRegistration(uri = "/remittance", method = HttpMethod.POST, auth = true)
    public void send(RoutingContext ctx,
                     @Param String symbol,
                     @Param String walletName,
                     @Param String walletData,
                     @Param String password,
                     @Param String targetAddress,
                     @Param String amount) {
        WalletService service = WalletServiceManager.getService(symbol);
        walletDatabaseService.findByAddress(targetAddress, res -> {
            WalletModel wallet = res.result();
            if (wallet != null) {
                String destAddress = targetAddress;
                if (res != null)
                    destAddress = wallet.getAccountAddress();
                TransactionResult result = service.send(walletName, walletData, password, destAddress, amount);
                ctx.response().putHeader("content-type", "application/json");
                ctx.response().setChunked(true).write(gson.toJson(result));
            } else {
                ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
            }
            ctx.response().end();
        });
    }
}
