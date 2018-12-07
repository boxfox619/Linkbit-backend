package com.boxfox.cross.service.price

import com.boxfox.cross.common.RoutingException
import com.boxfox.cross.common.data.RedisConfig
import com.boxfox.cross.common.entity.wallet.WalletModel
import com.google.api.client.http.HttpStatusCodes
import org.jooq.DSLContext

class PriceServiceImpl : PriceUsecase {

    override fun getTotalPrice(ctx: DSLContext, wallets: List<WalletModel>, moneySymbol: String): Double {
        var totalPrice = 0.0
        for (wallet in wallets) {
            totalPrice += wallet.balance * this.getPrice(wallet.coinSymbol, moneySymbol)
        }
        return totalPrice
    }

    override fun getPrice(ctx: DSLContext, symbol: String, moneySymbol: String, balance: Double): Double {
        return this.getPrice(ctx, symbol, moneySymbol, balance) * balance
    }

    override fun getPrice(symbol: String, moneySymbol: String): Double {
        RedisConfig.createPool().resource.use { jedis ->
            val value = jedis.hget("currency", String.format("%s-%s", symbol.toUpperCase(), moneySymbol))
            if (value!=null) {
                return value.toDouble()
            } else {
                throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "coin currency not found")
            }
        }
    }

}