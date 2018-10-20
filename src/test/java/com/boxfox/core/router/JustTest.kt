package com.boxfox.core.router

import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import org.json.JSONObject
import org.junit.Test
import java.util.*

class JustTest {

    @Test
    fun priceParsingCycle() {
        try {
            val array = Unirest.get(COINMARKET_CAP_URL + "listings/").asJson().body.getObject().getJSONArray("data")
            array.forEach { obj ->
                val coin = obj as JSONObject
                val symbol = coin.getString("symbol")
                val id = coin.getInt("id")
                for (money in moneySymbols) {
                    val price = parseCoinPrice(id, money)
                }
            }
        } catch (e: UnirestException) {
            e.printStackTrace()
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