package com.boxfox.linkbit.service.coin

import com.boxfox.linkbit.common.entity.CoinModel
import com.boxfox.linkbit.service.JooqReactiveService
import com.boxfox.linkbit.service.LocaleService
import com.boxfox.vertx.service.Service
import io.reactivex.Single
import java.util.*

class CoinService(private val impl: CoinUsecase = CoinServiceImpl()) : JooqReactiveService() {

    @Service
    internal lateinit var localeService: LocaleService

    fun list(locale: String = Locale.KOREA.toString()): Single<List<CoinModel>> {
        return single { impl.getAllCoins(it, localeService.getMoneySymbol(locale)) }
    }

    fun list(symbols: List<String>, locale: String = Locale.KOREA.toString()): Single<List<CoinModel>> {
        return single { impl.getCoins(it, symbols, localeService.getMoneySymbol(locale)) }
    }

    fun getPrice(symbol: String, balance: Double, locale: String = Locale.KOREA.toString()): Single<Double> {
        return single { impl.getPrice(symbol, localeService.getMoneySymbol(locale), balance) }
    }

    @JvmOverloads
    fun getPrice(symbol: String, locale: String = Locale.KOREA.toString()): Single<Double> {
        return single { impl.getPrice(symbol, locale) }
    }
}
