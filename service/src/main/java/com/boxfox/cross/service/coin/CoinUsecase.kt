package com.boxfox.cross.service.coin

import com.linkbit.android.entity.CoinModel
import io.reactivex.Single

interface CoinUsecase {
    val allCoins: Single<List<CoinModel>>
}
