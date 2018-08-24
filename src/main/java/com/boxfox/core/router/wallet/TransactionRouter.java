package com.boxfox.core.router.wallet;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.common.vertx.router.AbstractRouter;
import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.common.vertx.service.Service;
import com.boxfox.cross.service.AddressService;
import com.boxfox.cross.wallet.WalletService;
import com.boxfox.cross.wallet.WalletServiceManager;
import com.google.gson.Gson;
import com.linkbit.android.entity.TransactionModel;
import io.one.sys.db.tables.daos.WalletDao;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;

public class TransactionRouter extends AbstractRouter {

    @Service
    private AddressService addressService;

    @RouteRegistration(uri = "/transaction", method = HttpMethod.GET, auth = true)
    public void transaction(RoutingContext ctx, @Param String symbol, @Param String txHash) {
        WalletService service = WalletServiceManager.getService(symbol);
        TransactionModel transaction = service.getTransaction(txHash);
        ctx.response().setChunked(true).write(gson.toJson(transaction)).end();
    }

    @RouteRegistration(uri = "/transaction/count", method = HttpMethod.GET, auth = true)
    public void transactionCount(RoutingContext ctx, @Param String address) {
        addressService.findByAddress(address, res->{
            if(res.result() != null){
                WalletService service = WalletServiceManager.getService(res.result().getCoinSymbol());
                int count = service.getTransactionCount(address);
                ctx.response().setChunked(true).write(count+"").end();
            }else {
                ctx.response().setStatusCode(404).end();
            }
        });
    }

    @RouteRegistration(uri = "/transaction/:address/list", method = HttpMethod.GET, auth = true)
    public void transactionList(RoutingContext ctx, @Param String address) {
        addressService.findByAddress(address, res -> {
            if (res.result() != null) {
                WalletService service = WalletServiceManager.getService(res.result().getCoinSymbol());
                service.getTransactionList(address).setHandler(transactionStatusListResult -> {
                    List<TransactionModel> transactionStatusList = transactionStatusListResult.result();
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

    @RouteRegistration(uri = "/transaction/list", method = HttpMethod.GET, auth = true)
    public void transactionList(RoutingContext ctx, @Param int page, @Param int count) {
        String uid = (String) ctx.data().get("uid");
        doAsync(future -> {
            WalletDao dao = new WalletDao(PostgresConfig.create());
            List<TransactionModel> totalTxStatusList = new ArrayList<>();
            dao.fetchByUid(uid).forEach(w -> {
                WalletService service = WalletServiceManager.getService(w.getSymbol());
                try {
                    String accountAddress = w.getAddress();
                    service.getTransactionList(accountAddress).setHandler(txStatusListResult -> {
                        List<TransactionModel> txStatusList = txStatusListResult.result();
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
                ctx.response().setChunked(true).write(gson.toJson(e.result())).end();
            }else{
                ctx.response().setStatusCode(400).end();
            }
        });
    }

}
