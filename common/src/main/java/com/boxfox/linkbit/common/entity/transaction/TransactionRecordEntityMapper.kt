package com.boxfox.linkbit.common.entity.transaction

import io.one.sys.db.Tables.TRANSACTION
import org.jooq.Record

object TransactionRecordEntityMapper {
    fun fromRecord(record: Record): TransactionModel {
        return TransactionModel().apply {
            this.amount = record.get(TRANSACTION.AMOUNT)
            this.date = record.get(TRANSACTION.DATETIME)
            this.sourceAddress = record.get(TRANSACTION.SOURCEADDRESS)
            this.targetAddress = record.get(TRANSACTION.TARGETADDRESS)
            this.hash = record.get(TRANSACTION.HASH)
            //this.targetProfile = record.targetProfile
            //this.confirmation = record.comparmation
            //this.blockNumber = record.blockNumber
            //@TODO Implement transaction model more info
        }
    }
}