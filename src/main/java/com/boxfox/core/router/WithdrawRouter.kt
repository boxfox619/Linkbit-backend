package com.boxfox.core.router

import com.boxfox.vertx.router.*
import com.boxfox.vertx.service.*
import com.boxfox.linkbit.service.wallet.WalletService
import com.boxfox.linkbit.service.withdraw.WithdrawService
import com.boxfox.linkbit.wallet.WalletServiceRegistry
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class WithdrawRouter : AbstractRouter() {

    @Service
    private lateinit var withdrawService: WithdrawService

    @RouteRegistration(uri = "/withdraw", method = arrayOf(HttpMethod.POST))
    fun send(ctx: RoutingContext,
             @Param(name = "symbol") symbol: String,
             @Param(name = "walletName") walletName: String,
             @Param(name = "walletData") walletData: String,
             @Param(name = "password") password: String,
             @Param(name = "targetAddress") targetAddress: String,
             @Param(name = "amount") amount: String) {
        withdrawService.withdraw(symbol, walletName, walletData, password, targetAddress, amount).subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }
}
