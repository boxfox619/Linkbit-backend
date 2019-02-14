package com.boxfox.linkbit.service.currency

import com.boxfox.linkbit.common.data.PostgresConfig
import com.boxfox.linkbit.common.data.RedisUtil
import com.mashape.unirest.http.Unirest
import io.one.sys.db.tables.daos.LocaleDao
import io.vertx.core.AbstractVerticle
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import redis.clients.jedis.JedisPool
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit


class CurrencyIndexingVerticle(private val logger: Logger = LogManager.getRootLogger(),
                               private val jedisPool: JedisPool = RedisUtil.createPool(),
                               private val localeDao: LocaleDao = LocaleDao(PostgresConfig.create())) : AbstractVerticle() {
    private val CURRENCY_API = "https://free.currencyconverterapi.com/api/v6/convert?apiKey=1b68d72a5f4db84bf356&q=%s&compact=ultra"
    override fun start() {
        indexing()
        currencyParsingCycle()
    }

    private fun indexing() {
        logger.debug("indexing currency")
        localeDao.findAll().filter { !it.currency.equals("USD") }.forEach{
            val currency = it.currency.toUpperCase()
            val key= String.format("USD_%s", currency)
            logger.debug(String.format("indexing currency %s", key))
            val money = Unirest.get(String.format(CURRENCY_API, key )).asJson().body.`object`.getDouble(key)
            jedisPool.resource.use { jedis ->
                jedis.hset("Money", currency, money.toString())
            }
        }
    }

    private fun currencyParsingCycle() {
        CompletableFuture.delayedExecutor(60, TimeUnit.HOURS).execute {
            indexing()
            currencyParsingCycle()
        }
    }
}