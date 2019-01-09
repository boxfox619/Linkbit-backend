package com.boxfox.linkbit.service.coin

import com.boxfox.linkbit.common.entity.coin.CoinModel
import com.boxfox.linkbit.common.entity.coin.CoinPriceModel
import org.jooq.DSLContext

interface CoinUsecase {

    fun getCoins(ctx: DSLContext, locale: String): List<CoinModel>

    fun getAllPrices(ctx: DSLContext, locale: String): List<CoinPriceModel>

    fun getPrices(ctx: DSLContext, symbols: List<String>, locale: String): List<CoinPriceModel>

    fun getPrice(symbol: String, balance: Double): Double

    fun getPrice(symbol: String): Double

    fun getPrice(symbols: List<String>): Map<String, Double>
}
