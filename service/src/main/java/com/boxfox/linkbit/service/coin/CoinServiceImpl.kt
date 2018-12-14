package com.boxfox.linkbit.service.coin

import com.boxfox.linkbit.common.RoutingException
import com.boxfox.linkbit.common.data.RedisConfig
import com.boxfox.linkbit.common.entity.CoinModel
import com.google.api.client.http.HttpStatusCodes
import io.one.sys.db.Tables.COIN
import io.one.sys.db.tables.records.CoinRecord

import org.jooq.DSLContext

class CoinServiceImpl : CoinUsecase {

    override fun getAllCoins(ctx: DSLContext, moneySymbol: String): List<CoinModel> {
        val coins = ctx.selectFrom<CoinRecord>(COIN).fetch().map { record ->
            CoinModel().apply {
                this.name = record.name
                this.symbol = record.symbol
            }
        }
        val priceMap = getPrice(coins.map { it.symbol }, moneySymbol)
        return coins.map { it.apply { this.price = priceMap.getOrDefault(it.symbol.toUpperCase(), 0.toDouble()) } }
    }

    override fun getPrice(symbols: List<String>, moneySymbol: String): Map<String, Double> {
        val coins = HashMap<String, Double>()
        RedisConfig.createPool().resource.use { jedis ->
            val priceMap = jedis.hgetAll("currency")
            symbols.forEach {
                val key = String.format("%s-%s", it.toUpperCase(), moneySymbol)
                val value = priceMap.getOrDefault(key, "0")
                coins[it] = value.toDouble()
            }
        }
        return coins
    }

    override fun getPrice(symbol: String, moneySymbol: String, balance: Double): Double {
        return this.getPrice(symbol, moneySymbol) * balance
    }

    override fun getPrice(symbol: String, moneySymbol: String): Double {
        RedisConfig.createPool().resource.use { jedis ->
            val value = jedis.hget("currency", String.format("%s-%s", symbol.toUpperCase(), moneySymbol))
            if (value!=null) {
                return value.toDouble()
            } else {
                throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "coin currency not found")
            }
        }
    }
}
