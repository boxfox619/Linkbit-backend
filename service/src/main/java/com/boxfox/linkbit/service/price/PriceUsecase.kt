package com.boxfox.linkbit.service.price

import com.boxfox.linkbit.common.entity.wallet.WalletModel
import org.jooq.DSLContext

interface PriceUsecase {

    fun getTotalPrice(ctx:DSLContext, wallets: List<WalletModel>, moneySymbol: String): Double

    fun getPrice(ctx: DSLContext, symbol: String, moneySymbol: String, balance: Double): Double

    fun getPrice(symbol: String, moneySymbol: String): Double
}