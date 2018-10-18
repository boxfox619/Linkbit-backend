package com.boxfox.cross.service.coin

import io.one.sys.db.Tables.COIN

import com.linkbit.android.entity.CoinModel
import io.one.sys.db.tables.records.CoinRecord
import org.jooq.DSLContext

class CoinServiceImpl : CoinUsecase{

    override fun getAllCoins(ctx: DSLContext): List<CoinModel> {
        return ctx.selectFrom<CoinRecord>(COIN).fetch().map { coinRecord ->
            val coinModel = CoinModel()
            coinModel.name = coinRecord.getName()
            coinModel.symbol = coinRecord.getSymbol()
            coinModel
        }
    }
}
