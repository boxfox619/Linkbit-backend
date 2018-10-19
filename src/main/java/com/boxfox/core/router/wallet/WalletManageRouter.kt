package com.boxfox.core.router.wallet

import com.boxfox.cross.service.wallet.WalletService
import com.boxfox.cross.util.LogUtil.getLogger
import com.boxfox.vertx.router.AbstractRouter
import com.boxfox.vertx.router.Param
import com.boxfox.vertx.router.RouteRegistration
import com.boxfox.vertx.service.Service
import com.google.api.client.http.HttpStatusCodes
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class WalletManageRouter : AbstractRouter() {

    @Service
    protected lateinit var walletService: WalletService

    //@TODO Anonymous wallet create function
    @RouteRegistration(uri = "/wallet/new", method = arrayOf(HttpMethod.POST), auth = true)
    fun create(ctx: RoutingContext,
               @Param(name = "address") symbol: String,
               @Param(name = "name") name: String,
               @Param(name = "password") password: String,
               @Param(name = "description") description: String,
               @Param(name = "major") major: Boolean,
               @Param(name = "open") open: Boolean) {
        val uid = ctx.data()["uid"] as String
        getLogger().debug("Wallet create test$uid")
        walletService.createWallet(uid, symbol, password, name, description, open, major).subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/wallet/add", method = arrayOf(HttpMethod.POST), auth = true)
    fun add(ctx: RoutingContext,
            @Param(name = "address") address: String,
            @Param(name = "symbol") symbol: String,
            @Param(name = "name") name: String,
            @Param(name = "description") description: String,
            @Param(name = "major") major: Boolean,
            @Param(name = "open") open: Boolean) {
        val uid = ctx.data()["uid"] as String
        walletService.createWallet(uid, symbol, name, address, description, open, major).subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/wallet", method = arrayOf(HttpMethod.PUT), auth = true)
    fun updateWallet(ctx: RoutingContext,
                     @Param(name = "address") address: String,
                     @Param(name = "name") name: String,
                     @Param(name = "description") description: String,
                     @Param(name = "major") major: Boolean,
                     @Param(name = "open") open: Boolean) {
        val uid = ctx.data()["uid"] as String
        walletService.checkOwner(uid, address).andThen(walletService.updateWallet(uid, address, name, description, major, open)).subscribe({
            ctx.response().setStatusCode(HttpStatusCodes.STATUS_CODE_OK).end()
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/wallet", method = arrayOf(HttpMethod.DELETE))
    fun deleteWallet(ctx: RoutingContext, @Param(name = "address") address: String) {
        val uid = ctx.data()["uid"] as String
        walletService.checkOwner(uid, address).andThen(walletService.deleteWallet(uid, address)).subscribe({
            ctx.response().setStatusCode(HttpStatusCodes.STATUS_CODE_OK).end()
        }, {
            ctx.fail(it)
        })
    }

}
