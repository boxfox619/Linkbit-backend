package com.boxfox.core.router.wallet;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.common.vertx.service.Service;
import com.boxfox.cross.service.AddressService;
import com.boxfox.cross.wallet.WalletService;
import com.boxfox.cross.wallet.WalletServiceManager;
import com.boxfox.cross.wallet.model.TransactionStatus;
import com.google.gson.Gson;
import io.one.sys.db.tables.Transaction;
import io.one.sys.db.tables.daos.WalletDao;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;

public class WalletTransactionRouter extends WalletRouter{

    @Service
    private AddressService service;

    @RouteRegistration(uri = "/transaction", method = HttpMethod.GET, auth = true)
    public void transaction(RoutingContext ctx, @Param String symbol, @Param String hash) {
        WalletService service = WalletServiceManager.getService(symbol);
        TransactionStatus transactionStatus = service.getTransaction(hash);
        ctx.response().setChunked(true).write(new Gson().toJson(transactionStatus)).end();
    }

    @RouteRegistration(uri = "/transaction/count", method = HttpMethod.GET, auth = true)
    public void transactionCount(RoutingContext ctx, @Param String address) {
        service.findByAddress(address, res->{
            if(res.result() != null){
                WalletService service = WalletServiceManager.getService(res.result().getSymbol());
                int count = service.getTransactionCount(address);
                ctx.response().setChunked(true).write(count+"").end();
            }else {
                ctx.response().setStatusCode(404).end();
            }
        });
    }

    @RouteRegistration(uri = "/wallet/transaction/list", method = HttpMethod.GET, auth = true)
    public void transactionList(RoutingContext ctx, @Param String address) {
        service.findByAddress(address, res -> {
            if (res.result() != null) {
                WalletService service = WalletServiceManager.getService(res.result().getSymbol());
                service.getTransactionList(address).setHandler(transactionStatusListResult -> {
                    List<TransactionStatus> transactionStatusList = transactionStatusListResult.result();
                    if (transactionStatusList.size() == 0) {
                        service.indexingTransactions(address);
                    }
                    ctx.response().setChunked(true).write(new Gson().toJson(transactionStatusList)).end();
                });
            } else {
                ctx.response().setStatusCode(404).end();
            }
        });
    }

    @RouteRegistration(uri = "/transaction/wallet/list", method = HttpMethod.GET, auth = true)
    public void transactionList(RoutingContext ctx) {
        String uid = (String) ctx.data().get("uid");
        doAsync(future -> {
            WalletDao dao = new WalletDao(PostgresConfig.create());
            List<TransactionStatus> totalTxStatusList = new ArrayList<>();
            dao.fetchByUid(uid).forEach(w -> {
                WalletService service = WalletServiceManager.getService(w.getSymbol());
                try {
                    String accountAddress = w.getAddress();
                    service.getTransactionList(accountAddress).setHandler(txStatusListResult -> {
                        List<TransactionStatus> txStatusList = txStatusListResult.result();
                        if (txStatusList.size() == 0) {
                            service.indexingTransactions(accountAddress);
                        }
                        totalTxStatusList.addAll(txStatusList);
                    }).wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    future.fail(e);
                }
            });
            future.complete(totalTxStatusList);
        }, e -> {
            if(e.succeeded()){
                List<TransactionStatus> txList = (List<TransactionStatus>)e.result();
                ctx.response().setChunked(true).write(new Gson().toJson(txList)).end();
            }else{
                ctx.response().setStatusCode(400).end();
            }
        });
    }

}
