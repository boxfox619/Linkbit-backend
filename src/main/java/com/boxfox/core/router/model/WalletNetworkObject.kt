package com.boxfox.core.router.model

import com.google.gson.annotations.SerializedName

open class WalletNetworkObject {
    @SerializedName("ownerId") lateinit var  ownerId: String
    @SerializedName("ownerName") lateinit var  ownerName: String
    @SerializedName("name") lateinit var  walletName: String
    @SerializedName("coin") lateinit var  coinSymbol: String
    @SerializedName("description") lateinit var  description: String
    @SerializedName("accountAddress") lateinit var  accountAddress: String
    @SerializedName("linkbitAddress") lateinit var  linkbitAddress: String
    @SerializedName("balance") var balance: Double = 0.0
}