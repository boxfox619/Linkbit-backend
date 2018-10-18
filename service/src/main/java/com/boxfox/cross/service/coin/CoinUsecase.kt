package com.boxfox.cross.service.coin

import com.linkbit.android.entity.CoinModel
import org.jooq.DSLContext

interface CoinUsecase {
    fun getAllCoins(ctx: DSLContext): List<CoinModel>
}
