package com.boxfox.cross.service.transaction

import com.boxfox.cross.common.RoutingException
import com.boxfox.linkbit.wallet.WalletServiceRegistry
import com.google.api.client.http.HttpStatusCodes
import com.linkbit.android.entity.TransactionModel
import io.one.sys.db.Tables.TRANSACTION
import io.one.sys.db.tables.records.TransactionRecord
import org.jooq.DSLContext

class TransactionServiceImpl : TransactionUsecase {
    override fun getTransaction(ctx: DSLContext, symbol: String, txHash: String): TransactionModel {
        var record: TransactionRecord? = ctx.selectFrom(TRANSACTION).where(TRANSACTION.HASH.eq(txHash)).fetch().firstOrNull()
        var transaction: TransactionModel
        if (record == null) {
            transaction = WalletServiceRegistry.getService(symbol).getTransaction(txHash)
        } else {
            transaction = TransactionModel().apply {
                this.amount = record.amount
                this.date = record.datetime
                this.sourceAddress = record.sourceaddress
                this.targetAddress = record.targetaddress
                this.transactionHash = record.hash
                //this.targetProfile = record.targetProfile
                //this.confirmation = record.comparmation
                //this.blockNumber = record.blockNumber
            }
        }
        if (transaction == null) {
            throw RoutingException(HttpStatusCodes.STATUS_CODE_NOT_FOUND, "transaction not found")
        }
        return transaction
    }

    override fun getTransactionList(ctx: DSLContext, symbol: String, uid: String, address: String, page: Int, count: Int): List<TransactionModel> {
    }

    override fun getAllTransactionList(ctx: DSLContext, symbol: String, uid: String, page: Int, count: Int): List<TransactionModel> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTransactionCount(ctx: DSLContext, symbol: String, uid: String, address: String): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllTransactionCount(ctx: DSLContext, symbol: String, uid: String): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}