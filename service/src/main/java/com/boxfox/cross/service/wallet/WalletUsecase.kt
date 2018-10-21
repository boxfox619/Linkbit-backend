package com.boxfox.cross.service.wallet

import com.boxfox.cross.common.entity.wallet.WalletCreateModel
import com.linkbit.android.entity.WalletModel
import org.jooq.DSLContext

interface WalletUsecase {
    fun createWallet(ctx: DSLContext, uid: String, symbol: String, name: String, address: String, description: String, open: Boolean, major: Boolean): WalletCreateModel
    fun getWallet(ctx: DSLContext, address: String): WalletModel
    fun getWalletList(ctx: DSLContext, uid: String): List<WalletModel>
    fun getMajorWallet(ctx: DSLContext, uid: String, symbol: String): WalletModel
    fun setMajorWallet(ctx: DSLContext, uid: String, symbol: String, address: String)
    fun updateWallet(ctx: DSLContext, uid: String, address: String, name: String, description: String, open: Boolean, major: Boolean)
    fun deleteWallet(ctx: DSLContext, uid: String, address: String)
    fun checkOwner(ctx: DSLContext, uid: String, address: String)
    fun getBalance(ctx: DSLContext, symbol: String, address: String): Double
    fun getBalance(ctx: DSLContext, address: String): Double
    fun getTotalBalance(ctx: DSLContext, uid: String, symbol: String): Double
}