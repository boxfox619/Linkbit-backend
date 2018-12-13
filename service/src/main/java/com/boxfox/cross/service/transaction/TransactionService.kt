package com.boxfox.cross.service.transaction

import com.boxfox.cross.common.entity.transaction.TransactionModel
import com.boxfox.cross.service.JooqReactiveService
import io.reactivex.Single

class TransactionService(private val impl: TransactionServiceImpl = TransactionServiceImpl()) : JooqReactiveService() {
    fun getTransaction(symbol: String, txHash: String): Single<TransactionModel> = single { impl.getTransaction(it, symbol, txHash) }
    fun getTransactionList(address: String, page: Int, count: Int): Single<List<TransactionModel>> = single { impl.getTransactionList(it, address, page, count) }
    fun getAllTransactionList(uid: String, symbol: String, page: Int, count: Int): Single<List<TransactionModel>> = single { impl.getAllTransactionList(it, symbol, uid, page, count) }
    fun getAllTransactionList(uid: String, page: Int, count: Int): Single<List<TransactionModel>> = single { impl.getAllTransactionList(it, uid, page, count) }
    fun getTransactionCount(address: String): Single<Int> = single { impl.getTransactionCount(it, address) }
    fun getAllTransactionCount(uid: String, symbol: String): Single<Int> = single { impl.getAllTransactionCount(it, symbol, uid) }
    fun getAllTransactionCount(uid: String): Single<Int> = single { impl.getAllTransactionCount(it, uid) }
}