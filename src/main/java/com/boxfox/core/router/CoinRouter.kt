package com.boxfox.core.router

import com.boxfox.core.router.model.CoinPriceNetworkObject
import com.boxfox.linkbit.service.LocaleService
import com.boxfox.linkbit.service.coin.CoinService
import com.boxfox.linkbit.util.LogUtil.getLogger
import com.boxfox.vertx.router.AbstractRouter
import com.boxfox.vertx.router.Param
import com.boxfox.vertx.router.RouteRegistration
import com.boxfox.vertx.service.Service
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class CoinRouter : AbstractRouter() {

    @Service
    private lateinit var localeService: LocaleService

    @Service
    private lateinit var coinService: CoinService

    @RouteRegistration(uri = "/coins", method = [HttpMethod.GET])
    fun getSupportWalletList(ctx: RoutingContext) {
        getLogger().debug(String.format("Coin Load : %s", ctx.request().remoteAddress().host()))
        this.coinService.list().subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/coins/:symbol", method = [HttpMethod.GET])
    fun getCoinPrice(ctx: RoutingContext, @Param(name = "symbol") symbol: String) {
        val locale = ctx.data()["locale"].toString()
        localeService.getMoneySymbol(locale) { res ->
            if (res.succeeded()) {
                coinService.getPrice(symbol).subscribe({ price ->
                    val unit = res.result()
                    if (price > 0) {
                        val result = CoinPriceNetworkObject().apply {
                            this.symbol = symbol
                            this.amount = price
                            this.unit = unit
                        }
                        ctx.response().end(gson.toJson(result))
                    } else {
                        ctx.response().setStatusCode(404).end()
                    }
                },{
                    ctx.fail(it)
                })
            } else {
                ctx.fail(res.cause())
            }
        }
    }

}
