package com.boxfox.cross.service.transaction

import com.boxfox.cross.service.JooqReactiveService
import com.linkbit.android.entity.TransactionModel
import io.reactivex.Single

class TransactionService(private val impl: TransactionServiceImpl = TransactionServiceImpl()) : JooqReactiveService() {
    fun getTransaction(symbol: String, txHash: String): Single<TransactionModel> = createSingle { impl.getTransaction(it, symbol, txHash) }
    fun getTransactionList(address: String, page: Int, count: Int): Single<List<TransactionModel>> = createSingle { impl.getTransactionList(it, address, page, count) }
    fun getAllTransactionList(uid: String, symbol: String, page: Int, count: Int): Single<List<TransactionModel>> = createSingle { impl.getAllTransactionList(it, symbol, uid, page, count) }
    fun getAllTransactionList(uid: String, page: Int, count: Int): Single<List<TransactionModel>> = createSingle { impl.getAllTransactionList(it, uid, page, count) }
    fun getTransactionCount(address: String): Single<Int> = createSingle { impl.getTransactionCount(it, address) }
    fun getAllTransactionCount(uid: String, symbol: String): Single<Int> = createSingle { impl.getAllTransactionCount(it, symbol, uid) }
    fun getAllTransactionCount(uid: String): Single<Int> = createSingle { impl.getAllTransactionCount(it, uid) }
}