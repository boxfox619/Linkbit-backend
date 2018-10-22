package com.boxfox.core.router

import com.boxfox.core.router.model.CoinPriceNetworkObject
import com.boxfox.cross.service.LocaleService
import com.boxfox.cross.service.price.PriceService
import com.boxfox.cross.service.coin.CoinService
import com.boxfox.cross.util.LogUtil.getLogger
import com.boxfox.vertx.router.AbstractRouter
import com.boxfox.vertx.router.Param
import com.boxfox.vertx.router.RouteRegistration
import com.boxfox.vertx.service.Service
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class CoinRouter : AbstractRouter() {

    @Service
    private lateinit var priceService: PriceService

    @Service
    private lateinit var localeService: LocaleService

    @Service
    private lateinit var coinService: CoinService

    @RouteRegistration(uri = "/coin/supported/list", method = arrayOf(HttpMethod.GET))
    fun getSupportWalletList(ctx: RoutingContext) {
        getLogger().debug(String.format("Supported Coin Load : %s", ctx.request().remoteAddress().host()))
        this.coinService.allCoins.subscribe({ list ->
            ctx.response().end(gson.toJson(list))
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/coin/price", method = arrayOf(HttpMethod.GET))
    fun getCoinPrice(ctx: RoutingContext, @Param(name = "symbol") symbol: String) {
        val locale = ctx.data()["locale"].toString()
        localeService.getLocaleMoneySymbol(locale) { res ->
            if (res.succeeded()) {
                priceService.getPrice(symbol).subscribe({ price ->
                    val unit = res.result()
                    if (price > 0) {
                        val result = CoinPriceNetworkObject()
                        result.amount = price
                        result.unit = unit
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
