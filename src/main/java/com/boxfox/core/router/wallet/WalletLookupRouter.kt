package com.boxfox.core.router.wallet

import com.boxfox.cross.service.price.PriceService
import com.boxfox.cross.service.wallet.WalletService
import com.boxfox.linkbit.wallet.WalletServiceRegistry
import com.boxfox.vertx.router.AbstractRouter
import com.boxfox.vertx.router.Param
import com.boxfox.vertx.router.RouteRegistration
import com.boxfox.vertx.service.Service
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import org.apache.log4j.Logger

class WalletLookupRouter : AbstractRouter() {

    @Service
    private lateinit var walletService: WalletService
    @Service
    private lateinit var priceService: PriceService

    @RouteRegistration(uri = "/wallet/list", method = arrayOf(HttpMethod.GET), auth = true)
    fun getWallets(ctx: RoutingContext) {
        val locale = ctx.data()["locale"].toString()
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
        walletService.findByAddress(address).subscribe({
            val service = WalletServiceRegistry.getService(it.coinSymbol)
            val balance = service.getBalance(it.accountAddress)
            if (balance < 0) {
                ctx.response().statusCode = HttpResponseStatus.NOT_FOUND.code()
            } else {
                ctx.response().setStatusCode(200).setChunked(true).write(balance.toString())
            }
            ctx.response().end()
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/wallet/price", method = arrayOf(HttpMethod.GET))
    fun getPrice(ctx: RoutingContext, @Param(name = "address") address: String) {
        val locale = ctx.data()["locale"].toString()
        priceService.getWalletPrice(address, locale).subscribe({
            ctx.response().end(it.toString())
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/wallet/balance/all", method = arrayOf(HttpMethod.GET), auth = true)
    fun getTotalBalance(ctx: RoutingContext, @Param(name = "symbol") symbol: String) {
        val uid = ctx.data()["uid"] as String
        walletService.getTotalBalance(uid, symbol).subscribe({
            ctx.response().end(it.toString())
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/wallet/price/all", method = arrayOf(HttpMethod.GET), auth = true)
    fun getTotalPrice(ctx: RoutingContext, @Param(name = "symbol") symbol: String) {
        val uid = ctx.data()["uid"] as String
        val locale = ctx.data()["locale"].toString()
        priceService.getTotalPrice(uid, locale).subscribe({
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
