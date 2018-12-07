package com.boxfox.cross.service.transaction

import com.boxfox.cross.common.entity.transaction.TransactionModel
import org.jooq.DSLContext

interface TransactionUsecase {
    fun getTransaction(ctx: DSLContext, symbol: String, txHash: String): TransactionModel
    fun getTransactionList(ctx: DSLContext, address: String, page: Int, count: Int): List<TransactionModel>
    fun getAllTransactionList(ctx: DSLContext, symbol: String, uid: String, page: Int, count: Int): List<TransactionModel>
    fun getAllTransactionList(ctx: DSLContext, uid: String, page: Int, count: Int): List<TransactionModel>
    fun getTransactionCount(ctx: DSLContext, address: String): Int
    fun getAllTransactionCount(ctx: DSLContext, symbol: String, uid: String): Int
    fun getAllTransactionCount(ctx: DSLContext, uid: String): Int
}