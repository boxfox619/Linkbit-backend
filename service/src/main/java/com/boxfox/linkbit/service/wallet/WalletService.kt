package com.boxfox.linkbit.service.wallet


import com.boxfox.linkbit.common.entity.wallet.WalletCreateModel
import com.boxfox.linkbit.common.entity.wallet.WalletModel
import io.reactivex.Completable
import io.reactivex.Single

class WalletService(private val impl: WalletServiceImpl = WalletServiceImpl()) : com.boxfox.linkbit.service.JooqReactiveService() {

    fun createWallet(uid: String,
                     symbol: String,
                     password: String,
                     name: String,
                     description: String,
                     open: Boolean,
                     major: Boolean): Single<WalletCreateModel> = single { impl.createWallet(it, uid, symbol, password, name, description, open, major) }

    fun getWalletList(uid: String): Single<List<WalletModel>> = single { impl.getWalletList(it, uid) }

    fun findByAddress(address: String): Single<WalletModel> = single { impl.getWallet(it, address) }

    fun getMajorWallet(uid: String, symbol: String): Single<WalletModel> = single { impl.getMajorWallet(it, uid, symbol) }

    fun setMajorWallet(uid: String, symbol: String, address: String): Completable = completable { impl.setMajorWallet(it, uid, symbol, address) }

    fun updateWallet(uid: String, address: String, name: String, description: String, open: Boolean, major: Boolean): Completable = completable { impl.updateWallet(it, uid, address, name, description, open, major) }

    fun deleteWallet(uid: String, address: String): Completable = completable { impl.deleteWallet(it, uid, address) }

    fun checkOwner(uid: String, address: String): Completable = completable { impl.checkOwner(it, uid, address) }

    fun getBalance(symbol:String, address: String): Single<Double> = single { impl.getBalance(it, symbol, address) }

    fun getBalance(address: String): Single<Double> = single { impl.getBalance(it, address) }

    fun getTotalBalance(uid: String, symbol: String): Single<Double> = single { impl.getTotalBalance(it, uid, symbol) }
}
