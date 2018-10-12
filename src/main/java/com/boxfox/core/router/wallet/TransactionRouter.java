package com.boxfox.core.router.wallet;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.service.WalletDatabaseService;
import com.boxfox.cross.wallet.WalletService;
import com.boxfox.cross.wallet.WalletServiceManager;
import com.boxfox.vertx.router.*;
import com.boxfox.vertx.service.*;
import com.linkbit.android.entity.TransactionModel;
import io.one.sys.db.tables.daos.WalletDao;
import io.one.sys.db.tables.pojos.Wallet;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransactionRouter extends AbstractRouter {

    @Service
    private WalletDatabaseService walletDatabaseService;

    @RouteRegistration(uri = "/transaction", method = HttpMethod.GET)
    public void lookupTransaction(RoutingContext ctx,
                                  @Param(name="symbol") String symbol,
                                  @Param(name="txHash") String txHash) {
        WalletService service = WalletServiceManager.getService(symbol);
        TransactionModel transaction = service.getTransaction(txHash);
        ctx.response().setChunked(true).write(gson.toJson(transaction)).end();
    }

    @RouteRegistration(uri = "/transaction/count", method = HttpMethod.GET)
    public void wallTransactionCount(RoutingContext ctx, @Param(name="address") String address) {
        walletDatabaseService.findByAddress(address, res -> {
            if (res.result() != null) {
                WalletService service = WalletServiceManager.getService(res.result().getCoinSymbol());
                int count = service.getTransactionCount(address);
                ctx.response().setChunked(true).write(count + "").end();
            } else {
                ctx.response().setStatusCode(404).end();
            }
        });
    }

    @RouteRegistration(uri = "/transaction/list", method = HttpMethod.GET)
    public void walletTransactionList(RoutingContext ctx,
                                      @Param(name="address") String address,
                                      @Param(name="page") int page,
                                      @Param(name="count") int count) {
        //@TODO transaction list pagenation
        walletDatabaseService.findByAddress(address, res -> {
            if (res.result() != null) {
                WalletService service = WalletServiceManager.getService(res.result().getCoinSymbol());
                service.getTransactionList(address).setHandler(transactionStatusListResult -> {
                    List<TransactionModel> transactionStatusList = transactionStatusListResult.result();
                    if (transactionStatusList.size() == 0) {
                        service.indexingTransactions(address);
                    }
                    ctx.response().setChunked(true).write(gson.toJson(transactionStatusList)).end();
                });
            } else {
                ctx.response().setStatusCode(404).end();
            }
        });
    }

    @RouteRegistration(uri = "/transaction/all/count", method = HttpMethod.GET, auth = true)
    public void allTransactionCount(RoutingContext ctx) {
        String uid = (String) ctx.data().get("uid");
        doAsync(future -> {
            WalletDao dao = new WalletDao(PostgresConfig.create(), getVertx());
            dao.findManyByUid(Arrays.asList(uid)).result().forEach(w -> {
                int count = 0;
                WalletService service = WalletServiceManager.getService(w.getSymbol());
                String accountAddress = w.getAddress();
                count += service.getTransactionCount(accountAddress);
                future.complete(count);
            });
            if (!future.isComplete())
                future.fail("Transaction Not Found");
        }, e -> {
            if (e.succeeded()) {
                ctx.response().setChunked(true).write(gson.toJson(e.result())).end();
            } else {
                ctx.response().setStatusCode(400).end();
            }
        });
    }

    @RouteRegistration(uri = "/transaction/all/list", method = HttpMethod.GET, auth = true)
    public void allTransactionList(RoutingContext ctx, @Param(name = "page") int page, @Param(name = "count") int count) {
        String uid = (String) ctx.data().get("uid");
        doAsync(future -> {
            WalletDao dao = new WalletDao(PostgresConfig.create(), getVertx());
            List<TransactionModel> totalTxStatusList = new ArrayList<>();
            List<Future> tasks = new ArrayList();
            for(Wallet wallet : dao.findManyByUid(Arrays.asList(uid)).result()){
                WalletService service = WalletServiceManager.getService(wallet.getSymbol());
                String accountAddress = wallet.getAddress();
                tasks.add(service.getTransactionList(accountAddress).setHandler(txStatusListResult -> {
                    List<TransactionModel> txStatusList = txStatusListResult.result();
                    if (txStatusList.size() == 0) {
                        service.indexingTransactions(accountAddress);
                    }
                    totalTxStatusList.addAll(txStatusList);
                }));
                if(totalTxStatusList.size()>= page * count){
                    break;
                }
            }
            boolean check;
            do{
                check = true;
                for(Future task : tasks){
                    if(!task.isComplete()){
                        check = false;
                        break;
                    }
                }
            }while(!check);
            future.complete(totalTxStatusList);
        }, e -> {
            if (e.succeeded()) {
                ctx.response().setChunked(true).write(gson.toJson(e.result())).end();
            } else {
                ctx.response().setStatusCode(400).end();
            }
        });
    }

}
