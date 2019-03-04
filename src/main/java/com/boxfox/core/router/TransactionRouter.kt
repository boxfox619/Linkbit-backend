package com.boxfox.core.router

import com.boxfox.linkbit.service.transaction.TransactionService
import com.boxfox.vertx.router.AbstractRouter
import com.boxfox.vertx.router.Param
import com.boxfox.vertx.router.RouteRegistration
import com.boxfox.vertx.service.Service
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext

class TransactionRouter : AbstractRouter() {

    @Service
    private lateinit var transactionService: TransactionService

    @RouteRegistration(uri = "/wallet/:symbol/transactions", method = [HttpMethod.GET])
    fun transactionList(ctx: RoutingContext,
                        @Param(name = "symbol") symbol: String,
                        @Param(name = "address") address: String,
                        @Param(name = "page") page: Int,
                        @Param(name = "count") count: Int) {
        transactionService.getTransactionList(symbol, address, page, count).subscribe({
            ctx.response().end(gson.toJson(it)) //{transactions: [], blackNum: 111}
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/transaction/:symbol", method = [HttpMethod.GET])
    fun lookupTransaction(ctx: RoutingContext,
                          @Param(name = "symbol") symbol: String,
                          @Param(name = "txHash") txHash: String) {
        transactionService.getTransaction(symbol, txHash).subscribe({
            ctx.response().end(gson.toJson(it))
        },{ ctx.fail(it)})
    }

    @RouteRegistration(uri = "/transaction/:symbol", method = [HttpMethod.POST])
    fun lookupTransactions(ctx: RoutingContext,
                           @Param(name = "symbol") symbol: String,
                           @Param(name = "transactions") transactions: JsonArray) {
        val hashList = transactions.map { it -> it.toString() }.toList()
        transactionService.getTransactions(symbol, hashList).subscribe({
            ctx.response().end(gson.toJson(it))
        }, { ctx.fail(it) })
    }

    @RouteRegistration(uri = "/transactions/:symbol", method = [HttpMethod.GET])
    fun lookupTransactions(ctx: RoutingContext,
                           @Param(name = "symbol") symbol: String,
                           @Param(name = "address") address: String,
                           @Param(name = "lastBlock") lastBlock: Int) {
        transactionService.getTransactions(symbol, address, lastBlock).subscribe({
            ctx.response().end(gson.toJson(it))
        }, { ctx.fail(it) })
    }
}
