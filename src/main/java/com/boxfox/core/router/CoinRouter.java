package com.boxfox.core.router;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.common.vertx.service.Service;
import com.boxfox.cross.service.PriceService;
import com.google.api.client.http.HttpStatusCodes;
import io.one.sys.db.tables.daos.CoinDao;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class CoinRouter {

  @Service
  private PriceService priceService;

  @RouteRegistration(uri = "/coin/support/list", method = HttpMethod.GET, auth = true)
  public void getSupportCoinList(RoutingContext ctx) {
    CoinDao dao = new CoinDao(PostgresConfig.create());
    JsonArray coins = new JsonArray();
    dao.findAll().forEach(c -> {
      coins.add(new JsonObject().put("symbol", c.getSymbol()).put("name", c.getName()));
    });
    ctx.response().end(coins.encode());
  }


  @RouteRegistration(uri = "/coin/price", method = HttpMethod.GET, auth = true)
  public void getCoinPrice(RoutingContext ctx, @Param String symbol) {
    String locale = ctx.data().get("locale").toString();
    double price = priceService.getPrice(symbol, locale);
    if (price > 0) {
      ctx.response().end(new JsonObject().put("price", price).encode());
    } else {
      ctx.response().setStatusCode(HttpStatusCodes.STATUS_CODE_NOT_FOUND).end();
    }
  }
}
