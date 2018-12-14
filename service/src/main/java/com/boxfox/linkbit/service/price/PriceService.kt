package com.boxfox.linkbit.service.price

import com.boxfox.linkbit.service.wallet.WalletService
import com.boxfox.linkbit.service.wallet.WalletServiceImpl
import com.boxfox.vertx.service.Service
import io.reactivex.Single
import java.util.*

class PriceService(private val impl: PriceServiceImpl = PriceServiceImpl(),
                   private val walletImpl: WalletServiceImpl = WalletServiceImpl()) : com.boxfox.linkbit.service.JooqReactiveService() {

    @Service
    internal lateinit var localeService: com.boxfox.linkbit.service.LocaleService

    @Service
    lateinit var walletService: WalletService

    fun getTotalPrice(uid: String, locale: String): Single<Double> {
        return single { impl.getTotalPrice(it, walletImpl.getWalletList(it, uid), localeService.getLocaleMoneySymbol(locale)) }
    }

    fun getWalletPrice(address: String, locale: String): Single<Double> {
        return single {
            val wallet = walletImpl.getWallet(it, address)
            impl.getPrice(it, wallet.coinSymbol, localeService.getLocaleMoneySymbol(locale), wallet.balance)

        }
    }

    fun getTotalPrice(uid: String, symbol: String, locale: String): Single<Double> {
        return single {
            val wallets = walletImpl.getWalletList(it,uid, symbol)
            impl.getTotalPrice(it, wallets, localeService.getLocaleMoneySymbol(locale))
        }
    }

    fun getPrice(symbol: String, balance: Double): Single<Double> {
        return single { impl.getPrice(it, symbol, Locale.KOREA.toString(), balance) }
    }

    fun getPrice(symbol: String, locale: String, balance: Double): Single<Double> {
        return single { impl.getPrice(it, symbol, localeService.getLocaleMoneySymbol(locale), balance) }
    }

    @JvmOverloads
    fun getPrice(symbol: String, locale: String = Locale.KOREA.toString()): Single<Double> {
        return single { impl.getPrice(symbol, locale) }
    }
}
