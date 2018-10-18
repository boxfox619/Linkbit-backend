package com.boxfox.core.router.wallet

import com.boxfox.vertx.router.*
import com.boxfox.vertx.service.*
import com.boxfox.cross.service.wallet.WalletDatabaseService
import com.boxfox.linkbit.wallet.WalletServiceRegistry
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class RemittanceRouter : AbstractRouter() {

    @Service
    private lateinit var walletDatabaseService: WalletDatabaseService

    @RouteRegistration(uri = "/remittance", method = arrayOf(HttpMethod.POST))
    fun send(ctx: RoutingContext,
             @Param(name = "symbol") symbol: String,
             @Param(name = "walletName") walletName: String,
             @Param(name = "walletData") walletData: String,
             @Param(name = "password") password: String,
             @Param(name = "targetAddress") targetAddress: String,
             @Param(name = "amount") amount: String) {
        val service = WalletServiceRegistry.getService(symbol)
        walletDatabaseService.findByAddress(targetAddress).subscribe({
            var destAddress = it.accountAddress
            val result = service.send(walletName, walletData, password, destAddress, amount)
            ctx.response().putHeader("content-type", "application/json")
            ctx.response().setChunked(true).write(gson.toJson(result))
            ctx.response().end()
        }, {
            ctx.fail(it)
        })
    }
}
