package com.boxfox.linkbit.service.coin

import com.boxfox.linkbit.common.entity.coin.CoinModel
import com.boxfox.linkbit.common.entity.coin.CoinPriceModel
import org.jooq.DSLContext

interface CoinUsecase {

    fun getCoins(ctx: DSLContext): List<CoinModel>

    fun getAllPrices(ctx: DSLContext, moneySymbol: String): List<CoinPriceModel>

    fun getPrices(ctx: DSLContext, symbols: List<String>, moneySymbol: String): List<CoinPriceModel>

    fun getPrice(symbol: String, moneySymbol: String, balance: Double): Double

    fun getPrice(symbol: String, moneySymbol: String): Double

    fun getPrice(symbols: List<String>, moneySymbol: String): Map<String, Double>
}
