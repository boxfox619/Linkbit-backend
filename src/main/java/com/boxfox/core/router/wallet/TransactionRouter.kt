package com.boxfox.core.router.wallet

import com.boxfox.cross.common.data.PostgresConfig
import com.boxfox.cross.service.wallet.WalletDatabaseService
import com.boxfox.linkbit.wallet.WalletService
import com.boxfox.linkbit.wallet.WalletServiceManager
import com.boxfox.vertx.router.*
import com.boxfox.vertx.service.*
import com.linkbit.android.entity.TransactionModel
import io.one.sys.db.tables.daos.WalletDao
import io.one.sys.db.tables.pojos.Wallet
import io.vertx.core.Future
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

import java.util.ArrayList
import java.util.Arrays

class TransactionRouter : AbstractRouter() {

    @Service
    private lateinit var walletDatabaseService: WalletDatabaseService

    @RouteRegistration(uri = "/transaction", method = arrayOf(HttpMethod.GET))
    fun lookupTransaction(ctx: RoutingContext,
                          @Param(name = "symbol") symbol: String,
                          @Param(name = "txHash") txHash: String) {
        val service = WalletServiceManager.getService(symbol)
        val transaction = service.getTransaction(txHash)
        ctx.response().setChunked(true).write(gson.toJson(transaction)).end()
    }

    @RouteRegistration(uri = "/transaction/count", method = arrayOf(HttpMethod.GET))
    fun wallTransactionCount(ctx: RoutingContext, @Param(name = "address") address: String) {
        walletDatabaseService.findByAddress(address).subscribe({
            val service = WalletServiceManager.getService(it.coinSymbol)
            val count = service.getTransactionCount(address)
            ctx.response().end(count.toString() + "")
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/transaction/list", method = arrayOf(HttpMethod.GET))
    fun walletTransactionList(ctx: RoutingContext,
                              @Param(name = "address") address: String,
                              @Param(name = "page") page: Int,
                              @Param(name = "count") count: Int) {
        //@TODO transaction list pagenation
        walletDatabaseService.findByAddress(address).subscribe({
            val service = WalletServiceManager.getService(it.coinSymbol)
            service.getTransactionList(address).setHandler { transactionStatusListResult ->
                val transactionStatusList = transactionStatusListResult.result()
                if (transactionStatusList.size == 0) {
                    service.indexingTransactions(address)
                }
                ctx.response().setChunked(true).write(gson.toJson(transactionStatusList)).end()
            }
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/transaction/all/count", method = arrayOf(HttpMethod.GET), auth = true)
    fun allTransactionCount(ctx: RoutingContext) {
        val uid = ctx.data()["uid"] as String
        doAsync<Any>({ future ->
            val dao = WalletDao(PostgresConfig.create(), vertx)
            dao.findManyByUid(Arrays.asList(uid)).result().forEach { w ->
                var count = 0
                val service = WalletServiceManager.getService(w.symbol)
                val accountAddress = w.address
                count += service.getTransactionCount(accountAddress)
                future.complete(count)
            }
            if (!future.isComplete)
                future.fail("Transaction Not Found")
        }, { e ->
            if (e.succeeded()) {
                ctx.response().setChunked(true).write(gson.toJson(e.result())).end()
            } else {
                ctx.response().setStatusCode(400).end()
            }
        })
    }

    @RouteRegistration(uri = "/transaction/all/list", method = arrayOf(HttpMethod.GET), auth = true)
    fun allTransactionList(ctx: RoutingContext, @Param(name = "page") page: Int, @Param(name = "count") count: Int) {
        val uid = ctx.data()["uid"] as String
        doAsync<Any>({ future ->
            val dao = WalletDao(PostgresConfig.create(), vertx)
            val totalTxStatusList = ArrayList<TransactionModel>()
            val tasks = ArrayList<Future<List<TransactionModel>>>()
            for (wallet in dao.findManyByUid(Arrays.asList(uid)).result()) {
                val service = WalletServiceManager.getService(wallet.symbol)
                val accountAddress = wallet.address
                tasks.add(service.getTransactionList(accountAddress).setHandler { txStatusListResult ->
                    val txStatusList = txStatusListResult.result()
                    if (txStatusList.size == 0) {
                        service.indexingTransactions(accountAddress)
                    }
                    totalTxStatusList.addAll(txStatusList)
                })
                if (totalTxStatusList.size >= page * count) {
                    break
                }
            }
            var check: Boolean
            do {
                check = true
                for (task in tasks) {
                    if (!task.isComplete()) {
                        check = false
                        break
                    }
                }
            } while (!check)
            future.complete(totalTxStatusList)
        }, { e ->
            if (e.succeeded()) {
                ctx.response().setChunked(true).write(gson.toJson(e.result())).end()
            } else {
                ctx.response().setStatusCode(400).end()
            }
        })
    }

}
