package com.boxfox.core.router

import com.boxfox.linkbit.service.transaction.TransactionService
import com.boxfox.vertx.router.AbstractRouter
import com.boxfox.vertx.router.Param
import com.boxfox.vertx.router.RouteRegistration
import com.boxfox.vertx.service.Service
import io.vertx.core.http.HttpMethod
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
        val transaction = transactionService.getTransaction(symbol, txHash)
        ctx.response().setChunked(true).write(gson.toJson(transaction)).end()
    }
}
