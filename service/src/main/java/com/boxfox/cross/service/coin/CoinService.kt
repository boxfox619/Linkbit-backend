package com.boxfox.cross.service.coin

import com.boxfox.cross.common.entity.CoinModel
import com.boxfox.cross.service.JooqReactiveService
import io.reactivex.Single

class CoinService(private val impl: CoinServiceImpl = CoinServiceImpl()) : JooqReactiveService() {

    val list: Single<List<CoinModel>> = single { ctx -> impl.getAllCoins(ctx) }
}
