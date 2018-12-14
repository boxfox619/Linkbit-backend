package com.boxfox.linkbit.service.coin

import com.boxfox.linkbit.common.entity.CoinModel
import org.jooq.DSLContext

interface CoinUsecase {
    fun getAllCoins(ctx: DSLContext): List<CoinModel>
}
