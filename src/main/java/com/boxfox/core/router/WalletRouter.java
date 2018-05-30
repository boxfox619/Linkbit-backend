package com.boxfox.core.router;

import com.boxfox.cross.service.wallet.model.TransactionResult;
import com.boxfox.cross.service.wallet.WalletServiceManager;
import com.boxfox.cross.service.wallet.WalletService;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.common.vertx.router.Param;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static com.boxfox.cross.common.data.PostgresConfig.createContext;
import static io.one.sys.db.Tables.COIN;
import static io.one.sys.db.tables.Wallet.WALLET;

public class WalletRouter {
    private Gson gson;
    private WalletRouter(){
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
    }

    @RouteRegistration(uri = "/support/wallet/", method = HttpMethod.GET, auth = true)
    public void getSupportWalletList(RoutingContext ctx){
        JsonArray coins = new JsonArray();
        createContext().selectFrom(COIN).fetch().forEach(r-> coins.add(new JsonObject(r.formatJSON())));
        ctx.response().end(coins.encode());
    }

    @RouteRegistration(uri = "/wallet/", method = HttpMethod.GET, auth = true)
    public void getWallets(RoutingContext ctx) {
        String ownUid = (String) ctx.data().get("uid");
        JsonArray wallets = new JsonArray();
        createContext()
                .select()
                .from(WALLET.join(COIN).on(WALLET.SYMBOL.eq(COIN.SYMBOL)))
                .where(WALLET.UID.equal(ownUid))
                .fetch()
                .forEach(r->{
                    String symbol = (String) r.getValue("symbol");
                    String address = (String) r.getValue("address");
                    String balance = WalletServiceManager.getService(symbol).getBalance(address);
                    JsonObject obj = new JsonObject(r.formatJSON());
                    obj.put("balance", balance);
                    wallets.add(obj);
                });
        ctx.response().end(wallets.encode());
    }

    @RouteRegistration(uri = "/wallet/:name/balance", method = HttpMethod.GET, auth = true)
    public void getBalance(RoutingContext ctx, @Param String address) {
        String name = ctx.pathParam("name");
        WalletService service = WalletServiceManager.getService(name);
        String balance = service.getBalance(address);
        if(balance==null){
            ctx.response().write(balance);
            ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
        }else{
            ctx.response().write(balance);
            ctx.response().setStatusCode(200);
        }
        ctx.response().end();
    }

    @RouteRegistration(uri = "/wallet/:name/create", method = HttpMethod.POST, auth = true)
    public void create(RoutingContext ctx, @Param String password) {
        String name = ctx.pathParam("name");
        WalletService service = WalletServiceManager.getService(name);
        JsonObject result = service.createWallet(password);
        ctx.response().putHeader("content-type", "application/json");
        ctx.response().write(result.encodePrettily());
        ctx.response().end();
    }

    @RouteRegistration(uri = "/wallet:name/send", method = HttpMethod.POST, auth = true)
    public void send(RoutingContext ctx,
                     @Param String walletFileName,
                     @Param String walletJsonFile,
                     @Param String password,
                     @Param String targetAddress,
                     @Param String amount) {
        String name = ctx.pathParam("name");
        WalletService service = WalletServiceManager.getService(name);
        TransactionResult result = service.send(walletFileName, walletJsonFile, password, targetAddress, amount);
        ctx.response().putHeader("content-type", "application/json");
        ctx.response().write(gson.toJson(result));
        ctx.response().end();
    }

    @RouteRegistration(uri= "/status/:address", method = HttpMethod.GET, auth= true)
    public void status(RoutingContext ctx, @Param String address){

    }
}
