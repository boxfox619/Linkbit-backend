package com.boxfox.linkbit.service.withdraw

import com.boxfox.linkbit.wallet.model.TransactionResult
import io.reactivex.Single

class WithdrawService(private val impl: WithdrawServiceImpl = WithdrawServiceImpl()) : com.boxfox.linkbit.service.JooqReactiveService() {
    fun withdraw(symbol: String, walletFileName: String, walletJsonFile: String, password: String, targetAddress: String, amount: String): Single<TransactionResult>
    = single { impl.withdraw(it, symbol, walletFileName, walletJsonFile, password, targetAddress, amount)}
}