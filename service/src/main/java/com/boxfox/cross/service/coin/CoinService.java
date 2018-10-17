package com.boxfox.cross.service.coin;

import com.boxfox.cross.service.JooqReactiveService;
import com.linkbit.android.entity.CoinModel;
import io.reactivex.Single;
import java.util.List;

public class CoinService extends JooqReactiveService implements CoinUsecase {

  private CoinServiceImpl impl;

  public CoinService() {
    this.impl = new CoinServiceImpl();
  }

  @Override
  public Single<List<CoinModel>> getAllCoins() {
    return createSingle(ctx -> impl.getAllCoins(ctx));
  }
}
