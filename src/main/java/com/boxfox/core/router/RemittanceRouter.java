package com.boxfox.core.router;

import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.service.wallet.WalletService;
import com.boxfox.cross.service.wallet.WalletServiceManager;
import com.boxfox.cross.service.wallet.model.TransactionResult;
import com.google.gson.Gson;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public class RemittanceRouter {
    private Gson gson;

    public RemittanceRouter(){
        this.gson = new Gson();
    }

    @RouteRegistration(uri = "/wallet/:symbol/send", method = HttpMethod.POST, auth = true)
    public void send(RoutingContext ctx,
                     @Param String symbol,
                     @Param String walletFileName,
                     @Param String walletJsonFile,
                     @Param String password,
                     @Param String targetAddress,
                     @Param String amount) {
        WalletService service = WalletServiceManager.getService(symbol);
        TransactionResult result = service.send(walletFileName, walletJsonFile, password, targetAddress, amount);
        ctx.response().putHeader("content-type", "application/json");
        ctx.response().setChunked(true).write(gson.toJson(result));
        ctx.response().end();
    }
}
