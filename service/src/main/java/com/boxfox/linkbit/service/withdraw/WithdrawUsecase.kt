package com.boxfox.linkbit.service.withdraw

import com.boxfox.linkbit.wallet.model.TransactionResult
import io.vertx.core.json.JsonObject
import org.jooq.DSLContext

interface WithdrawUsecase {
    fun withdraw(ctx: DSLContext, symbol: String, walletData: JsonObject, targetAddress:String, amount: String): TransactionResult
}