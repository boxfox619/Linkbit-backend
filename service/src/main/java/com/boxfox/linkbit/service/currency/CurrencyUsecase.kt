package com.boxfox.linkbit.service.currency

interface CurrencyUsecase {
    fun getCurrency(currency: String = "USD"): Double
}