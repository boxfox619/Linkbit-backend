package com.boxfox.core.router

import com.boxfox.linkbit.service.wallet.WalletService
import com.boxfox.linkbit.util.LogUtil.getLogger
import com.boxfox.vertx.router.AbstractRouter
import com.boxfox.vertx.router.Param
import com.boxfox.vertx.router.RouteRegistration
import com.boxfox.vertx.service.Service
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class WalletRouter : AbstractRouter() {

    @Service
    lateinit var walletService: WalletService

    @RouteRegistration(uri = "/wallet/new", method = [HttpMethod.POST], auth = true)
    fun create(ctx: RoutingContext,
               @Param(name = "password") password: String,
               @Param(name = "symbol") symbol: String) {
        val uid = ctx.data()["uid"] as String
        getLogger().debug("Wallet create test$uid")
        walletService.createWallet(uid, symbol, password).subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/wallet/balance", method = [HttpMethod.GET])
    fun getBalance(ctx: RoutingContext,
                   @Param(name = "symbol") symbol: String,
                   @Param(name = "address") address: String) {
        walletService.getBalance(symbol, address).subscribe({
            ctx.response().end(it.toString())
        }, {
            ctx.fail(it)
        })
    }

}
