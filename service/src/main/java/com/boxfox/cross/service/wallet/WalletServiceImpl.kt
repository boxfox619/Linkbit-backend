package com.boxfox.cross.service.wallet

import com.boxfox.cross.common.RoutingException
import com.boxfox.cross.common.entity.wallet.WalletCreateModel
import com.boxfox.cross.common.entity.wallet.WalletModel
import com.boxfox.cross.common.entity.wallet.WalletRecordEntityMapper
import com.boxfox.cross.util.AddressUtil
import com.boxfox.linkbit.wallet.WalletServiceRegistry
import com.google.api.client.http.HttpStatusCodes
import io.one.sys.db.tables.Account.ACCOUNT
import io.one.sys.db.tables.Majorwallet.MAJORWALLET
import io.one.sys.db.tables.Wallet.WALLET
import io.one.sys.db.tables.records.WalletRecord
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Result

class WalletServiceImpl : WalletUsecase {

    override fun createWallet(ctx: DSLContext, uid: String, symbol: String, password: String, name: String, description: String, open: Boolean, major: Boolean): WalletCreateModel {
        val walletCreateResult = WalletServiceRegistry.getService(symbol).createWallet(password)
        val result = WalletCreateModel()
        result.walletData = walletCreateResult.walletData.toString()
        result.walletFileName = walletCreateResult.walletName
        result.accountAddress = walletCreateResult.address
        val originalAddress = walletCreateResult.address
        val linkedAddress = AddressUtil.createRandomAddress(ctx)
        ctx.insertInto(WALLET)
                .values(uid, symbol.toUpperCase(), name, description, originalAddress, linkedAddress, open, major)
                .execute()
        val wallet = getWallet(ctx, originalAddress)
        result.ownerName = wallet.ownerName
        result.linkbitAddress = wallet.linkbitAddress
        result.walletName = wallet.walletName
        result.description = wallet.description
        result.coinSymbol = wallet.coinSymbol
        result.balance = wallet.balance
        result.ownerId = wallet.ownerId
        return result
    }

    override fun getWalletList(ctx: DSLContext, uid: String): List<WalletModel> {
        val records = ctx.selectFrom(WALLET.join(ACCOUNT).on(WALLET.UID.eq(ACCOUNT.UID))).where(WALLET.UID.eq(uid)).fetch()
        return records.map {
            val service = WalletServiceRegistry.getService(it.get(WALLET.SYMBOL))
            val balance = service.getBalance(it.get(WALLET.ADDRESS))
            val walletModel = WalletRecordEntityMapper.fromRecord(it)
            walletModel.balance = balance
            walletModel
        }
    }

    override fun getWalletList(ctx: DSLContext, uid: String, symbol: String): List<WalletModel> {
        val records = ctx.selectFrom(WALLET.join(ACCOUNT).on(WALLET.UID.eq(ACCOUNT.UID))).where(WALLET.UID.eq(uid).and(WALLET.SYMBOL.eq(symbol))).fetch()
        return records.map {
            val service = WalletServiceRegistry.getService(it.get(WALLET.SYMBOL))
            val balance = service.getBalance(it.get(WALLET.ADDRESS))
            val walletModel = WalletRecordEntityMapper.fromRecord(it)
            walletModel.balance = balance
            walletModel
        }
    }

    override fun getWallet(ctx: DSLContext, address: String): WalletModel {
        var result: Result<Record>
        if (AddressUtil.isCrossAddress(address)) {
            result = ctx.selectFrom(WALLET.join(ACCOUNT).on(ACCOUNT.UID.eq(WALLET.UID))).where(WALLET.CROSSADDRESS.eq(address)).fetch()
            if (result.size == 0) {
                result = ctx.selectFrom(ACCOUNT.join(MAJORWALLET).on(MAJORWALLET.UID.eq(ACCOUNT.UID))
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
            throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "wallet not found")
        }
    }

    override fun getMajorWallet(ctx: DSLContext, uid: String, symbol: String): WalletModel {
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
        throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "Major wallet not found")
    }

    override fun setMajorWallet(ctx: DSLContext, uid: String, symbol: String, address: String) {
        val record = ctx.selectFrom(MAJORWALLET)
                .where(MAJORWALLET.UID.eq(uid).and(MAJORWALLET.SYMBOL.eq(symbol))).fetch().stream().findFirst().orElse(null)
        if (record != null) {
            ctx.update(MAJORWALLET).set(MAJORWALLET.ADDRESS, address)
                    .where(MAJORWALLET.UID.eq(uid).and(MAJORWALLET.SYMBOL.eq(symbol))).execute()
        } else {
            ctx.insertInto(MAJORWALLET).values(uid, symbol, address).execute()
        }
    }

    override fun updateWallet(ctx: DSLContext, uid: String, address: String, name: String, description: String, open: Boolean, major: Boolean) {
        val updatedRows = ctx.update(WALLET)
                .set(WALLET.NAME, name)
                .set(WALLET.DESCRIPTION, description)
                .set(WALLET.MAJOR, major)
                .set(WALLET.PUBLISH, open)
                .where(WALLET.ADDRESS.eq(address).and(WALLET.UID.eq(uid)))
                .execute()
        if (updatedRows == 0) {
            throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_MODIFIED, "Wallet update fail")
        }
    }

    override fun deleteWallet(ctx: DSLContext, uid: String, address: String) {
        val deletedRows = ctx.delete(WALLET).where(WALLET.ADDRESS.eq(address).and(WALLET.UID.eq(uid))).execute()
        if (deletedRows == 0) {
            throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_MODIFIED, "Wallet update fail")
        }
    }

    override fun checkOwner(ctx: DSLContext, uid: String, address: String) {
        val result = ctx.selectFrom(WALLET).where(WALLET.ADDRESS.eq(address).and(WALLET.UID.eq(uid))).fetch()
        if (result.size == 0) {
            throw RoutingException(HttpStatusCodes.STATUS_CODE_SEE_OTHER, "Not owner")
        }
    }

    override fun getBalance(ctx: DSLContext, symbol: String, address: String): Double {
        val service = WalletServiceRegistry.getService(symbol)
        if (service == null) {
            throw RoutingException(HttpStatusCodes.STATUS_CODE_SERVER_ERROR)
        } else {
            return service.getBalance(address)
        }
    }

    override fun getBalance(ctx: DSLContext, address: String): Double {
        val wallet = this.getWallet(ctx, address)
        return getBalance(ctx, wallet.coinSymbol, wallet.accountAddress)
    }

    override fun getTotalBalance(ctx: DSLContext, uid: String, symbol: String): Double {
        val records: Result<WalletRecord> = ctx.selectFrom(WALLET).where(WALLET.UID.eq(uid).and(WALLET.SYMBOL.eq(symbol))).fetch()
        var balance = 0.0
        for (record in records) {
            balance += getBalance(ctx, record.symbol, record.address)
        }
        return balance
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
