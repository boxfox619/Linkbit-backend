package com.boxfox.core.router.wallet

import com.boxfox.cross.service.transaction.TransactionService
import com.boxfox.cross.service.wallet.WalletService
import com.boxfox.linkbit.wallet.WalletServiceRegistry
import com.boxfox.vertx.router.*
import com.boxfox.vertx.service.*
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class TransactionRouter : AbstractRouter() {

    @Service
    private lateinit var transactionService: TransactionService

    @RouteRegistration(uri = "/transaction", method = arrayOf(HttpMethod.GET))
    fun lookupTransaction(ctx: RoutingContext,
                          @Param(name = "symbol") symbol: String,
                          @Param(name = "txHash") txHash: String) {
        val service = WalletServiceRegistry.getService(symbol)
        val transaction = service.getTransaction(txHash)
        ctx.response().setChunked(true).write(gson.toJson(transaction)).end()
    }

    @RouteRegistration(uri = "/transaction/count", method = arrayOf(HttpMethod.GET))
    fun wallTransactionCount(ctx: RoutingContext, @Param(name = "address") address: String) {
        transactionService.getTransactionCount(address).subscribe({
            ctx.response().end(gson.toJson(it))
        },{
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/transaction/list", method = arrayOf(HttpMethod.GET))
    fun walletTransactionList(ctx: RoutingContext,
                              @Param(name = "address") address: String,
                              @Param(name = "page") page: Int,
                              @Param(name = "count") count: Int) {
        //@TODO transaction list pagenation
        transactionService.getTransactionList(address, page, count).subscribe({
            ctx.response().end(gson.toJson(it))
        },{
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/transaction/all/count", method = arrayOf(HttpMethod.GET), auth = true)
    fun allTransactionCount(ctx: RoutingContext, @Param(name = "symbol") symbol: String) {
        val uid = ctx.data()["uid"] as String
        transactionService.getAllTransactionCount(uid, symbol).subscribe({
            ctx.response().end(gson.toJson(it))
        },{
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/transaction/all/list", method = arrayOf(HttpMethod.GET), auth = true)
    fun allTransactionList(ctx: RoutingContext, @Param(name = "symbol") symbol: String, @Param(name = "page") page: Int, @Param(name = "count") count: Int) {
        val uid = ctx.data()["uid"] as String
        transactionService.getAllTransactionList(uid,symbol,  page, count).subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }

}
