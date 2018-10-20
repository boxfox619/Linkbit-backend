package com.boxfox.cross.service.price

import com.boxfox.cross.common.RoutingException
import com.google.api.client.http.HttpStatusCodes
import com.linkbit.android.entity.WalletModel
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import org.apache.log4j.Logger
import org.jooq.DSLContext
import org.json.JSONObject
import java.util.*

class PriceServiceImpl : PriceUsecase {
    private val coinIdMap: MutableMap<String, Int>

    init {
        this.coinIdMap = HashMap()
        try {
            val array = Unirest.get(COINMARKET_CAP_URL + "listings/").asJson().body.getObject().getJSONArray("data")
            array.forEach { obj ->
                val coin = obj as JSONObject
                val symbol = coin.getString("symbol")
                val id = coin.getInt("id")
                coinIdMap[symbol] = id
            }
        } catch (e: UnirestException) {
            e.printStackTrace()
        }
    }
    //@TODO coin price to redis

    override fun getTotalPrice(ctx: DSLContext, wallets: List<WalletModel>, moneySymbol: String): Double {
        var totalPrice = 0.0
        for (wallet in wallets) {
            totalPrice += wallet.balance + this.getPrice(ctx, wallet.coinSymbol, moneySymbol)
        }
        return totalPrice
    }

    override fun getPrice(ctx: DSLContext, symbol: String, locale: String, balance: Double): Double {
        return this.getPrice(ctx, symbol, locale, balance) * balance
    }

    override fun getPrice(ctx: DSLContext, symbol: String, moneySymbol: String): Double {
        var symbol = symbol
        symbol = symbol.toUpperCase()
        if (coinIdMap.containsKey(symbol)) {
            try {
                val id = coinIdMap[symbol]
                val url = String.format("%s%s/?convert=%s", COINMARKET_CAP_URL + "ticker/", id, moneySymbol)
                val obj = Unirest
                        .get(url).asJson()
                        .body.getObject()
                val krw = obj.getJSONObject("data").getJSONObject("quotes").getJSONObject(moneySymbol)
                return krw.getDouble("price")
            } catch (e: UnirestException) {
                Logger.getRootLogger().error(e)
                throw RoutingException(HttpStatusCodes.STATUS_CODE_SERVER_ERROR)
            }
        }
        throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "coin price not found")
    }

    companion object {
        private val COINMARKET_CAP_URL = "https://api.coinmarketcap.com/v2/"
    }

}