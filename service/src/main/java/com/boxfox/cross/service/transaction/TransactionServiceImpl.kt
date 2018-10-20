package com.boxfox.cross.service.transaction

import com.boxfox.cross.common.RoutingException
import com.boxfox.cross.entity.transaction.TransactionRecordEntityMapper
import com.boxfox.linkbit.wallet.WalletServiceRegistry
import com.google.api.client.http.HttpStatusCodes
import com.linkbit.android.entity.TransactionModel
import io.one.sys.db.Tables.TRANSACTION
import io.one.sys.db.Tables.WALLET
import io.one.sys.db.tables.records.TransactionRecord
import org.jooq.DSLContext
import java.util.*

class TransactionServiceImpl : TransactionUsecase {
    override fun getTransaction(ctx: DSLContext, symbol: String, txHash: String): TransactionModel {
        var record: TransactionRecord? = ctx.selectFrom(TRANSACTION).where(TRANSACTION.HASH.eq(txHash)).fetch().firstOrNull()
        var transaction: TransactionModel
        if (record == null) {
            transaction = WalletServiceRegistry.getService(symbol).getTransaction(txHash)
        } else {
            transaction = TransactionRecordEntityMapper.fromRecord(record)
        }
        if (transaction == null) {
            throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "transaction not found")
        }
        return transaction
    }

    override fun getTransactionList(ctx: DSLContext, address: String, page: Int, count: Int): List<TransactionModel> {
        var records = ctx
                .selectFrom(TRANSACTION.join(WALLET).on(TRANSACTION.SOURCEADDRESS.eq(WALLET.ADDRESS).or(TRANSACTION.TARGETADDRESS.eq(WALLET.ADDRESS))))
                .where(WALLET.ADDRESS.eq(address).or(WALLET.CROSSADDRESS.eq(address)))
                .orderBy(TRANSACTION.DATETIME)
                .offset(page * count)
                .limit(count)
                .fetch()
        val transactionList = ArrayList<TransactionModel>()
        if (records.isEmpty()) {
            ctx.selectFrom(WALLET).where(WALLET.ADDRESS.eq(address).or(WALLET.CROSSADDRESS.eq(address))).fetch().forEach { record ->
                WalletServiceRegistry.getService(record.symbol).requestTransactionIndexing(record.address)
            }
        }
        for (record in records) {
            transactionList.add(TransactionRecordEntityMapper.fromRecord(record))
        }
        return transactionList
    }

    override fun getAllTransactionList(ctx: DSLContext, symbol: String, uid: String, page: Int, count: Int): List<TransactionModel> {
        var records = ctx
                .selectFrom(TRANSACTION.join(WALLET).on(TRANSACTION.SOURCEADDRESS.eq(WALLET.ADDRESS).or(TRANSACTION.TARGETADDRESS.eq(WALLET.ADDRESS))))
                .where(WALLET.UID.eq(uid).and(WALLET.SYMBOL.eq(symbol)))
                .orderBy(TRANSACTION.DATETIME)
                .offset(page * count)
                .limit(count)
                .fetch()
        val totalTxStatusList = ArrayList<TransactionModel>()
        if (records.isEmpty()) {
            ctx.selectFrom(WALLET).where(WALLET.UID.eq(uid)).fetch().forEach { record ->
                WalletServiceRegistry.getService(record.symbol).requestTransactionIndexing(record.address)
            }
        }
        for (record in records) {
            totalTxStatusList.add(TransactionRecordEntityMapper.fromRecord(record))
        }
        return totalTxStatusList
    }

    override fun getTransactionCount(ctx: DSLContext, address: String): Int {
        val count = ctx.selectFrom(TRANSACTION.join(WALLET).on(TRANSACTION.SOURCEADDRESS.eq(WALLET.ADDRESS).or(TRANSACTION.TARGETADDRESS.eq(WALLET.ADDRESS))))
                .where(WALLET.ADDRESS.eq(address).or(WALLET.CROSSADDRESS.eq(address)))
                .fetch()
                .size
        return count
    }

    override fun getAllTransactionCount(ctx: DSLContext, symbol: String, uid: String): Int {
        val count = ctx.selectFrom(TRANSACTION.join(WALLET).on(TRANSACTION.SOURCEADDRESS.eq(WALLET.ADDRESS).or(TRANSACTION.TARGETADDRESS.eq(WALLET.ADDRESS))))
                .where(WALLET.UID.eq(uid).and(WALLET.SYMBOL.eq(symbol)))
                .fetch()
                .size
        return count
    }

}