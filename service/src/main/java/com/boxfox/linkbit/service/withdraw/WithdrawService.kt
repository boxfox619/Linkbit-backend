package com.boxfox.linkbit.service.withdraw

import com.boxfox.linkbit.wallet.model.TransactionResult
import io.reactivex.Single
import io.vertx.core.json.JsonObject

class WithdrawService(private val impl: WithdrawServiceImpl = WithdrawServiceImpl()) : com.boxfox.linkbit.service.JooqReactiveService() {
    fun withdraw(symbol: String, walletData: JsonObject, targetAddress: String, amount: String): Single<TransactionResult>
    = single { impl.withdraw(it, symbol, walletData, targetAddress, amount)}
}