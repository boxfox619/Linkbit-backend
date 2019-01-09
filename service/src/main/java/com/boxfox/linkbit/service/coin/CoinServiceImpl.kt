package com.boxfox.linkbit.service.coin

import com.boxfox.linkbit.common.RoutingException
import com.boxfox.linkbit.common.data.RedisUtil
import com.boxfox.linkbit.common.entity.coin.CoinModel
import com.boxfox.linkbit.common.entity.coin.CoinPriceModel
import com.google.api.client.http.HttpStatusCodes
import io.one.sys.db.Tables.COIN
import io.one.sys.db.Tables.COINNAME

import org.jooq.DSLContext
import redis.clients.jedis.JedisPool

class CoinServiceImpl(private val jedisPool: JedisPool = RedisUtil.createPool()) : CoinUsecase {

    override fun getAllPrices(ctx: DSLContext, locale: String): List<CoinPriceModel> {
        return updatePrice(getCoins(ctx, locale))
    }

    override fun getPrices(ctx: DSLContext, symbols: List<String>, locale: String): List<CoinPriceModel> {
        return updatePrice(getCoins(ctx, locale).filter { symbols.contains(it.symbol) })
    }

    override fun getCoins(ctx: DSLContext, locale: String): List<CoinModel> {
        return ctx.selectFrom(COIN.join(COINNAME).on(COIN.SYMBOL.eq(COINNAME.SYMBOL))).where(COINNAME.LOCALE.eq(locale)).fetch().map { record ->
            CoinPriceModel().apply {
                this.name = record.get(COINNAME.NAME)
                this.symbol = record.get(COIN.SYMBOL)
                this.themeColor = record.get(COIN.COLOR)
            }
        }
    }

    private fun updatePrice(coins: List<CoinModel>): List<CoinPriceModel> {
        val priceMap = getPrice(coins.map { it.symbol })
        return coins.map { CoinPriceModel(it).apply {
            this.price = priceMap.getOrDefault(it.symbol.toUpperCase(), 0.toDouble())
        } }
    }

    override fun getPrice(symbols: List<String>): Map<String, Double> {
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

    override fun getPrice(symbol: String, balance: Double): Double {
        return this.getPrice(symbol) * balance
    }

    override fun getPrice(symbol: String): Double {
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
