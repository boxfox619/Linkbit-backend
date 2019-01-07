package com.boxfox.linkbit.service.coin

import com.boxfox.linkbit.common.data.RedisUtil
import io.vertx.core.AbstractVerticle
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import redis.clients.jedis.JedisCluster


class PriceIndexingVerticle(private val logger: Logger = LogManager.getRootLogger(),
                            private  val jedis: JedisCluster = RedisUtil.create()) : AbstractVerticle() {
    override fun start() {
        //@TODO
        priceParsingCycle()
    }

    private fun priceParsingCycle() {
        CoinMarketCapIndexer(this.vertx).indexing().subscribe({ priceMap ->
            logger.debug("indexing coin price : ${priceMap.size}")
            priceMap.keys.forEach { symbol ->
                jedis.hset("Currency", symbol, priceMap[symbol].toString())
            }
        }, { logger.error(it.message, it) })
    }
}