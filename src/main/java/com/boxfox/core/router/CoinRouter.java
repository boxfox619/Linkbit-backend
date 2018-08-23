package com.boxfox.core.router;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.common.vertx.router.AbstractRouter;
import com.boxfox.cross.common.vertx.router.Param;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.boxfox.cross.common.vertx.service.Service;
import com.boxfox.cross.service.PriceService;
import com.linkbit.android.data.model.coin.CoinPriceNetworkObject;
import io.one.sys.db.tables.daos.CoinDao;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class CoinRouter extends AbstractRouter{

    @Service
    private PriceService priceService;

    @RouteRegistration(uri = "/coin/supported/list", method = HttpMethod.GET, auth = true)
    public void getSupportWalletList(RoutingContext ctx) {
        CoinDao dao = new CoinDao(PostgresConfig.create());
        JsonArray coins = new JsonArray();
        dao.findAll().forEach(c -> {
            coins.add(new JsonObject().put("symbol", c.getSymbol()).put("name", c.getName()));
        });
        ctx.response().end(coins.encode());
    }

    @RouteRegistration(uri = "/coin/price", method = HttpMethod.GET, auth = true)
    public void getCoinPrice(RoutingContext ctx, @Param String symbol, @Param String locale) {
        String unit = priceService.getUnit(locale);
        double price = priceService.getPrice(symbol, locale);
        if(price > 0){
            CoinPriceNetworkObject result = new CoinPriceNetworkObject();
            result.setAmount(price);
            result.setUnit(unit);
            ctx.response().end(gson.toJson(result));
        }else{
            ctx.response().setStatusCode(404).end();
        }
    }

}
