package com.boxfox.cross.service.coin

import com.boxfox.cross.service.JooqReactiveService
import com.linkbit.android.entity.CoinModel
import io.reactivex.Single

class CoinService(private val impl: CoinServiceImpl = CoinServiceImpl()) : JooqReactiveService() {

    val allCoins: Single<List<CoinModel>> = createSingle { ctx -> impl.getAllCoins(ctx) }
}
