package com.boxfox.linkbit.service.coin

import com.boxfox.linkbit.common.entity.CoinModel
import org.jooq.DSLContext

interface CoinUsecase {
    fun getAllCoins(ctx: DSLContext, moneySymbol: String): List<CoinModel>

    fun getPrice(symbol: String, moneySymbol: String, balance: Double): Double

    fun getPrice(symbol: String, moneySymbol: String): Double

    fun getPrice(symbols: List<String>, moneySymbol: String): Map<String, Double>
}
