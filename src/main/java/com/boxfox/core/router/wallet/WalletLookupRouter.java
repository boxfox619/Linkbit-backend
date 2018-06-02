package com.boxfox.core.router.wallet;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.service.model.Wallet;
import com.boxfox.cross.service.wallet.WalletService;
import com.boxfox.cross.service.wallet.WalletServiceManager;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.one.sys.db.tables.daos.WalletDao;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;

public class WalletLookupRouter extends WalletRouter {

    @RouteRegistration(uri = "/wallet/list", method = HttpMethod.GET, auth = true)
    public void getWallets(RoutingContext ctx) {
        String uid = (String) ctx.data().get("uid");
        System.out.println("Wallet list lookup : "+uid);
        WalletDao dao = new WalletDao(PostgresConfig.create());
        List<Wallet> wallets = new ArrayList<>();
        List<io.one.sys.db.tables.pojos.Wallet> list = dao.fetchByUid(uid);
        for(int i = 0 ; i < list.size() ; i++){
            io.one.sys.db.tables.pojos.Wallet wallet = list.get(i);
            WalletService service = WalletServiceManager.getService(wallet.getSymbol());
            String balance = service.getBalance(wallet.getAddress());
            double price = service.getPrice(wallet.getAddress());
            Wallet walletObj = Wallet.fromDao(wallet);
            walletObj.setBalance(balance);
            walletObj.setKrBalance(price);
            wallets.add(walletObj);
        }
        ctx.response().end(gson.toJson(wallets));
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

    @RouteRegistration(uri = "/wallet/:symbol/price", method = HttpMethod.GET, auth = true)
    public void getPrice(RoutingContext ctx, @Param String symbol, @Param String address) {
        WalletService service = WalletServiceManager.getService(symbol);
        if (service != null) {
            double price = service.getPrice(address);
            ctx.response().setStatusCode(200).setChunked(true).write(price+"");
        } else {
            ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
        }
        ctx.response().end();
    }

    @RouteRegistration(uri = "/wallet/:symbol/price/all", method = HttpMethod.GET, auth = true)
    public void getTotalPrice(RoutingContext ctx, @Param String symbol) {
        String uid = ctx.user().principal().getString("su");
        WalletService service = WalletServiceManager.getService(symbol);
        if (service != null) {
            WalletDao dao = new WalletDao(PostgresConfig.create());
            List<io.one.sys.db.tables.pojos.Wallet> list = dao.fetchByUid(uid);
            double totlaPrice = 0;
            for(int i = 0 ; i < list.size() ; i++){
                totlaPrice += service.getPrice(list.get(i).getAddress());
            }
            ctx.response().setStatusCode(200).setChunked(true).write(totlaPrice+"");
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
