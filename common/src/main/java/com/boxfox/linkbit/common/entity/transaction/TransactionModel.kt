package com.boxfox.linkbit.common.entity.transaction

class TransactionModel {
    lateinit var hash: String
    lateinit var sourceAddress: String
    lateinit var targetAddress: String
    var status: Boolean = false
    var amount: Double = 0.toDouble()
    lateinit var targetProfile: String
    lateinit var date: String
    var block: Int = 0
    var confirm: Int = 0
}