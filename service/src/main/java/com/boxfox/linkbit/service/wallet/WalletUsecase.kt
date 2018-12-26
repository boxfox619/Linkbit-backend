package com.boxfox.linkbit.service.wallet

import com.boxfox.linkbit.common.entity.wallet.WalletCreateModel
import org.jooq.DSLContext

interface WalletUsecase {
    fun createWallet(ctx: DSLContext, symbol: String, password: String): WalletCreateModel
    fun getBalance(ctx: DSLContext, symbol: String, address: String): Double
}