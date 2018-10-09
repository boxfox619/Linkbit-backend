package com.boxfox.core.router;

import com.boxfox.core.router.model.CoinPriceNetworkObject;
import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.vertx.router.*;
import com.boxfox.vertx.service.*;
import com.boxfox.cross.service.LocaleService;
import com.boxfox.cross.service.PriceService;
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

    @RouteRegistration(uri = "/coin/supported/list", method = HttpMethod.GET, auth = true)
    public void getSupportWalletList(RoutingContext ctx) {
        getLogger().debug(String.format("Supported Coin Load : %s", ctx.request().remoteAddress().host()));
        doAsync(future -> {
            CoinDao dao = new CoinDao(PostgresConfig.create(),getVertx());
            List<CoinModel> coins = new ArrayList();
            dao.findAll().result().forEach(c -> {
                CoinModel coin = new CoinModel();
                coin.setSymbol(c.getSymbol());
                coin.setName(c.getName());
                coins.add(coin);
            });
            ctx.response().end(gson.toJson(coins));
            future.complete();
        });
    }

    @RouteRegistration(uri = "/coin/price", method = HttpMethod.GET, auth = true)
    public void getCoinPrice(RoutingContext ctx, @Param(name="symbol") String symbol, @Param(name="locale") String locale) {
        localeService.getLocaleMoneySymbol(locale, res -> {
            if(res.succeeded()) {
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
            }else{
                ctx.response().setStatusCode(500).end();
            }
        });
    }

}
