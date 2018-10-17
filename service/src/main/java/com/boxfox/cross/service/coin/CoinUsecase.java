package com.boxfox.cross.service.coin;

import com.linkbit.android.entity.CoinModel;
import io.reactivex.Single;
import java.util.List;

public interface CoinUsecase {
  Single<List<CoinModel>> getAllCoins();
}
