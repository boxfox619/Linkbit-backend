package com.boxfox.cross.service.price

import org.jooq.DSLContext

interface PriceUsecase {

    fun getTotalPrice(ctx:DSLContext, balances: List<Double>, moneySymbol: String): Double

    fun getWalletPrice(ctx:DSLContext, address: String, moneySymbol: String): Double

    fun getPrice(ctx: DSLContext, symbol: String, moneySymbol: String, balance: Double): Double

    fun getPrice(ctx: DSLContext, symbol: String, moneySymbol: String): Double
}