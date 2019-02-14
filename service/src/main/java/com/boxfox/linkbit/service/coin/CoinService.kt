package com.boxfox.linkbit.service.coin

import com.boxfox.linkbit.common.entity.coin.CoinModel
import com.boxfox.linkbit.common.entity.coin.CoinPriceModel
import com.boxfox.linkbit.service.JooqReactiveService
import com.boxfox.linkbit.service.currency.CurrencyServiceImpl
import com.boxfox.linkbit.service.currency.CurrencyUsecase
import io.reactivex.Single
import java.util.*

class CoinService(private val impl: CoinUsecase = CoinServiceImpl(),
                  private val currencyImpl: CurrencyUsecase = CurrencyServiceImpl()) : JooqReactiveService() {

    fun getCoins(locale: String = Locale.KOREA.language): Single<List<CoinModel>> {
        return single { impl.getCoins(it, locale) }
    }

    fun getPrices(currency: String = "USD", locale: String = Locale.KOREA.language): Single<List<CoinPriceModel>> {
        return single {
            val value = currencyImpl.getCurrency(currency)
            impl.getAllPrices(it, locale).map { c ->
                c.price = c.price * value
                c
            }
        }
    }

    fun getPrices(symbols: List<String>, currency: String = "USD", locale: String = Locale.KOREA.language): Single<List<CoinPriceModel>> {
        return single {
            val value = currencyImpl.getCurrency(currency)
            impl.getPrices(it, symbols, locale).map { c ->
                c.price = c.price * value
                c
            }
        }
    }

    fun getPrice(symbol: String, balance: Double, currency: String = "USD"): Single<Double> {
        return single {
            val value = currencyImpl.getCurrency(currency)
            impl.getPrice(symbol, balance) * value
        }
    }

    @JvmOverloads
    fun getPrice(symbol: String, currency: String = "USD"): Single<Double> {
        return single {
            val value = currencyImpl.getCurrency(currency)
            impl.getPrice(symbol) * value
        }
    }
}
