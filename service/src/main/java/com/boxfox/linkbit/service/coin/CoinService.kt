package com.boxfox.linkbit.service.coin

import com.boxfox.linkbit.common.entity.CoinModel
import com.boxfox.linkbit.service.JooqReactiveService
import io.reactivex.Single

class CoinService(private val impl: com.boxfox.linkbit.service.coin.CoinServiceImpl = com.boxfox.linkbit.service.coin.CoinServiceImpl()) : com.boxfox.linkbit.service.JooqReactiveService() {

    val list: Single<List<CoinModel>> = single { ctx -> impl.getAllCoins(ctx) }
}
