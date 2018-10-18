package com.boxfox.cross.service.wallet

import com.linkbit.android.entity.WalletModel
import io.reactivex.Completable
import io.reactivex.Single
import org.jooq.DSLContext

interface WalletUsecase {
    fun createWallet(ctx: DSLContext, uid: String, symbol: String, name: String, address: String, description: String, open: Boolean, major: Boolean): WalletModel
    fun findByAddress(ctx: DSLContext, address: String): WalletModel
    fun getMajorWallet(ctx: DSLContext, uid:String, symbol: String): WalletModel
    fun setMajorWallet(ctx: DSLContext, uid: String, symbol: String, address: String)
    fun updateWallet(ctx: DSLContext, uid: String, address: String, name: String, description: String, open: Boolean, major: Boolean)
    fun deleteWallet(ctx: DSLContext, uid: String, address: String)
    fun checkOwner(ctx: DSLContext, uid: String, address:String)
}