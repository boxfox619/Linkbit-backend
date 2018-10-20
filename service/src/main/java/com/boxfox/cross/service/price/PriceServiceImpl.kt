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
        //@TODO Implement price getting from redis
        throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "coin price not found")
    }

}