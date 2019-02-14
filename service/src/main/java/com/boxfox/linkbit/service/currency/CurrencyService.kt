package com.boxfox.linkbit.service.currency

import com.boxfox.linkbit.service.JooqReactiveService
import io.reactivex.Single

class CurrencyService(val impl: CurrencyUsecase = CurrencyServiceImpl()) : JooqReactiveService() {

    fun getCurrency(currency: String = "USD"): Single<Double> = single { impl.getCurrency(currency) }

}