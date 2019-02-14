package com.boxfox.linkbit.service.currency

import com.boxfox.linkbit.common.data.RedisUtil
import redis.clients.jedis.JedisPool

class CurrencyServiceImpl(private val jedisPool: JedisPool = RedisUtil.createPool()) : CurrencyUsecase {

    override fun getCurrency(currency: String): Double {
        return jedisPool.resource.use { jedis ->
            val priceMap = jedis.hgetAll("Money")
            val value = priceMap.getOrDefault(currency.toUpperCase(), "1")
            return value.toDouble()
        }
    }

}