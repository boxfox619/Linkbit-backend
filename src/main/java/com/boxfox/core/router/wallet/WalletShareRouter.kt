package com.boxfox.core.router.wallet

import com.boxfox.vertx.router.*
import com.boxfox.vertx.service.*
import com.boxfox.cross.service.ShareService
import com.boxfox.cross.service.wallet.WalletService
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

import com.boxfox.cross.util.LogUtil.getLogger

class WalletShareRouter : AbstractRouter() {

    @Service
    private lateinit var shareService: ShareService
    @Service
    private lateinit var walletService: WalletService

    @RouteRegistration(uri = "/share/send", method = arrayOf(HttpMethod.GET))
    fun connectShareLink(ctx: RoutingContext, @Param(name = "data") data: String) {
        ctx.response().end(shareService.createTransactionHtml(data))
    }

    @RouteRegistration(uri = "/share/decode", method = arrayOf(HttpMethod.GET))
    fun decodeTransactionData(ctx: RoutingContext, @Param(name = "data") data: String) {
        val content = shareService.decodeTransactionData(data)
        if (content != null) {
            ctx.response().end(gson.toJson(content))
        } else {
            ctx.response().setStatusMessage("wrong transaction data").statusCode = 400
        }
        ctx.response().end()
    }

    @RouteRegistration(uri = "/share/qr", method = arrayOf(HttpMethod.GET))
    fun createQrCode(ctx: RoutingContext, @Param(name = "address") address: String, @Param(name = "amount") amount: Int) {
        getLogger().debug(String.format("Create QR Code %s %s", address, amount))
        walletService.findByAddress(address).subscribe({
            val urlPrefix = ctx.request().uri().replace(ctx.currentRoute().path, "")
            val data = shareService.createTransactionData(it.coinSymbol, address, amount.toFloat())
            val url = urlPrefix + data
            val qrFile = shareService.createQRImage(url)
            ctx.response().sendFile(qrFile.name)
            ctx.response().closeHandler { e -> qrFile.delete() }
        }, {
            ctx.fail(it)
        })

    }

    @RouteRegistration(uri = "/share/link", method = arrayOf(HttpMethod.POST))
    fun createLink(ctx: RoutingContext, @Param(name = "address") address: String, @Param(name = "amount") amount: Int) {
        walletService.findByAddress(address).subscribe({
            val urlPrefix = ctx.request().uri().replace(ctx.currentRoute().path, "")
            val data = shareService.createTransactionData(it.coinSymbol, address, amount.toFloat())
            ctx.response().end("$urlPrefix/$data")
        }, {
            ctx.fail(it)
        })
    }

}
