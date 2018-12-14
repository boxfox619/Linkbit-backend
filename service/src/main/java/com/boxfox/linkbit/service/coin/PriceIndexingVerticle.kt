package com.boxfox.linkbit.service.coin

import com.boxfox.linkbit.common.data.RedisConfig
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import io.vertx.core.AbstractVerticle
import org.json.JSONObject
import java.util.*


class PriceIndexingVerticle : AbstractVerticle() {
    override fun start() {
        //@TODO
        priceParsingCycle()
    }

    fun priceParsingCycle() {
        val pool = RedisConfig.createPool()
        pool.resource.use {jedis ->
            try  {
                val array = Unirest.get(COINMARKET_CAP_URL + "listings/").asJson().body.getObject().getJSONArray("data")
                array.forEach { obj ->
                    val coin = obj as JSONObject
                    val symbol = coin.getString("symbol")
                    val id = coin.getInt("id")
                    for (money in moneySymbols) {
                        val price = parseCoinPrice(id, money)
                        jedis.hset("Currency", String.format("%s-%s", symbol, money), price.toString())
                    }
                }
                vertx.setTimer(3000) {
                    priceParsingCycle()
                }
            } catch (e: UnirestException) {
                e.printStackTrace()
            }
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