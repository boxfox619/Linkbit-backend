package com.boxfox.linkbit.service.coin

import com.boxfox.linkbit.common.data.RedisUtil
import io.vertx.core.AbstractVerticle
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import redis.clients.jedis.JedisPool


class PriceIndexingVerticle(private val logger: Logger = LogManager.getRootLogger(),
                            private val jedisPool: JedisPool = RedisUtil.createPool()) : AbstractVerticle() {
    override fun start() {
        //@TODO
        priceParsingCycle()
    }

    private fun priceParsingCycle() {
        CoinMarketCapIndexer(this.vertx).indexing().subscribe({ priceMap ->
            logger.debug("indexing coin price : ${priceMap.size}")
            priceMap.keys.forEach { symbol ->
                jedisPool.resource.use { jedis ->
                    jedis.hset("Currency", symbol, priceMap[symbol].toString())
                }
            }
        }, { logger.error(it.message, it) })
    }
}