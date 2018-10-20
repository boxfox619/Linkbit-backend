package com.boxfox.cross.service.price

import com.linkbit.android.entity.WalletModel
import io.reactivex.Single
import io.vertx.core.Vertx
import io.vertx.redis.RedisClient
import io.vertx.redis.RedisOptions
import org.jooq.DSLContext

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

    override fun getPrice(symbol: String, moneySymbol: String): Single<Double> {
        val config = RedisOptions().setHost("127.0.0.1")
        val redis = RedisClient.create(Vertx.vertx(), config)
        var symbol = symbol
        symbol = symbol.toUpperCase()
        return Single.create { subscriber ->
            redis.hget("currency", String.format("%s-%s", symbol, moneySymbol)) {
                if (it.succeeded()) {
                    subscriber.onSuccess(it.result().toDouble())
                } else {
                    subscriber.onError(it.cause())
                }
            }
        }
    }

}