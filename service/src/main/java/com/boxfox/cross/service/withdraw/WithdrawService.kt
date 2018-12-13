package com.boxfox.cross.service.withdraw

import com.boxfox.cross.service.JooqReactiveService
import com.boxfox.linkbit.wallet.model.TransactionResult
import io.reactivex.Single

class WithdrawService(private val impl: WithdrawServiceImpl = WithdrawServiceImpl()) : JooqReactiveService() {
    fun withdraw(symbol: String, walletFileName: String, walletJsonFile: String, password: String, targetAddress: String, amount: String): Single<TransactionResult>
    = single { impl.withdraw(it, symbol, walletFileName, walletJsonFile, password, targetAddress, amount)}
}