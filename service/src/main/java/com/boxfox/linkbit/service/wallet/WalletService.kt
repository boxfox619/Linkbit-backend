package com.boxfox.linkbit.service.wallet


import com.boxfox.linkbit.common.entity.wallet.WalletCreateModel
import com.boxfox.linkbit.service.JooqReactiveService
import com.boxfox.linkbit.service.address.AddressServiceImpl
import com.boxfox.linkbit.service.address.AddressUsecase
import io.reactivex.Single

class WalletService(private val impl: WalletServiceImpl = WalletServiceImpl(),
                    private val addressImpl: AddressUsecase = AddressServiceImpl()) : JooqReactiveService() {

    fun createWallet(uid: String,
                     symbol: String,
                     password: String): Single<WalletCreateModel> = single {
        val wallet = impl.createWallet(it, symbol, password)
        addressImpl.registerRandomAddress(it, uid, symbol, wallet.address)
        wallet
    }

    fun getBalance(symbol:String, address: String): Single<Double> = single { impl.getBalance(it, symbol, address) }

}
