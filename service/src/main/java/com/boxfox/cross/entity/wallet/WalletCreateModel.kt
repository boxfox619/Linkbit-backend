package com.boxfox.cross.entity.wallet

import com.linkbit.android.entity.WalletModel

class WalletCreateModel : WalletModel() {
    lateinit var walletFileName: String
    lateinit var walletData: String
}