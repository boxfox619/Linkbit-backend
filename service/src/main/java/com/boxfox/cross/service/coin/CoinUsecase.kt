package com.boxfox.cross.service.coin

import com.boxfox.cross.common.entity.CoinModel
import org.jooq.DSLContext

interface CoinUsecase {
    fun getAllCoins(ctx: DSLContext): List<CoinModel>
}
