package com.boxfox.core.router.wallet;

import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.service.model.Wallet;
import com.boxfox.cross.service.wallet.WalletService;
import com.boxfox.cross.service.wallet.WalletServiceManager;
import com.boxfox.cross.service.wallet.model.TransactionResult;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public class RemittanceRouter extends WalletRouter{

    @RouteRegistration(uri = "/wallet/:symbol/send", method = HttpMethod.POST, auth = true)
    public void send(RoutingContext ctx,
                     @Param String symbol,
                     @Param String walletFileName,
                     @Param String walletJsonFile,
                     @Param String password,
                     @Param String targetAddress,
                     @Param String amount) {
        WalletService service = WalletServiceManager.getService(symbol);
        addressService.findByAddress(symbol, targetAddress, res -> {
            Wallet wallet = res.result();
            String destAddress = targetAddress;
            if (res != null)
                destAddress = wallet.getOriginalAddress();
            TransactionResult result = service.send(walletFileName, walletJsonFile, password, destAddress, amount);
            ctx.response().putHeader("content-type", "application/json");
            ctx.response().setChunked(true).write(gson.toJson(result));
            ctx.response().end();
        });
    }
}
