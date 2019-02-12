package com.boxfox.linkbit.service.transaction

import com.boxfox.linkbit.common.entity.transaction.TransactionModel
import org.jooq.DSLContext

interface TransactionUsecase {
    fun getTransaction(ctx: DSLContext, symbol: String, txHash: String): TransactionModel
    fun getTransactionList(ctx: DSLContext, symbol: String, address: String, page: Int, count: Int): List<TransactionModel>
    fun getTransactionCount(ctx: DSLContext, address: String): Int
    fun getTransactionList(symbol: String, address: String, lastBlock: Int): List<TransactionModel>
}