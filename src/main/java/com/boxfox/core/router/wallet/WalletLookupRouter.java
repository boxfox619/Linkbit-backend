package com.boxfox.core.router.wallet;

import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.service.model.Wallet;
import com.boxfox.cross.service.wallet.WalletService;
import com.boxfox.cross.service.wallet.WalletServiceManager;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.one.sys.db.Tables.COIN;
import static io.one.sys.db.tables.Wallet.WALLET;

public class WalletLookupRouter extends WalletRouter {

    @RouteRegistration(uri = "/wallet/list", method = HttpMethod.GET, auth = true)
    public void getWallets(RoutingContext ctx) {
        String ownUid = (String) ctx.data().get("uid");
        JsonArray wallets = new JsonArray();
        create.select()
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

    @RouteRegistration(uri = "/wallet/:symbol/lookup/", method = HttpMethod.GET, auth = true)
    public void walletInfoLookup(RoutingContext ctx, @Param String symbol, @Param String address) {
        Wallet wallet = addressService.findByAddress(symbol, address);
        if (wallet == null) {
            ctx.response().setStatusCode(NO_CONTENT.code());
        } else {
            ctx.response().setChunked(true).write(gson.toJson(wallet));
        }
        ctx.response().end();
    }
}
