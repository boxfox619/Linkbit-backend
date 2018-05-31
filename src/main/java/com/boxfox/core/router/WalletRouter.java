package com.boxfox.core.router;

import com.boxfox.cross.service.wallet.model.TransactionResult;
import com.boxfox.cross.service.wallet.WalletServiceManager;
import com.boxfox.cross.service.wallet.WalletService;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.service.wallet.model.TransactionStatus;
import com.boxfox.cross.service.wallet.model.WalletCreateResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.List;

import static com.boxfox.cross.common.data.PostgresConfig.createContext;
import static io.one.sys.db.Tables.COIN;
import static io.one.sys.db.tables.Wallet.WALLET;

public class WalletRouter {
    private Gson gson;

    public WalletRouter() {
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
    }

    @RouteRegistration(uri = "/support/wallet/list", method = HttpMethod.GET, auth = true)
    public void getSupportWalletList(RoutingContext ctx) {
        JsonArray coins = new JsonArray();
        createContext().selectFrom(COIN).fetch().forEach(r -> coins.add(new JsonObject(r.formatJSON())));
        ctx.response().end(coins.encode());
    }

    @RouteRegistration(uri = "/wallet/list", method = HttpMethod.GET, auth = true)
    public void getWallets(RoutingContext ctx) {
        String ownUid = (String) ctx.data().get("uid");
        JsonArray wallets = new JsonArray();
        createContext()
                .select()
                .from(WALLET.join(COIN).on(WALLET.SYMBOL.eq(COIN.SYMBOL)))
                .where(WALLET.UID.equal(ownUid))
                .fetch()
                .forEach(r -> {
                    String symbol = (String) r.getValue("symbol");
                    String address = (String) r.getValue("address");
                    String balance = WalletServiceManager.getService(symbol).getBalance(address);
                    JsonObject obj = new JsonObject(r.formatJSON());
                    obj.put("balance", balance);
                    wallets.add(obj);
                });
        ctx.response().end(wallets.encode());
    }

    @RouteRegistration(uri = "/wallet/:symbol/balance", method = HttpMethod.GET, auth = true)
    public void getBalance(RoutingContext ctx, @Param String symbol, @Param String address) {
        WalletService service = WalletServiceManager.getService(symbol);
        if (service != null) {
            String balance = service.getBalance(address);
            if (balance == null) {
                ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
            } else {
                ctx.response().setStatusCode(200).setChunked(true).write(balance);
            }
        } else {
            ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
        }
        ctx.response().end();
    }

    @RouteRegistration(uri = "/wallet/:symbol/create", method = HttpMethod.POST, auth = true)
    public void create(RoutingContext ctx, @Param String password, @Param String symbol, @Param String name, @Param String description) {
        String uid = ctx.user().principal().getString("su");
        if (password != null) {
            WalletService service = WalletServiceManager.getService(name);
            WalletCreateResult result = service.createWallet(password, uid, symbol, name, description);
            ctx.response().putHeader("content-type", "application/json");
            ctx.response().setChunked(true).write(new Gson().toJson(result));
        } else {
            ctx.response().setStatusMessage("Illegal Argument").setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
        }
        ctx.response().end();
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
        List<TransactionStatus> transactionStatusList = service.getTransactionList(address);
        ctx.response().setChunked(true).write(new Gson().toJson(transactionStatusList)).end();
    }
}
