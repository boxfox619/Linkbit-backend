package com.boxfox.core.router

import com.boxfox.cross.service.transaction.TransactionService
import com.boxfox.linkbit.wallet.WalletServiceRegistry
import com.boxfox.vertx.router.AbstractRouter
import com.boxfox.vertx.router.Param
import com.boxfox.vertx.router.RouteRegistration
import com.boxfox.vertx.service.Service
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class TransactionRouter : AbstractRouter() {

    @Service
    private lateinit var transactionService: TransactionService

    @RouteRegistration(uri = "/transaction", method = arrayOf(HttpMethod.GET))
    fun lookupTransaction(ctx: RoutingContext,
                          @Param(name = "symbol") symbol: String,
                          @Param(name = "txHash") txHash: String) {
        val transaction = transactionService.getTransaction(symbol, txHash)
        ctx.response().setChunked(true).write(gson.toJson(transaction)).end()
    }

    @RouteRegistration(uri = "/transaction/count", method = arrayOf(HttpMethod.GET))
    fun transactionCount(ctx: RoutingContext, @Param(name = "address") address: String) {
        transactionService.getTransactionCount(address).subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/transaction/list", method = arrayOf(HttpMethod.GET))
    fun transactionList(ctx: RoutingContext,
                              @Param(name = "address") address: String,
                              @Param(name = "page") page: Int,
                              @Param(name = "count") count: Int) {
        transactionService.getTransactionList(address, page, count).subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/transaction/:symbol/count", method = arrayOf(HttpMethod.GET), auth = true)
    fun allTransactionCount(ctx: RoutingContext, @Param(name = "symbol") symbol: String) {
        val uid = ctx.data()["uid"] as String
        transactionService.getAllTransactionCount(uid, symbol).subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/transaction/:symbol/list", method = arrayOf(HttpMethod.GET), auth = true)
    fun allTransactionList(ctx: RoutingContext, @Param(name = "symbol") symbol: String, @Param(name = "page") page: Int, @Param(name = "count") count: Int) {
        val uid = ctx.data()["uid"] as String
        transactionService.getAllTransactionList(uid, symbol, page, count).subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }


    @RouteRegistration(uri = "/transaction/all/count", method = arrayOf(HttpMethod.GET), auth = true)
    fun allTransactionCount(ctx: RoutingContext) {
        val uid = ctx.data()["uid"] as String
        transactionService.getAllTransactionCount(uid).subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/transaction/all/list", method = arrayOf(HttpMethod.GET), auth = true)
    fun allTransactionList(ctx: RoutingContext, @Param(name = "page") page: Int, @Param(name = "count") count: Int) {
        val uid = ctx.data()["uid"] as String
        transactionService.getAllTransactionList(uid, page, count).subscribe({
            ctx.response().end(gson.toJson(it))
        }, {
            ctx.fail(it)
        })
    }
}
