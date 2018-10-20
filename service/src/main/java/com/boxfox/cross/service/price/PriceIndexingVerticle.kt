package com.boxfox.cross.service.price

import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import io.vertx.core.AbstractVerticle
import io.vertx.redis.RedisClient
import io.vertx.redis.RedisOptions
import org.apache.log4j.Logger
import org.json.JSONObject
import java.util.*


class PriceIndexingVerticle : AbstractVerticle() {
    override fun start() {
        priceParsingCycle()
    }

    fun priceParsingCycle() {
        val config = RedisOptions().setHost("127.0.0.1")
        val redis = RedisClient.create(vertx, config)
        try {
            val array = Unirest.get(COINMARKET_CAP_URL + "listings/").asJson().body.getObject().getJSONArray("data")
            array.forEach { obj ->
                val coin = obj as JSONObject
                val symbol = coin.getString("symbol")
                val id = coin.getInt("id")
                for (money in moneySymbols) {
                    val price = parseCoinPrice(id, money)
                    redis.hset("Currency", String.format("%s-%s", symbol, money), price.toString()) {}
                }
            }
            vertx.setTimer(3000) {
                priceParsingCycle()
            }
        } catch (e: UnirestException) {
            e.printStackTrace()
        } finally {
            redis.close{}
        }
    }

    fun parseCoinPrice(id: Int, moneySymbol: String): Double {
        val url = String.format("%sticker/%d/?convert=%s", COINMARKET_CAP_URL, id, moneySymbol)
        val obj = Unirest.get(url).asJson().body.getObject()
        val value = obj.getJSONObject("data").getJSONObject("quotes").getJSONObject(moneySymbol)
        return value.getDouble("price")
    }

    companion object {
        private val COINMARKET_CAP_URL = "https://api.coinmarketcap.com/v2/"
        private val moneySymbols: List<String> = Arrays.asList("KRW", "")
    }
}