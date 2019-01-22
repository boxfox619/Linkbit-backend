package com.boxfox.linkbit.service.wallet

import com.boxfox.linkbit.common.entity.wallet.WalletCreateModel
import io.vertx.core.json.JsonObject
import org.jooq.DSLContext

interface WalletUsecase {
    fun createWallet(ctx: DSLContext, symbol: String, password: String): WalletCreateModel
    fun importWallet(ctx: DSLContext, symbol: String, type: String, data: JsonObject): WalletCreateModel
    fun getBalance(ctx: DSLContext, symbol: String, address: String): Double
}