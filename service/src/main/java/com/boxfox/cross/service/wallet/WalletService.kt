package com.boxfox.cross.service.wallet


import com.boxfox.cross.entity.wallet.WalletCreateModel
import com.boxfox.cross.service.JooqReactiveService
import com.linkbit.android.entity.WalletModel
import io.reactivex.Completable
import io.reactivex.Single

class WalletService(private val impl: WalletServiceImpl = WalletServiceImpl()) : JooqReactiveService() {

    fun createWallet(uid: String,
                     symbol: String,
                     password: String,
                     name: String,
                     description: String,
                     open: Boolean,
                     major: Boolean): Single<WalletCreateModel> = createSingle { impl.createWallet(it, uid, symbol, password, name, description, open, major) }

    fun getWalletList(uid: String): Single<WalletModel> = createSingle { impl.getWalletList(it, uid) }

    fun findByAddress(address: String): Single<WalletModel> = createSingle { impl.getWallet(it, address) }

    fun getMajorWallet(uid: String, symbol: String): Single<WalletModel> = createSingle { impl.getMajorWallet(it, uid, symbol) }

    fun setMajorWallet(uid: String, symbol: String, address: String): Completable = createCompletable { impl.setMajorWallet(it, uid, symbol, address) }

    fun updateWallet(uid: String, address: String, name: String, description: String, open: Boolean, major: Boolean): Completable = createCompletable { impl.updateWallet(it, uid, address, name, description, open, major) }

    fun deleteWallet(uid: String, address: String): Completable = createCompletable { impl.deleteWallet(it, uid, address) }

    fun checkOwner(uid: String, address: String): Completable = createCompletable { impl.checkOwner(it, uid, address) }

    fun getBalance(symbol:String, address: String): Single<Double> = createSingle { impl.getBalance(it, symbol, address) }

    fun getBalance(address: String): Single<Double> = createSingle { impl.getBalance(it, address) }

    fun getTotalBalance(uid: String, symbol: String): Single<Double> = createSingle { impl.getTotalBalance(it, uid, symbol) }
}
