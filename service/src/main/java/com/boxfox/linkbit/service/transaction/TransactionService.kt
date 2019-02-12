package com.boxfox.linkbit.service.transaction

import com.boxfox.linkbit.common.entity.transaction.TransactionModel
import com.boxfox.linkbit.service.JooqReactiveService
import io.reactivex.Single

class TransactionService(private val impl: TransactionUsecase = TransactionServiceImpl()) : JooqReactiveService() {
    fun getTransaction(symbol: String, txHash: String): Single<TransactionModel> = single { impl.getTransaction(it, symbol, txHash) }
    fun getTransactions(symbol: String, txList: List<String>): Single<List<TransactionModel>> = single { ctx -> txList.map { impl.getTransaction(ctx, symbol, it) } }
    fun getTransactionList(symbol: String, address: String, page: Int, count: Int): Single<List<TransactionModel>> = single { impl.getTransactionList(it, symbol, address, page, count) }
    fun getTransactions(symbol: String, address: String, lastBlock: Int): Single<List<TransactionModel>> = single {impl.getTransactionList(symbol, address, lastBlock)}
}