package com.boxfox.cross.service.withdraw

import com.boxfox.linkbit.wallet.model.TransactionResult
import org.jooq.DSLContext

interface WithdrawUsecase {
    fun withdraw(ctx: DSLContext, symbol: String, walletFileName: String, walletJsonFile: String, password: String, targetAddress:String, amount: String): TransactionResult
}