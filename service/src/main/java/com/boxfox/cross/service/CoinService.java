package com.boxfox.cross.service;

import com.boxfox.vertx.service.AbstractService;
import com.linkbit.android.entity.CoinModel;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

import static com.boxfox.cross.util.JooqUtil.useContext;
import static io.one.sys.db.Tables.COIN;

public class CoinService extends AbstractService{

    public void getAllCoins(Handler<AsyncResult<List<CoinModel>>> hander) {
        doAsync(future -> {
            useContext(ctx -> {
                List<CoinModel> coins =  ctx.selectFrom(COIN).fetch().map(coinRecord -> {
                    CoinModel coinModel = new CoinModel();
                    coinModel.setName(coinRecord.getName());
                    coinModel.setSymbol(coinRecord.getSymbol());
                    return coinModel;
                });
                future.complete(coins);
            });
        }, hander);
    }
}
