package com.boxfox.cross.entity.transaction

import com.linkbit.android.entity.TransactionModel
import io.one.sys.db.Tables.TRANSACTION
import org.jooq.Record

object TransactionRecordEntityMapper {
    fun fromRecord(record: Record): TransactionModel {
        return TransactionModel().apply {
            this.amount = record.get(TRANSACTION.AMOUNT)
            this.date = record.get(TRANSACTION.DATETIME)
            this.sourceAddress = record.get(TRANSACTION.SOURCEADDRESS)
            this.targetAddress = record.get(TRANSACTION.TARGETADDRESS)
            this.transactionHash = record.get(TRANSACTION.HASH)
            //this.targetProfile = record.targetProfile
            //this.confirmation = record.comparmation
            //this.blockNumber = record.blockNumber
            //@TODO Implement transaction model more info
        }
    }
}