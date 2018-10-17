package com.boxfox.cross.service.wallet

import com.boxfox.cross.service.ServiceException
import com.boxfox.cross.util.AddressUtil
import com.linkbit.android.entity.WalletModel
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import org.jooq.Record
import org.jooq.Result

import com.google.api.client.http.HttpStatusCodes
import io.one.sys.db.tables.Account.ACCOUNT
import io.one.sys.db.tables.Majorwallet.MAJORWALLET
import io.one.sys.db.tables.Wallet.WALLET
import org.jooq.DSLContext

class WalletServiceImpl {

    fun createWallet(ctx: DSLContext, uid: String, symbol: String, name: String, address: String, description: String, open: Boolean, major: Boolean): WalletModel {
        val linkedAddress = AddressUtil.createRandomAddress(ctx)
        ctx.insertInto(WALLET)
                .values(uid, symbol.toUpperCase(), name, description, address, linkedAddress, open, major)
                .execute()
        return findByAddress(ctx, address)
    }

    fun findByAddress(ctx: DSLContext, address: String): WalletModel {
        var result: Result<Record>
        if (AddressUtil.isCrossAddress(address)) {
            result = ctx.selectFrom(WALLET.join(ACCOUNT).on(ACCOUNT.UID.eq(WALLET.UID))).where(WALLET.CROSSADDRESS.eq(address)).fetch()
            if (result.size == 0) {
                result = ctx.selectFrom(
                        ACCOUNT.join(MAJORWALLET).on(MAJORWALLET.UID.eq(ACCOUNT.UID))
                                .join(WALLET).on(WALLET.ADDRESS.eq(MAJORWALLET.ADDRESS))).where(ACCOUNT.ADDRESS.eq(address))
                        .fetch()
            }
        } else {
            result = ctx.selectFrom(WALLET.join(ACCOUNT).on(ACCOUNT.UID.eq(WALLET.UID)))
                    .where(WALLET.ADDRESS.eq(address))
                    .fetch()
        }
        if (result.size > 0) {
            val record = result[0]
            val wallet = getWalletFromRecord(record)
            return wallet
        } else {
            //@TODO un saved wallet data response
            throw ServiceException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "wallet not found")
        }
    }

    fun getMajorWallet(ctx: DSLContext, uid: String, symbol: String): WalletModel {
        val records = ctx.selectFrom(MAJORWALLET
                .join(WALLET).on(MAJORWALLET.ADDRESS.eq(WALLET.ADDRESS)
                        .and(MAJORWALLET.SYMBOL.eq(WALLET.SYMBOL)))
                .join(ACCOUNT).on(WALLET.UID.eq(ACCOUNT.UID)))
                .where(MAJORWALLET.UID.eq(uid).and(MAJORWALLET.SYMBOL.eq(symbol)))
                .fetch()
        if (records.size > 0) {
            val record = records[0]
            return getWalletFromRecord(record)
        }
        throw ServiceException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "Major wallet not found")
    }

    fun setMajorWallet(ctx: DSLContext, uid: String, symbol: String, address: String) {
        val record = ctx.selectFrom(MAJORWALLET)
                .where(MAJORWALLET.UID.eq(uid).and(MAJORWALLET.SYMBOL.eq(symbol))).fetch().stream().findFirst().orElse(null)
        if (record != null) {
            ctx.update(MAJORWALLET).set(MAJORWALLET.ADDRESS, address)
                    .where(MAJORWALLET.UID.eq(uid).and(MAJORWALLET.SYMBOL.eq(symbol))).execute()
        } else {
            ctx.insertInto(MAJORWALLET).values(uid, symbol, address).execute()
        }
    }

    fun updateWallet(ctx: DSLContext, uid: String, address: String, name: String, description: String, open: Boolean, major: Boolean) {
        val updatedRows = ctx.update(WALLET)
                .set(WALLET.NAME, name)
                .set(WALLET.DESCRIPTION, description)
                .set(WALLET.MAJOR, major)
                .set(WALLET.PUBLISH, open)
                .where(WALLET.ADDRESS.eq(address).and(WALLET.UID.eq(uid)))
                .execute()
        if (updatedRows == 0) {
            throw ServiceException(HttpStatusCodes.STATUS_CODE_NOT_MODIFIED, "Wallet update fail")
        }
    }

    fun deleteWallet(ctx: DSLContext, uid: String, address: String) {
        val deletedRows = ctx.delete(WALLET).where(WALLET.ADDRESS.eq(address).and(WALLET.UID.eq(uid))).execute()
        if (deletedRows == 0) {
            throw ServiceException(HttpStatusCodes.STATUS_CODE_NOT_MODIFIED, "Wallet update fail")
        }
    }

    fun checkOwner(ctx: DSLContext, uid: String, address: String) {
        val result = ctx.selectFrom(WALLET).where(WALLET.ADDRESS.eq(address).and(WALLET.UID.eq(uid))).fetch()
        if (result.size == 0) {
            throw ServiceException(HttpStatusCodes.STATUS_CODE_SEE_OTHER, "Not owner")
        }
    }

    companion object {

        fun getWalletFromRecord(record: Record): WalletModel {
            val wallet = WalletModel()
            wallet.ownerId = record.getValue(WALLET.UID)
            wallet.ownerName = record.getValue(ACCOUNT.NAME)
            wallet.walletName = record.getValue(WALLET.NAME)
            wallet.coinSymbol = record.getValue(WALLET.SYMBOL)
            wallet.description = record.getValue(WALLET.DESCRIPTION)
            wallet.accountAddress = record.get(WALLET.ADDRESS)
            wallet.linkbitAddress = record.get(WALLET.CROSSADDRESS)
            return wallet
        }
    }
}
