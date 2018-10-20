package com.boxfox.cross.service.price

import com.boxfox.cross.service.JooqReactiveService
import com.boxfox.cross.service.LocaleService
import com.boxfox.cross.service.wallet.WalletService
import com.boxfox.cross.service.wallet.WalletServiceImpl
import com.boxfox.vertx.service.Service
import io.reactivex.Single
import java.util.*

class PriceService(private val impl: PriceServiceImpl = PriceServiceImpl(),
                   private val walletImpl: WalletServiceImpl = WalletServiceImpl()) : JooqReactiveService() {

    @Service
    internal lateinit var localeService: LocaleService

    @Service
    lateinit var walletService: WalletService

    fun getTotalPrice(uid: String, locale: String): Single<Double> {
        return createSingle { impl.getTotalPrice(it, walletImpl.getWalletList(it, uid), localeService.getLocaleMoneySymbol(locale)) }
    }

    fun getWalletPrice(address: String, locale: String): Single<Double> {
        return createSingle {
            val wallet = walletImpl.getWallet(it, address)
            impl.getPrice(it, wallet.coinSymbol, localeService.getLocaleMoneySymbol(locale), wallet.balance)

        }
    }

    fun getPrice(symbol: String, balance: Double): Single<Double> {
        return createSingle { impl.getPrice(it, symbol, Locale.KOREA.toString(), balance) }
    }

    fun getPrice(symbol: String, locale: String, balance: Double): Single<Double> {
        return createSingle { impl.getPrice(it, symbol, localeService.getLocaleMoneySymbol(locale), balance) }
    }

    @JvmOverloads
    fun getPrice(symbol: String, locale: String = Locale.KOREA.toString()): Single<Double> {
        return impl.getPrice(symbol, locale)
    }
}
