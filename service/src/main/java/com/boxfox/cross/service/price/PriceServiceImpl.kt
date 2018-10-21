package com.boxfox.cross.service.price

import com.boxfox.cross.common.RoutingException
import com.boxfox.cross.common.data.RedisConfig
import com.google.api.client.http.HttpStatusCodes
import com.linkbit.android.entity.WalletModel
import org.jooq.DSLContext

class PriceServiceImpl : PriceUsecase {

    override fun getTotalPrice(ctx: DSLContext, wallets: List<WalletModel>, moneySymbol: String): Double {
        var totalPrice = 0.0
        for (wallet in wallets) {
            totalPrice += wallet.balance + this.getPrice(wallet.coinSymbol, moneySymbol)
        }
        return totalPrice
    }

    override fun getPrice(ctx: DSLContext, symbol: String, locale: String, balance: Double): Double {
        return this.getPrice(ctx, symbol, locale, balance) * balance
    }

    override fun getPrice(symbol: String, moneySymbol: String): Double {
        RedisConfig.createPool().resource.use { jedis ->
            var symbol = symbol
            symbol = symbol.toUpperCase()
            val value = jedis.hget("currency", String.format("%s-%s", symbol, moneySymbol))
            if (value!=null) {
                return value.toDouble()
            } else {
                throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "coin currency not found")
            }
        }
    }

}