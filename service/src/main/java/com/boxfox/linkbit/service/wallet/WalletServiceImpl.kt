package com.boxfox.linkbit.service.wallet

import com.boxfox.linkbit.common.RoutingException
import com.boxfox.linkbit.common.entity.wallet.WalletCreateModel
import com.boxfox.linkbit.wallet.WalletServiceRegistry
import com.google.api.client.http.HttpStatusCodes
import io.vertx.core.json.JsonObject
import org.jooq.DSLContext

class WalletServiceImpl : WalletUsecase {

    override fun createWallet(ctx: DSLContext, symbol: String, password: String): WalletCreateModel {
        val result = WalletServiceRegistry.getService(symbol).createWallet(password)
        if (result.isSuccess) {
            return WalletCreateModel().apply {
                this.address = result.address
                this.walletFileName = result.walletName
                this.walletData = result.walletData.toString()
            }
        } else {
            throw RoutingException(HttpStatusCodes.STATUS_CODE_SERVER_ERROR, "Wallet create fail")
        }
    }

    override fun importWallet(ctx: DSLContext, symbol: String, type: String, data: JsonObject): WalletCreateModel {
        val result = WalletServiceRegistry.getService(symbol).importWallet(type, data)
        if (result.isSuccess) {
            return WalletCreateModel().apply {
                this.address = result.address
                this.walletFileName = result.walletName
                this.walletData = result.walletData.toString()
            }
        } else {
            throw RoutingException(HttpStatusCodes.STATUS_CODE_SERVER_ERROR, "Wallet import fail")
        }
    }

    override fun getBalance(ctx: DSLContext, symbol: String, address: String): Double {
        val service = WalletServiceRegistry.getService(symbol)
        if (service == null) {
            throw RoutingException(HttpStatusCodes.STATUS_CODE_SERVER_ERROR)
        } else {
            return service.getBalance(address)
        }
    }
}
