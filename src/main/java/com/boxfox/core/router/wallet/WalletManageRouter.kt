package com.boxfox.core.router.wallet

import com.boxfox.core.router.model.WalletCreateNetworkObject
import com.boxfox.cross.service.wallet.WalletDatabaseService
import com.boxfox.linkbit.wallet.WalletService
import com.boxfox.linkbit.wallet.WalletServiceManager
import com.boxfox.linkbit.wallet.model.WalletCreateResult
import com.boxfox.vertx.router.AbstractRouter
import com.boxfox.vertx.router.Param
import com.boxfox.vertx.router.RouteRegistration
import com.boxfox.vertx.service.Service
import com.google.api.client.http.HttpStatusCodes
import com.linkbit.android.entity.WalletModel
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

import com.boxfox.cross.util.LogUtil.getLogger
import io.reactivex.Completable
import java.util.*

class WalletManageRouter : AbstractRouter() {

    @Service
    protected lateinit var walletDatabaseService: WalletDatabaseService

    //@TODO Anonymous wallet create function
    @RouteRegistration(uri = "/wallet/new", method = arrayOf(HttpMethod.POST), auth = true)
    fun create(ctx: RoutingContext,
               @Param(name = "address") symbol: String,
               @Param(name = "name") name: String,
               @Param(name = "password") password: String?,
               @Param(name = "description") description: String,
               @Param(name = "major") major: Boolean,
               @Param(name = "open") open: Boolean) {
        val uid = ctx.data()["uid"] as String
        getLogger().debug("Wallet create test$uid")
        doAsync<Any>({ future ->
            if (password != null) {
                val service = WalletServiceManager.getService(symbol)
                val result = service.createWallet(password)
                if (result.isSuccess) {
                    val response = WalletCreateNetworkObject()
                    response.walletData = result.walletData.toString()
                    response.walletFileName = result.walletName
                    response.accountAddress = result.address
                    walletDatabaseService!!.createWallet(uid, symbol, name, result.address, description, open, major).subscribe({
                        val walletModel = it
                        response.ownerName = walletModel.ownerName
                        response.linkbitAddress = walletModel.linkbitAddress
                        response.walletName = walletModel.walletName
                        response.description = walletModel.description
                        response.coinSymbol = walletModel.coinSymbol
                        response.balance = walletModel.balance
                        response.ownerId = walletModel.ownerId
                        future.complete(response)
                    }, {
                        future.fail(it)
                    })
                } else {
                    future.fail("Failure generate wallet data")
                }
            } else {
                future.fail("Illegal Argument")
            }
        }, { res ->
            if (res.succeeded()) {
                ctx.response().end(gson.toJson(res.result()))
            } else {
                ctx.response()
                        .setStatusMessage(res.cause().message)
                        .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                        .end()
            }
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
        walletDatabaseService.createWallet(uid, symbol, name, address, description, open, major).subscribe({
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
        walletDatabaseService.checkOwner(uid, address).andThen(walletDatabaseService.updateWallet(uid, address, name, description, major, open)).subscribe({
            ctx.response().setStatusCode(HttpStatusCodes.STATUS_CODE_OK).end()
        }, {
            ctx.fail(it)
        })
    }

    @RouteRegistration(uri = "/wallet", method = arrayOf(HttpMethod.DELETE))
    fun deleteWallet(ctx: RoutingContext, @Param(name = "address") address: String) {
        val uid = ctx.data()["uid"] as String
        walletDatabaseService.checkOwner(uid, address).andThen(walletDatabaseService.deleteWallet(uid, address)).subscribe({
            ctx.response().setStatusCode(HttpStatusCodes.STATUS_CODE_OK).end()
        }, {
            ctx.fail(it)
        })
    }

}
