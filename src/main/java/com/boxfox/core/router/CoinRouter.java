package com.boxfox.core.router;

import com.boxfox.core.router.model.CoinPriceNetworkObject;
import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.service.CoinService;
import com.boxfox.vertx.router.*;
import com.boxfox.vertx.service.*;
import com.boxfox.cross.service.LocaleService;
import com.boxfox.cross.service.PriceService;
import com.google.api.client.http.HttpStatusCodes;
import com.linkbit.android.entity.CoinModel;
import io.one.sys.db.tables.daos.CoinDao;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;

import static com.boxfox.cross.util.LogUtil.getLogger;

public class CoinRouter extends AbstractRouter {

    @Service
    private PriceService priceService;

    @Service
    private LocaleService localeService;

    @Service
    private CoinService coinService;

    @RouteRegistration(uri = "/coin/supported/list", method = HttpMethod.GET)
    public void getSupportWalletList(RoutingContext ctx) {
        getLogger().debug(String.format("Supported Coin Load : %s", ctx.request().remoteAddress().host()));
        this.coinService.getAllCoins(res -> {
            if (res.succeeded()) {
                ctx.response().end(gson.toJson(res.result()));
            } else {
                ctx.response().setStatusCode(HttpStatusCodes.STATUS_CODE_SERVER_ERROR).end();
            }
        });
    }

    @RouteRegistration(uri = "/coin/price", method = HttpMethod.GET)
    public void getCoinPrice(RoutingContext ctx, @Param(name = "symbol") String symbol, @Param(name = "locale") String locale) {
        localeService.getLocaleMoneySymbol(locale, res -> {
            if (res.succeeded()) {
                String unit = res.result();
                double price = priceService.getPrice(symbol, locale);
                if (price > 0) {
                    CoinPriceNetworkObject result = new CoinPriceNetworkObject();
                    result.setAmount(price);
                    result.setUnit(unit);
                    ctx.response().end(gson.toJson(result));
                } else {
                    ctx.response().setStatusCode(404).end();
                }
            } else {
                ctx.response().setStatusCode(500).end();
            }
        });
    }

}
