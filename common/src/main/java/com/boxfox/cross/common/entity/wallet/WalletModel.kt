package com.boxfox.cross.common.entity.wallet


open class WalletModel {
    lateinit var  ownerId: String
    lateinit var  ownerName: String
    lateinit var  walletName: String
    lateinit var  coinSymbol: String
    lateinit var  description: String
    lateinit var  accountAddress: String
    lateinit var  linkbitAddress: String
    var balance: Double = 0.0
}