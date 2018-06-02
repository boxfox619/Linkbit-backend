package com.boxfox.core.router.wallet;

import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.service.wallet.WalletService;
import com.boxfox.cross.service.wallet.WalletServiceManager;
import com.boxfox.cross.service.wallet.model.TransactionStatus;
import com.google.gson.Gson;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import rx.Observer;
import rx.Subscriber;

import java.util.List;

public class WalletTransactionRouter extends WalletRouter{

    @RouteRegistration(uri = "/wallet/:symbol/transaction", method = HttpMethod.GET, auth = true)
    public void transaction(RoutingContext ctx, @Param String symbol, @Param String hash) {
        WalletService service = WalletServiceManager.getService(symbol);
        TransactionStatus transactionStatus = service.getTransaction(hash);
        ctx.response().setChunked(true).write(new Gson().toJson(transactionStatus)).end();
    }

    @RouteRegistration(uri = "/wallet/:symbol/transaction/count", method = HttpMethod.GET, auth = true)
    public void transactionCount(RoutingContext ctx, @Param String symbol, @Param String address) {
        WalletService service = WalletServiceManager.getService(symbol);
        int count = service.getTransactionCount(address);
        ctx.response().setChunked(true).write(count+"").end();
    }

    @RouteRegistration(uri = "/wallet/:symbol/transaction/list", method = HttpMethod.GET, auth = true)
    public void transactionList(RoutingContext ctx, @Param String symbol, @Param String address) {
        WalletService service = WalletServiceManager.getService(symbol);
        service.getTransactionList(address).setHandler(transactionStatusListResult -> {
            List<TransactionStatus> transactionStatusList = transactionStatusListResult.result();
            if(transactionStatusList.size()==0){
                service.indexingTransactions(address);
            }
            ctx.response().setChunked(true).write(new Gson().toJson(transactionStatusList)).end();
        });
    }

}
