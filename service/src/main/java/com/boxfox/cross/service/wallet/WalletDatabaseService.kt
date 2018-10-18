package com.boxfox.cross.service.wallet


import com.boxfox.cross.service.JooqReactiveService
import com.linkbit.android.entity.WalletModel
import io.reactivex.Completable
import io.reactivex.Single

class WalletDatabaseService(private val impl: WalletServiceImpl = WalletServiceImpl()) : JooqReactiveService() {

    fun createWallet(uid: String,
                     symbol: String,
                     name: String,
                     address: String,
                     description: String,
                     open: Boolean,
                     major: Boolean): Single<WalletModel> = createSingle { impl.createWallet(it, uid, symbol, name, address, description, open, major) }

    fun findByAddress(address: String): Single<WalletModel> = createSingle { impl.findByAddress(it, address) }

    fun getMajorWallet(uid: String, symbol: String): Single<WalletModel> = createSingle { impl.getMajorWallet(it, uid, symbol) }

    fun setMajorWallet(uid: String, symbol: String, address: String): Completable = createCompletable { impl.setMajorWallet(it, uid, symbol, address) }

    fun updateWallet(uid: String, address: String, name: String, description: String, open: Boolean, major: Boolean): Completable = createCompletable { impl.updateWallet(it, uid, address, name, description, open, major) }

    fun deleteWallet(uid: String, address: String): Completable = createCompletable { impl.deleteWallet(it, uid, address) }

    fun checkOwner(uid: String, address: String): Completable = createCompletable { impl.checkOwner(it, uid, address) }
}
