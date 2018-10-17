package com.boxfox.cross.service.coin;

import static io.one.sys.db.Tables.COIN;

import com.linkbit.android.entity.CoinModel;
import java.util.List;
import org.jooq.DSLContext;

public class CoinServiceImpl {

  public List<CoinModel> getAllCoins(DSLContext ctx) {
    List<CoinModel> coins = ctx.selectFrom(COIN).fetch().map(coinRecord -> {
      CoinModel coinModel = new CoinModel();
      coinModel.setName(coinRecord.getName());
      coinModel.setSymbol(coinRecord.getSymbol());
      return coinModel;
    });
    return coins;
  }
}
