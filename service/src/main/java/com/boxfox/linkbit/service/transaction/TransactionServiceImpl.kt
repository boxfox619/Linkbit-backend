package com.boxfox.linkbit.service.transaction

import com.boxfox.linkbit.common.RoutingException
import com.boxfox.linkbit.common.entity.transaction.TransactionModel
import com.boxfox.linkbit.common.entity.transaction.TransactionRecordEntityMapper
import com.boxfox.linkbit.wallet.WalletServiceRegistry
import com.google.api.client.http.HttpStatusCodes
import io.one.sys.db.Tables.TRANSACTION
import io.one.sys.db.Tables.WALLET
import io.one.sys.db.tables.records.TransactionRecord
import org.jooq.DSLContext
import java.util.*

class TransactionServiceImpl : TransactionUsecase {

    override fun getTransaction(ctx: DSLContext, symbol: String, txHash: String): TransactionModel {
        val record: TransactionRecord? = ctx.selectFrom(TRANSACTION).where(TRANSACTION.HASH.eq(txHash)).fetch().firstOrNull()
        val transaction: TransactionModel
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

    override fun getTransactionList(ctx: DSLContext, symbol: String, address: String, page: Int, count: Int): List<TransactionModel> {
        return WalletServiceRegistry.getService(symbol).getTransactionList(address)
    }

    override fun getTransactionCount(ctx: DSLContext, address: String): Int {
        val count = ctx.selectCount().from(TRANSACTION.join(WALLET).on(TRANSACTION.SOURCEADDRESS.eq(WALLET.ADDRESS).or(TRANSACTION.TARGETADDRESS.eq(WALLET.ADDRESS))))
                .where(WALLET.ADDRESS.eq(address).or(WALLET.CROSSADDRESS.eq(address)))
                .fetch()
                .size
        return count
    }

    override fun getTransactionList(symbol: String, address: String, lastBlock: Int): List<TransactionModel> {
        return WalletServiceRegistry.getService(symbol).indexingTransactions(address, lastBlock)
    }
}