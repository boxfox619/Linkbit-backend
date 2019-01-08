package com.boxfox.linkbit.service.coin

import com.boxfox.linkbit.common.RoutingException
import com.boxfox.linkbit.common.data.RedisUtil
import com.boxfox.linkbit.common.entity.coin.CoinModel
import com.boxfox.linkbit.common.entity.coin.CoinPriceModel
import com.google.api.client.http.HttpStatusCodes
import io.one.sys.db.Tables.COIN
import io.one.sys.db.tables.records.CoinRecord

import org.jooq.DSLContext
import redis.clients.jedis.JedisPool

class CoinServiceImpl(private val jedisPool: JedisPool = RedisUtil.createPool()) : CoinUsecase {

    override fun getAllPrices(ctx: DSLContext, moneySymbol: String): List<CoinPriceModel> {
        return updatePrice(getCoins(ctx), moneySymbol)
    }

    override fun getPrices(ctx: DSLContext, symbols: List<String>, moneySymbol: String): List<CoinPriceModel> {
        return updatePrice(getCoins(ctx).filter { symbols.contains(it.symbol) }, moneySymbol)
    }

    override fun getCoins(ctx: DSLContext): List<CoinModel> {
        return ctx.selectFrom<CoinRecord>(COIN).fetch().map { record ->
            CoinPriceModel().apply {
                this.name = record.name
                this.symbol = record.symbol
                this.themeColor = record.color
            }
        }
    }

    private fun updatePrice(coins: List<CoinModel>, moneySymbol: String): List<CoinPriceModel> {
        val priceMap = getPrice(coins.map { it.symbol }, moneySymbol)
        return coins.map { CoinPriceModel(it).apply {
            this.price = priceMap.getOrDefault(it.symbol.toUpperCase(), 0.toDouble())
        } }
    }

    override fun getPrice(symbols: List<String>, moneySymbol: String): Map<String, Double> {
        val coins = HashMap<String, Double>()
        jedisPool.resource.use { jedis ->
            val priceMap = jedis.hgetAll("Currency")
            symbols.forEach {
                val value = priceMap.getOrDefault(it.toUpperCase(), "0")
                coins[it] = value.toDouble()
            }
        }
        return coins
    }

    override fun getPrice(symbol: String, moneySymbol: String, balance: Double): Double {
        return this.getPrice(symbol, moneySymbol) * balance
    }

    override fun getPrice(symbol: String, moneySymbol: String): Double {
        jedisPool.resource.use { jedis ->
            val value = jedis.hget("Currency", symbol.toUpperCase())
            if (value != null) {
                return value.toDouble()
            } else {
                throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "coin currency not found")
            }
        }
    }
}
