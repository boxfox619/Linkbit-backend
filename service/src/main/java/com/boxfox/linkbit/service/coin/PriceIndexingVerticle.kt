package com.boxfox.linkbit.service.coin

import com.boxfox.linkbit.common.data.RedisConfig
import com.boxfox.vertx.data.Config
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import io.vertx.core.AbstractVerticle
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.json.JSONObject
import java.util.*


class PriceIndexingVerticle(private val logger: Logger = LogManager.getRootLogger()) : AbstractVerticle() {
    override fun start() {
        //@TODO
        priceParsingCycle()
    }

    private fun priceParsingCycle() {
        val pool = RedisConfig.createPool()
        CoinMarketCapIndexer(this.vertx).indexing().subscribe({ priceMap ->
            logger.debug("indexing coin price : ${priceMap.size}")
            pool.resource.use { jedis ->
                priceMap.keys.forEach { symbol ->
                    jedis.hset("Currency", symbol, priceMap[symbol].toString())
                }
            }
        }, { logger.error(it.message, it) })
    }
}