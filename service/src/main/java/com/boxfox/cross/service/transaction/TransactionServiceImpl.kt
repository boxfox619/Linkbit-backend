package com.boxfox.cross.service.transaction

import com.linkbit.android.entity.TransactionModel
import org.jooq.DSLContext

class TransactionServiceImpl : TransactionUsecase {
    override fun getTransaction(ctx: DSLContext, symbol: String, txHash: String): TransactionModel {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTransactionList(ctx: DSLContext, symbol: String, uid: String, address: String, page: Int, count: Int): List<TransactionModel> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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