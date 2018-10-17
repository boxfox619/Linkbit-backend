package com.boxfox.cross.service.wallet

import com.linkbit.android.entity.WalletModel
import io.reactivex.Completable
import io.reactivex.Single

interface WalletUsecase {
    fun createWallet(uid: String, symbol: String, name: String, address: String, description: String, open: Boolean, major: Boolean): Single<WalletModel>
    fun findByAddress(address: String): Single<WalletModel>
    fun getMajorWallet(uid:String, symbol: String): Single<WalletModel>
    fun setMajorWallet(uid: String, symbol: String, address: String): Completable
    fun updateWallet(uid: String, address: String, name: String, description: String, open: Boolean, major: Boolean):Completable
    fun deleteWallet(uid: String, address: String): Completable
    fun checkOwner(uid: String, address:String): Completable
}