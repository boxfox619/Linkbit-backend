package com.boxfox.cross.service.wallet


import com.boxfox.cross.service.JooqReactiveService
import com.linkbit.android.entity.WalletModel
import io.reactivex.Completable
import io.reactivex.Single

class WalletDatabaseService(private val impl: WalletServiceImpl = WalletServiceImpl()) : JooqReactiveService(), WalletUsecase {

    override fun createWallet(uid: String,
                              symbol: String,
                              name: String,
                              address: String,
                              description: String,
                              open: Boolean,
                              major: Boolean): Single<WalletModel> = createSingle { impl.createWallet(it, uid, symbol, name, address, description, open, major) }

    override fun findByAddress(address: String): Single<WalletModel> = createSingle { impl.findByAddress(it, address) }

    override fun getMajorWallet(uid: String, symbol: String): Single<WalletModel> = createSingle { impl.getMajorWallet(it, uid, symbol) }

    override fun setMajorWallet(uid: String, symbol: String, address: String): Completable = createCompletable { impl.setMajorWallet(it, uid, symbol, address) }

    override fun updateWallet(uid: String, address: String, name: String, description: String, open: Boolean, major: Boolean): Completable = createCompletable { impl.updateWallet(it, uid, address, name, description, open, major) }

    override fun deleteWallet(uid: String, address: String): Completable = createCompletable { impl.deleteWallet(it, uid, address) }

    override fun checkOwner(uid: String, address: String): Completable = createCompletable { impl.checkOwner(it, uid, address) }
}
