package com.boxfox.linkbit.service.coin

import com.boxfox.linkbit.common.entity.CoinModel
import io.one.sys.db.Tables.COIN
import io.one.sys.db.tables.records.CoinRecord

import org.jooq.DSLContext

class CoinServiceImpl : com.boxfox.linkbit.service.coin.CoinUsecase {

    override fun getAllCoins(ctx: DSLContext): List<CoinModel> {
        return ctx.selectFrom<CoinRecord>(COIN).fetch().map { coinRecord ->
            val coinModel = CoinModel()
            coinModel.name = coinRecord.getName()
            coinModel.symbol = coinRecord.getSymbol()
            coinModel
        }
    }
}
