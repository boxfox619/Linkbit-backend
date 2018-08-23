package com.boxfox.core.router.wallet;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.common.vertx.router.AbstractRouter;
import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.common.vertx.service.Service;
import com.boxfox.cross.service.AddressService;
import com.boxfox.cross.service.PriceService;
import com.boxfox.cross.service.model.Wallet;
import com.boxfox.cross.wallet.WalletService;
import com.boxfox.cross.wallet.WalletServiceManager;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.one.sys.db.tables.daos.WalletDao;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import java.util.ArrayList;
import java.util.List;

public class WalletLookupRouter extends WalletRouter {

  @Service
  private AddressService addressService;
  @Service
  private PriceService priceService;

  @RouteRegistration(uri = "/wallet/list", method = HttpMethod.GET, auth = true)
  public void getWallets(RoutingContext ctx) {
    String locale = ctx.data().get("locale").toString();
    new Thread(() -> {
      String uid = (String) ctx.data().get("uid");
      System.out.println("Wallet list lookup : " + uid);
      WalletDao dao = new WalletDao(PostgresConfig.create());
      List<Wallet> wallets = new ArrayList<>();
      List<io.one.sys.db.tables.pojos.Wallet> list = dao.fetchByUid(uid);
      for (int i = 0; i < list.size(); i++) {
        io.one.sys.db.tables.pojos.Wallet wallet = list.get(i);
        WalletService service = WalletServiceManager.getService(wallet.getSymbol());
        double balance = service.getBalance(wallet.getAddress());
        double krBalance = priceService.getPrice(wallet.getAddress(), locale, balance);
        Wallet walletObj = Wallet.fromDao(wallet);
        walletObj.setBalance(balance);
        walletObj.setKrBalance(krBalance);
        wallets.add(walletObj);
      }
      ctx.response().end(gson.toJson(wallets));
    }).start();
  }

  @RouteRegistration(uri = "/wallet/balance", method = HttpMethod.GET, auth = true)
  public void getBalance(RoutingContext ctx, @Param String address) {
    addressService.findByAddress(address, res -> {
      if (res.result() != null) {
        WalletService service = WalletServiceManager.getService(res.result().getSymbol());
        double balance = service.getBalance(res.result().getOriginalAddress());
        if (balance < 0) {
          ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
        } else {
          ctx.response().setStatusCode(200).setChunked(true).write(String.valueOf(balance));
        }
      } else {
        ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
      }
      ctx.response().end();
    });
  }

  @RouteRegistration(uri = "/wallet/price", method = HttpMethod.GET, auth = true)
  public void getPrice(RoutingContext ctx, @Param String address) {
    String locale = ctx.data().get("locale").toString();
    addressService.findByAddress(address, res -> {
      String symbol = res.result().getSymbol();
      String originalAddress = res.result().getOriginalAddress();
      WalletService service = WalletServiceManager.getService(symbol);
      if (res.result() != null && service != null) {
        double price = priceService
            .getPrice(res.result().getSymbol(), locale, service.getBalance(originalAddress));
        ctx.response().setStatusCode(200).setChunked(true).write(String.valueOf(price));
      } else {
        ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
      }
      ctx.response().end();
    });
  }

  @RouteRegistration(uri = "/wallet/:symbol/balance/all", method = HttpMethod.GET, auth = true)
  public void getTotalBalance(RoutingContext ctx, @Param String symbol) {
    doAsync(future -> {
      String uid = (String) ctx.data().get("uid");
      WalletService walletService = WalletServiceManager.getService(symbol);
      if (walletService != null) {
        WalletDao dao = new WalletDao(PostgresConfig.create());
        List<io.one.sys.db.tables.pojos.Wallet> list = dao.fetchByUid(uid);
        double totlaBalance = 0;
        for (int i = 0; i < list.size(); i++) {
          totlaBalance += walletService.getBalance(list.get(i).getAddress());
        }
        ctx.response().setStatusCode(200).setChunked(true).write(String.valueOf(totlaBalance));
      } else {
        ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
      }
      ctx.response().end();
      future.complete();
    });
  }

  @RouteRegistration(uri = "/wallet/:symbol/price/all", method = HttpMethod.GET, auth = true)
  public void getTotalPrice(RoutingContext ctx, @Param String symbol) {
    String locale = ctx.data().get("locale").toString();
    doAsync(future -> {
      String uid = (String) ctx.data().get("uid");
      WalletService walletService = WalletServiceManager.getService(symbol);
      if (walletService != null) {
        WalletDao dao = new WalletDao(PostgresConfig.create());
        List<io.one.sys.db.tables.pojos.Wallet> list = dao.fetchByUid(uid);
        double totlaPrice = 0;
        for (int i = 0; i < list.size(); i++) {
          double balance = walletService.getBalance(list.get(i).getAddress());
          totlaPrice += priceService.getPrice(symbol, locale, balance);
        }
        ctx.response().setStatusCode(200).setChunked(true).write(String.valueOf(totlaPrice));
      } else {
        ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
      }
      ctx.response().end();
      future.complete();
    });
  }

  @RouteRegistration(uri = "/wallet", method = HttpMethod.GET, auth = true)
  public void walletInfoLookup(RoutingContext ctx, @Param String address) {
    addressService.findByAddress(address, res -> {
      if (res.result() == null) {
        ctx.response().setStatusCode(NO_CONTENT.code());
      } else {
        ctx.response().setChunked(true).write(gson.toJson(res.result()));
      }
      ctx.response().end();
    });
  }
}
