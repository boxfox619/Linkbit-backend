package com.boxfox.core.router.wallet

import com.boxfox.linkbit.service.coin.CoinService
import com.boxfox.linkbit.service.wallet.WalletService
import com.boxfox.vertx.router.AbstractRouter
import com.boxfox.vertx.router.Param
import com.boxfox.vertx.router.RouteRegistration
import com.boxfox.vertx.service.Service
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import org.apache.log4j.Logger

class WalletLookupRouter : AbstractRouter() {

    @Service
    private lateinit var walletService: WalletService

    @RouteRegistration(uri = "/wallet/list", method = arrayOf(HttpMethod.GET), auth = true)
    fun getWallets(ctx: RoutingContext) {
        val uid = ctx.data()["uid"] as String
        Logger.getRootLogger().info("Wallet list lookup : $uid")
        walletService.getWalletList(uid).subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/wallet/balance", method = arrayOf(HttpMethod.GET))
    fun getBalance(ctx: RoutingContext, @Param(name = "address") address: String) {
        walletService.getBalance(address).subscribe({
            ctx.response().end(it.toString())
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/wallet", method = arrayOf(HttpMethod.GET), auth = true)
    fun walletInfoLookup(ctx: RoutingContext, @Param(name = "address") address: String) {
        walletService.findByAddress(address).subscribe({
            ctx.response().setChunked(true).write(gson.toJson(it)).end()
        }, {
            ctx.fail(it)
        })
    }
}
