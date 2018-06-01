package com.boxfox.core.router.wallet;

import com.boxfox.cross.common.data.PostgresConfig;
import com.boxfox.cross.service.AddressService;
import com.boxfox.cross.common.vertx.router.RouteRegistration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.one.sys.db.tables.daos.CoinDao;
import io.one.sys.db.tables.pojos.Coin;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.jooq.DSLContext;

import java.util.List;

import static com.boxfox.cross.common.data.PostgresConfig.createContext;
import static io.one.sys.db.Tables.COIN;

public class WalletRouter {
    protected AddressService addressService;
    protected Gson gson;
    protected DSLContext create;

    public WalletRouter() {
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
        this.addressService = new AddressService();
        this.create = createContext();
    }

    @RouteRegistration(uri = "/support/wallet/list", method = HttpMethod.GET, auth = true)
    public void getSupportWalletList(RoutingContext ctx) {
        CoinDao dao = new CoinDao(PostgresConfig.create());
        JsonArray coins = new JsonArray();
        dao.findAll().forEach(c -> {
            coins.add(new JsonObject().put("symbol", c.getSymbol()).put("name", c.getName()));
        });
        ctx.response().end(coins.encode());
    }
}
